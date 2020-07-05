package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import java.lang.reflect.InvocationTargetException;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.*;
import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getNmsClass;

public class NonStaticSwimPose implements SwimModule
{

    /**NonSolid list
     * Water, water-logged - setSwimming
     * Torch, portal, sign, door, banners, bell, hole in front - setGlide
     * Air - barrier
     * Jump - barrier higher
     * */


    private final Player target;
    private BlockCache cache;
    private boolean under;
    private Object packet;


    public NonStaticSwimPose(Player target) {
        this.target = target;
        try {
            NMSUtils.setPlayerPose(target, Pose.SWIMMING);
            Object dataWatcher = getNmsClass("Entity").getDeclaredMethod("getDataWatcher").invoke(asNMSCopy(target));
            packet = createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, target.getEntityId(), dataWatcher, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }


        Block above = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.UP);

        PosePluginAPI.getAPI().registerListener(this);

        if(canMoveUnder(target)){
            target.setGliding(true);
            under = true;
        } else if(above.getType().isAir()) {
            cache = new BlockCache(above.getType(), above.getBlockData(), above.getLocation());
            target.sendBlockChange(above.getLocation(), Bukkit.createBlockData(Material.BARRIER));
            under = false;
        }
    }

    @Override
    public void action() {
        if (cache != null&&cache.getLocation().distance(target.getLocation())>0.5) {
            cache.restore(target);
            cache = null;
        }
        Block above = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.UP);
        if(!above.getType().isAir()){
            if(under){
                target.setGliding(false);
            } else {
                if(cache!=null){
                    cache.restore(target);
                    cache = null;
                }
            }
        } else if(canMoveUnder(target)){
            if(!under){
                if(cache!=null) {
                    cache.restore(target);
                    cache = null;
                }
                under = true;
            }
            target.setGliding(true);
        } else if (above.getType().isAir()) {
            if(under) {
                target.setGliding(false);
                under = false;
            }
            if(packet!=null)
                Bukkit.getOnlinePlayers().forEach(p->sendPacket(p,packet));
            cache = new BlockCache(above.getType(), above.getBlockData(), above.getLocation());
            target.sendBlockChange(above.getLocation(), Bukkit.createBlockData(Material.BARRIER));
        }
    }

    @Override
    public void stop() {
        if(!under&&cache!=null) cache.restore(target);
        HandlerList.unregisterAll(this);
        NMSUtils.setPlayerPose(target, Pose.SNEAKING);
        if(under) target.setGliding(false);
    }

    @Override
    public SwimPose.SwimMode getMode() {
        return SwimPose.SwimMode.MOVING;
    }

    @EventHandler
    public void onSwim(EntityToggleGlideEvent event){
        if(event.getEntity().equals(target)){
            if(under) event.setCancelled(true);
        }
    }

    private boolean canMoveUnder(Player player){
        {
            Block lookingat = VectorUtils.getDirBlock(player.getLocation());
            if(lookingat==null) return false;
            boolean unsolidlookingat = !lookingat.getType().isSolid() || Tag.PORTALS.isTagged(lookingat.getType()) || Tag.SIGNS.isTagged(lookingat.getType()) || Tag.DOORS.isTagged(lookingat.getType()) || Tag.BANNERS.isTagged(lookingat.getType());

            lookingat = lookingat.getRelative(BlockFace.UP);

            boolean solidAbovelookingat = !(!lookingat.getType().isSolid() || Tag.PORTALS.isTagged(lookingat.getType()) || Tag.SIGNS.isTagged(lookingat.getType()) || Tag.DOORS.isTagged(lookingat.getType()) || Tag.BANNERS.isTagged(lookingat.getType()));

            return unsolidlookingat && solidAbovelookingat;
        }
    }

}
