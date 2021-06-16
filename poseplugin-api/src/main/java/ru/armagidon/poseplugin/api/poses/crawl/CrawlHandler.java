package ru.armagidon.poseplugin.api.poses.crawl;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;


public abstract class CrawlHandler implements Tickable, PersonalListener, Listener
{

    @Getter
    private final Player player;

    private PressingBlock pressingBlock;

    private boolean isOnSlab;

    public CrawlHandler(Player player) {
        this.player = player;
    }

    public void init() {
        PosePluginAPI.getAPI().registerListener(this);
        PosePluginAPI.getAPI().getPersonalHandlerList().subscribe(player, this);
        Location above = BlockPositionUtils.getAbove(player.getLocation());
        setPressingBlock(above, player);
    }

    public void dispose() {
        HandlerList.unregisterAll(this);
        PosePluginAPI.getAPI().getPersonalHandlerList().unsubscribe(player, this);
        pressingBlock.hide();
    }

    @EventHandler
    public final void onJoin(PlayerJoinEvent event) {
        updatePose(event.getPlayer());
    }

    @PersonalEventHandler
    public final void onToggleSneakEvent(PlayerToggleSneakEvent event) {
        Bukkit.getOnlinePlayers().forEach(this::updatePose);
    }

    @EventHandler
    public final void onBlockBreak(BlockBreakEvent event) {
        if (pressingBlock instanceof PressingBlock.BarrierPressingBlock) {
            if (event.getBlock().getLocation().equals(pressingBlock.location)) {
                event.setCancelled(true);
            }
        }
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getZ() != event.getFrom().getZ() || event.getTo().getX() != event.getFrom().getX()) {
            Location above = BlockPositionUtils.getAbove(event.getPlayer().getLocation());
            setPressingBlock(above, event.getPlayer());
            pressingBlock.move(above);
        }
    }

    private void setPressingBlock(Location above, @NotNull Player player) {
        if (isSlabLike(player.getLocation().getY())) { //Standing on a slab
            //Shulker
            if (!isOnSlab) {
                if (pressingBlock != null)
                    pressingBlock.hide();
                pressingBlock = createPressingBlock(above, true);
                if (pressingBlock != null)
                    pressingBlock.show();
                isOnSlab = true;
            }

        } else {
            //Barrier
            if (isOnSlab) {
                if (pressingBlock != null) pressingBlock.hide();
                pressingBlock = createPressingBlock(above, false);
                if (pressingBlock != null) pressingBlock.show();
                isOnSlab = false;
            }
        }
    }

    private boolean isSlabLike(double y) {
        int i = (int) y;
        double d = Math.ceil(y);
        return d - i >= 0.5;
    }

    protected abstract void updateBoundingBox();

    protected abstract void updatePose(Player receiver);

    protected abstract PressingBlock createPressingBlock(Location above, boolean isSlab);

    @Override
    public void tick() {
        if (!BlockPositionUtils.getAbove(player.getLocation()).getBlock().getType().isSolid()) {
            updatePose(player);
            updateBoundingBox();
        }
    }
}
