package ru.armagidon.poseplugin.api.poses.swim;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.utils.misc.ConfigurationManager;

import static ru.armagidon.poseplugin.utils.misc.ConfigurationManager.getBoolean;
import static ru.armagidon.poseplugin.utils.misc.VectorUtils.getBlock;

public class SwimPose extends PluginPose {

    private ISwimAnimationHandler handler = null;
    private final Block under;
    private final Block above;

    public SwimPose(Player player) {
        super(player);
        Location ploc = getPlayer().getLocation();
        above = getBlock(ploc.clone().add(0,1,0));
        under = getBlock(ploc.clone().subtract(0,1,0));
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver, log);
        getPlayer().setCollidable(false);
        if(getBoolean(ConfigurationManager.PACKET_SWIM)) handler = new PacketSwimHandler(getPlayer());
        else setHandler(getPlayer().getLocation());
        handler.play(getPlayer());
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        getPlayer().setCollidable(true);
        if(handler!=null)handler.stop();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIMMING;
    }

    @PersonalEventHandler
    public void move(PlayerMoveEvent event){
        if(moved(event.getFrom(),event.getTo())) {
            setHandler(getPlayer().getLocation());
            //Use swim handler
            handler.play(getPlayer());
        }
    }

    private boolean notFullHighBB(Block block){
        if(block.getBoundingBox().getHeight()<1&&block.getBoundingBox().getHeight()>0) return true;
        else if(block.getType().equals(Material.SNOW)) return true;
        else return Tag.SLABS.isTagged(block.getType());
    }

    @EventHandler
    public void swim(EntityToggleSwimEvent e){
        if(e.getEntity() instanceof Player){
            Player player = (Player) e .getEntity();
            if(getPlayer().getName().equalsIgnoreCase(player.getName())){
                if(e.isSwimming()) e.setCancelled(true);
            }
        }
    }

    private boolean moved(Location from, Location to){
        return from.getX()!=to.getX()||from.getZ()!=to.getZ();
    }


    private void setHandler(Location plocation){

        double y = plocation.getY();
        int blocky = plocation.getBlockY();
        //if player is standing on stairs, use packet handler
        if(Tag.STAIRS.isTagged(under.getType())||Tag.PORTALS.isTagged(getBlock(plocation).getType())){
            changeHandler(new PacketSwimHandler(getPlayer()));
        }
        //If y greater than block y and under-block is full-high, place barrier in two blocks above
        else if(blocky<y&&!notFullHighBB(under)){
            changeHandler(new CommonSwimHandler(2,getPlayer()));
        } else {
            changeHandler(new CommonSwimHandler(1,getPlayer()));
        }
    }

    private void changeHandler(ISwimAnimationHandler handler){
        if(this.handler!=null) this.handler.stop();
        this.handler = handler;
    }
}
