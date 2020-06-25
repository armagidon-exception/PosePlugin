package ru.armagidon.poseplugin.api.poses.swim;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.poses.swim.module.*;
import ru.armagidon.poseplugin.api.ticking.TickModule;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.utils.nms.AnimationPlayer;
import ru.armagidon.poseplugin.utils.nms.FakePlayer;

public class NonStaticSwimPose extends SwimPose
{

    /**NonSolid list
     * Water, water-logged - setSwimming
     * Torch, portal, sign, door, banners, bell, hole in front - setGlide
     * Air - barrier
     * Jump - barrier higher
     * */


    private SwimModule module;

    NonStaticSwimPose(Player target) {
        super(target);
        this.module = new NoneModule();
    }

    @Override
    protected void initTickModules() {
        FakePlayer fakePlayer = new FakePlayer(getPlayer(), Pose.SWIMMING);
        TickModule moveTick = fakePlayer.move();


        addTickModule(()->{
            SwimModule.SwimModuleType type = SwimModule.SwimModuleType.NONE;
            if(BlockAirModule.test(getPlayer())){
                if(getPlayer().isFlying()){
                    type = SwimModule.SwimModuleType.FLY;
                } else {
                    if(NonSolidBlockModule.test(getPlayer())){
                        Block above = VectorUtils.getBlock(getPlayer().getLocation()).getRelative(BlockFace.UP);
                        boolean unsolidabove = !above.getType().isSolid() || Tag.PORTALS.isTagged(above.getType()) || Tag.SIGNS.isTagged(above.getType()) || Tag.DOORS.isTagged(above.getType()) || Tag.BANNERS.isTagged(above.getType());
                        if(unsolidabove){
                            type = SwimModule.SwimModuleType.NONSOLID;
                        } else {
                            type = SwimModule.SwimModuleType.BLOCK_AIR;
                        }
                    } else {
                        type = SwimModule.SwimModuleType.BLOCK_AIR;
                    }
                }
            } else if(WaterSwimModule.test(getPlayer())){
                type = SwimModule.SwimModuleType.WATER;
            }



            if(!type.equals(module.getType())) {
                module.stop();
                switch (type){
                    case FLY:
                        module = new FlyModule(fakePlayer, getPlayer());
                        break;
                    case NONE:
                        module = new NoneModule();
                        break;
                    case WATER:
                        module = new WaterSwimModule(getPlayer());
                        break;
                    case BLOCK_AIR:
                        module = new BlockAirModule(getPlayer());
                        break;
                    case NONSOLID:
                        module = new NonSolidBlockModule(getPlayer());
                        break;
                }
            }
            module.action();

            if(!module.getType().equals(SwimModule.SwimModuleType.FLY)&&containsTickModule(moveTick)){
                removeTickModule(moveTick);
            } else if(module.getType().equals(SwimModule.SwimModuleType.FLY)&&!containsTickModule(moveTick)){
                addTickModule(moveTick);
            }
        });
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver, log);
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        if(module.getType().equals(SwimModule.SwimModuleType.BLOCK_AIR)){
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                AnimationPlayer.play(getPlayer(), onlinePlayer, Pose.SNEAKING);
            }
        }
        module.stop();
    }
    private class NoneModule implements SwimModule{

        @Override
        public void action() {}

        @Override
        public void stop() {}

        @Override
        public SwimModuleType getType() {
            return SwimModuleType.NONE;
        }
    }
}
