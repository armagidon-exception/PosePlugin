package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

public class StaticSwimPose implements SwimModule
{

    private final Player target;

    public StaticSwimPose(Player target) {
        this.target = target;
        PosePluginAPI.getAPI().registerListener(this);
        Bukkit.getOnlinePlayers().forEach(p -> NMSUtils.setPlayerPose(target, Pose.SWIMMING));
    }

    @Override
    public void action() {
        Block above = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.UP);
        if (above.getType().isAir()) {
            BlockData barrier = Bukkit.createBlockData(Material.BARRIER);
            target.sendBlockChange(above.getLocation(), barrier);
        } else {
            target.sendBlockChange(above.getLocation(), above.getBlockData());
        }
    }

    @Override
    public void stop() {
        Block above = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.UP);
        target.sendBlockChange(above.getLocation(), above.getBlockData());
        Bukkit.getOnlinePlayers().forEach(p -> NMSUtils.setPlayerPose(target,Pose.SNEAKING));
        HandlerList.unregisterAll(this);
    }

    @Override
    public SwimPose.SwimMode getMode() {
        return SwimPose.SwimMode.STATIC;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(!event.getPlayer().equals(target)) return;
        if (event.getTo() != null) {
            if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ()) {
                Location center = VectorUtils.getBlock(target.getLocation()).getLocation().add(0.5, 0, 0.5);
                event.setCancelled(true);
            }
        }
    }
}
