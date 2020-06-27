package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.misc.BlockCache;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.utils.nms.AnimationPlayer;

public class BlockAirModule implements SwimModule {

    private final Player player;
    private BlockCache cache;

    public BlockAirModule(Player player) {
        this.player = player;
        Bukkit.getServer().getPluginManager().registerEvents(this, PosePlugin.getInstance());
        Block above = VectorUtils.getBlock(player.getLocation()).getRelative(BlockFace.UP);
        if(above.getType().isAir()) {
            cache = new BlockCache(above.getType(), above.getBlockData(), above.getLocation());
            player.sendBlockChange(above.getLocation(), Bukkit.createBlockData(Material.BARRIER));
        }
    }

    @Override
    public void action() {
        Bukkit.getOnlinePlayers().forEach(p->AnimationPlayer.play(player, p, Pose.SWIMMING));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(event.getPlayer().equals(player)) {
            Block above = VectorUtils.getBlock(player.getLocation()).getRelative(BlockFace.UP);
            if (cache != null) {
                if(event.getTo()!=null){
                    if(event.getTo().getX()!=event.getFrom().getX()||event.getTo().getZ()!=event.getFrom().getZ()){
                        cache.restore(player);
                        cache = null;
                    }
                }
            }
            if(above.getType().isAir()) {
                cache = new BlockCache(above.getType(), above.getBlockData(), above.getLocation());
                player.sendBlockChange(above.getLocation(), Bukkit.createBlockData(Material.BARRIER));
            }
        }
    }

    @Override
    public void stop() {
        if(cache!=null) cache.restore(player);
        HandlerList.unregisterAll(this);
    }

    @Override
    public SwimModuleType getType() {
        return SwimModuleType.BLOCK_AIR;
    }

    public static boolean test(Player player){
        Block above = VectorUtils.getBlock(player.getLocation()).getRelative(BlockFace.UP);
        return above.getType().isAir();
    }
}
