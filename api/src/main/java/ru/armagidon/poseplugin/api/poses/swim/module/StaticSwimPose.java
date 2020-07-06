package ru.armagidon.poseplugin.api.poses.swim.module;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
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
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.*;

public class StaticSwimPose implements SwimModule
{

    private final Player target;
    private BlockCache cache;
    private final Object metadata;

    @SneakyThrows
    public StaticSwimPose(Player target) {
        this.target = target;

        Block above = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.UP);

        setPlayerPose(target, Pose.SWIMMING);
        metadata = createPosePacket(target, true);

        if(above.getType().isAir()) {
            cache = new BlockCache(above.getType(), above.getBlockData(), above.getLocation());
            target.sendBlockChange(above.getLocation(), Bukkit.createBlockData(Material.BARRIER));
        }

        PosePluginAPI.getAPI().registerListener(this);

    }

    @Override
    public void action() {
        Block above = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.UP);
        if (above.getType().isAir()) {
            BlockData barrier = Bukkit.createBlockData(Material.BARRIER);
            target.sendBlockChange(above.getLocation(), barrier);
            Bukkit.getOnlinePlayers().forEach(receiver-> sendPacket(receiver,metadata));
        }
    }

    @Override
    public void stop() {
        if(cache!=null) cache.restore(target);
        NMSUtils.setPlayerPose(target, Pose.SNEAKING);
        Object sneakPacket = createPosePacket(target,false);
        Bukkit.getOnlinePlayers().forEach(receiver-> sendPacket(receiver, sneakPacket));
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
                event.setCancelled(true);
            }
        }
    }
}
