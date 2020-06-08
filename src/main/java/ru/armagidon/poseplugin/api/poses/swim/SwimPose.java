package ru.armagidon.poseplugin.api.poses.swim;

import net.minecraft.server.v1_15_R1.DataWatcherRegistry;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityPose;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.utils.nms.AnimationPlayer;

import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class SwimPose extends PluginPose {

    private final BukkitTask ticker;
    public SwimPose(Player target) {
        super(target);
        Block above = VectorUtils.getBlock(getPlayer().getLocation()).getRelative(BlockFace.UP);
        ticker = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(), ()->{
            if(above.getType().isAir()){
                BlockData barrier = Bukkit.createBlockData(Material.BARRIER);
                getPlayer().sendBlockChange(above.getLocation(), barrier);
            } else {
                getPlayer().sendBlockChange(above.getLocation(), above.getBlockData());
            }

        },0,1);
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver, log);
        if(receiver ==null){
            Bukkit.getOnlinePlayers().forEach(p-> AnimationPlayer.play(getPlayer(), p, Pose.SWIMMING));
        } else {
            AnimationPlayer.play(getPlayer(), receiver, Pose.SWIMMING);
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        Block above = VectorUtils.getBlock(getPlayer().getLocation()).getRelative(BlockFace.UP);
        getPlayer().sendBlockChange(above.getLocation(), above.getBlockData());
        EntityPlayer player = ((CraftPlayer)getPlayer()).getHandle();
        player.getDataWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.CROUCHING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), false);
        Bukkit.getOnlinePlayers().forEach(p-> {
            sendPacket(p, metadata);
        });
        if(!ticker.isCancelled()){
            ticker.cancel();
        }
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIMMING;
    }

    @Override
    public String getSectionName() {
        return "swim";
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent event){
        if(event.getTo().getX()!=event.getFrom().getX()||event.getTo().getZ()!=event.getFrom().getZ()) {
            Location center = VectorUtils.getBlock(getPlayer().getLocation()).getLocation().add(0.5, 0, 0.5);
            event.setCancelled(true);
            if (getPlayer().getLocation().distance(center) > 0.5) {
                Vector v = getPlayer().getLocation().getDirection().clone();
                v.setY(0);
                v.multiply(-1);
                v.divide(new Vector(2, 0, 2));
                getPlayer().setVelocity(v);
            }
        }
    }
}