package ru.armagidon.sit.poses;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.utils.VectorUtils;
import ru.armagidon.sit.utils.nms.NMSUtils;

import static org.bukkit.Material.AIR;
import static ru.armagidon.sit.utils.Utils.COLLIDABLE;

public class SwimPose extends PluginPose
{

    private Location cache = null;
    private final BukkitTask task;
    private boolean underblock = false;

    public SwimPose(Player player) {
        super(player);
        task = Bukkit.getScheduler().runTaskTimer(SitPlugin.getInstance(),()-> VectorUtils.getNear(100,getPlayer()).forEach(this::playAnimation),0,1);
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver,log);
        getPlayer().setCollidable(COLLIDABLE);
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        task.cancel();
        if(cache!=null) {
            restoreBlock();
        }
        getPlayer().setCollidable(true);
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIM;
    }

    private void playAnimation(Player near){
        if(near.getUniqueId().equals(getPlayer().getUniqueId())) return;
        NMSUtils.getSwimAnimationExecutor().play(near,getPlayer());
    }


    @EventHandler
    public void move(PlayerMoveEvent event) {
        if(!containsPlayer(event.getPlayer())) return;
        if(!event.getPlayer().getName().equalsIgnoreCase(getPlayer().getName())) return;
        Block above = getPlayer().getLocation().getBlock().getRelative(BlockFace.UP);
        if(cache!=null){
            restoreBlock();
        }
        if(above.getType().equals(AIR)||above.getType().equals(org.bukkit.Material.VOID_AIR)||above.getType().equals(org.bukkit.Material.CAVE_AIR)){
            getPlayer().sendBlockChange(above.getLocation(), Bukkit.createBlockData(Material.BARRIER));
            cache = above.getLocation();
            if(underblock) underblock=false;
        }
        Block lookat = VectorUtils.getDirBlock(getPlayer().getLocation());
        Block abovelookat = lookat.getRelative(BlockFace.UP);
        if((lookat.getType().equals(Material.AIR)||!lookat.getType().isSolid())&&!abovelookat.getType().equals(Material.AIR)&&abovelookat.getType().isSolid()){
            if(!underblock) {
                Location destination =lookat.getLocation().add(.5, 0, .5);
                destination.setYaw(getPlayer().getEyeLocation().getYaw());
                destination.setPitch(getPlayer().getEyeLocation().getPitch());
                getPlayer().teleport(destination);
                underblock=true;
            }
        }
    }

    private void restoreBlock(){
        getPlayer().sendBlockChange(cache,Bukkit.createBlockData(AIR));
        cache = null;
    }
}
