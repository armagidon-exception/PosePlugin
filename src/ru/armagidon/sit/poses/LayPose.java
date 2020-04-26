package ru.armagidon.sit.poses;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.utils.VectorUtils;
import ru.armagidon.sit.utils.nms.FakePlayer;
import ru.armagidon.sit.utils.nms.NMSUtils;

import static ru.armagidon.sit.utils.Utils.COLLIDABLE;
import static ru.armagidon.sit.utils.VectorUtils.yawToFace;

public class LayPose extends PluginPose
{
    private final FakePlayer fake;

    public LayPose(Player player) {
        super(player);
        this.fake = NMSUtils.getFakePlayerInstance(player);
    }

    @Override
    public void play(Player receiver,boolean log) {
        super.play(receiver,log);
        getPlayer().setCollidable(COLLIDABLE);
        if(receiver==null){
            VectorUtils.getNear(100,getPlayer()).forEach(this::playAnimation);
        } else {
            playAnimation(receiver);
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        getPlayer().setCollidable(true);
        Bukkit.getOnlinePlayers().forEach(this::stopAnimation);

    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    public void playAnimation(Player receiver) {
        if(receiver.getUniqueId().equals(getPlayer().getUniqueId())) return;
        receiver.hidePlayer(SitPlugin.getInstance(),getPlayer());
        fake.spawn(receiver);
    }

    public void stopAnimation(Player receiver){
        if(receiver.getUniqueId().equals(getPlayer().getUniqueId())) return;
        receiver.showPlayer(SitPlugin.getInstance(),getPlayer());
        fake.remove(receiver);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        if(!event.getPlayer().getName().equalsIgnoreCase(getPlayer().getName())) return;
        SitPluginPlayer p = getPlayers().get(getPlayer().getName());
        if(p.getPoseType().equals(EnumPose.LYING)){
            if(!checkPosition(event.getFrom(),event.getTo())) event.setCancelled(true);
            if(event.getFrom().getYaw()!=event.getTo().getYaw()||event.getFrom().getPitch()!=event.getTo().getPitch()){
                if(constrainYaw()) {
                    VectorUtils.getNear(100, getPlayer()).forEach(near -> fake.rotateHead(near, getPlayer().getLocation().getPitch(), getPlayer().getLocation().getYaw()));
                }
            }
        }
    }

    public boolean checkPosition(Location from, Location to){
        if(from.getX()!=to.getX()||from.getY()!=to.getY()||from.getZ()!=to.getZ()) return false;
        return true;
    }

    public boolean constrainYaw(){
        BlockFace face = fake.getFace();
        BlockFace playerface = yawToFace(getPlayer().getLocation().getYaw());

        if(face.equals(BlockFace.EAST)){
            return playerface.equals(BlockFace.EAST) || playerface.equals(BlockFace.SOUTH) || playerface.equals(BlockFace.NORTH);
        } else if(face.equals(BlockFace.WEST)){
            return playerface.equals(BlockFace.WEST) || playerface.equals(BlockFace.SOUTH) || playerface.equals(BlockFace.NORTH);
        } else if(face.equals(BlockFace.SOUTH)){
            return playerface.equals(BlockFace.WEST) || playerface.equals(BlockFace.SOUTH) || playerface.equals(BlockFace.EAST);
        } else {
            return playerface.equals(BlockFace.WEST) || playerface.equals(BlockFace.NORTH) || playerface.equals(BlockFace.EAST);
        }
    }
}
