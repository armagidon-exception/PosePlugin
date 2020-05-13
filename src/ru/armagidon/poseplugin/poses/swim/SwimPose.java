package ru.armagidon.poseplugin.poses.swim;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.PosePluginPlayer;
import ru.armagidon.poseplugin.poses.EnumPose;
import ru.armagidon.poseplugin.poses.PluginPose;
import ru.armagidon.poseplugin.utils.ConfigurationManager;

import static ru.armagidon.poseplugin.utils.ConfigurationManager.COLLIDABLE;
import static ru.armagidon.poseplugin.utils.VectorUtils.getBlock;

public class SwimPose extends PluginPose {

    private ISwimAnimationHandler handler = null;


    public SwimPose(Player player) {
        super(player);
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver, log);
        getPlayer().setCollidable((Boolean) ConfigurationManager.get(COLLIDABLE));
        handler = new CommonSwimHandler(1,getPlayer());
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

    @EventHandler
    public void move(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        if(!event.getPlayer().getName().equals(getPlayer().getName())) return;
        PosePluginPlayer p = getPlayers().get(event.getPlayer().getName());
        if(p.getPoseType().equals(EnumPose.SWIMMING)){
            Block under = getBlock(getPlayer().getLocation().clone().subtract(0,1,0));
            double y = getPlayer().getLocation().getY();
            int blocky = getPlayer().getLocation().getBlockY();

            if(moved(event.getFrom(),event.getTo())) {
                //if player is standing on stairs, use packet handler
                if(Tag.STAIRS.isTagged(under.getType())){
                    handler.stop();
                    handler = new PacketSwimHandler();
                }
                //If y greater than block y and under-block is full-high, place barrier in two blocks above
                else if(blocky<y&&!notFullHighBB(under)){
                    handler.stop();
                    handler = new CommonSwimHandler(2,getPlayer());
                } else {
                    handler.stop();
                    handler = new CommonSwimHandler(1,getPlayer());
                }
                //Use swim handler
                handler.play(getPlayer());
            }
        }
    }

    private boolean notFullHighBB(Block block){
        if(block.getBoundingBox().getHeight()<1&&block.getBoundingBox().getHeight()>0) return true;
        else if(block.getType().equals(Material.SNOW)) return true;
        else if(Tag.SLABS.isTagged(block.getType())) return true;
        return false;
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

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)) return;
        Player pl = (Player) event.getEntity();
        if(!pl.getName().equals(getPlayer().getName())) return;
        if(!containsPlayer(pl)) return;
        PosePluginPlayer p = getPlayers().get(pl.getName());
        if(!p.getPoseType().equals(EnumPose.SWIMMING)) return;
        if(event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
            event.setCancelled(true);
        }
    }

    private boolean moved(Location from, Location to){
        return from.getX()!=to.getX()||from.getZ()!=to.getZ();
    }

}
