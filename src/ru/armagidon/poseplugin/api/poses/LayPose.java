package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.utils.nms.interfaces.FakePlayer;

import static ru.armagidon.poseplugin.utils.misc.VectorUtils.yawToFace;

public class LayPose extends PluginPose
{
    private final FakePlayer fake;
    private final BukkitTask synctask;

    public LayPose(Player player) {
        super(player);
        this.fake = NMSUtils.getFakePlayerInstance(player);
        synctask = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(), ()-> Bukkit.getOnlinePlayers().forEach(p-> fake.changeEquipment(p,getPlayer().getEquipment())),0,1);
    }

    @Override
    public void play(Player receiver,boolean log) {
        super.play(receiver,log);
        getPlayer().setCollidable(false);
        if(receiver==null){
            Bukkit.getOnlinePlayers().forEach(this::playAnimation);
        } else {
            playAnimation(receiver);
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        getPlayer().setCollidable(true);
        Bukkit.getOnlinePlayers().forEach(this::stopAnimation);
        synctask.cancel();

    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    private void playAnimation(Player receiver) {
        if(receiver.getUniqueId().equals(getPlayer().getUniqueId())) return;
        receiver.hidePlayer(PosePlugin.getInstance(),getPlayer());
        fake.spawn(receiver);
    }

    private void stopAnimation(Player receiver){
        if(receiver.getUniqueId().equals(getPlayer().getUniqueId())) return;
        receiver.showPlayer(PosePlugin.getInstance(),getPlayer());
        fake.remove(receiver);
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!checkPosition(event.getFrom(), event.getTo())) event.setCancelled(true);
        if (event.getFrom().getYaw() != event.getTo().getYaw() || event.getFrom().getPitch() != event.getTo().getPitch()) {
            if (constrainYaw()) {
                Bukkit.getOnlinePlayers().forEach(near -> fake.rotateHead(near, getPlayer().getLocation().getPitch(), getPlayer().getLocation().getYaw()));
            }
        }
    }

    @PersonalEventHandler
    public void onInteract(PlayerInteractEvent event){
        if(event.getHand() == null) return;
        boolean mainhand = event.getHand().equals(EquipmentSlot.HAND);
        Bukkit.getOnlinePlayers().forEach(pl->fake.swingHand(pl,mainhand));
    }

    @PersonalEventHandler
    public void onInteractAt(PlayerInteractAtEntityEvent event){
        boolean mainhand = event.getHand().equals(EquipmentSlot.HAND);
        Bukkit.getOnlinePlayers().forEach(pl->fake.swingHand(pl,mainhand));
    }

    private boolean checkPosition(Location from, Location to){
        return from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ();
    }

    private boolean constrainYaw(){
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
