package ru.armagidon.poseplugin.api.poses.crawl;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;


public abstract class CrawlHandler implements Tickable, PersonalListener, Listener
{

    @Getter(value = AccessLevel.PROTECTED) private final Player player;

    private PressingBlock pressingBlock;

    public CrawlHandler(Player player) {
        this.player = player;
    }

    public void init() {
        PosePluginAPI.getAPI().registerListener(this);
        PosePluginAPI.getAPI().getPersonalHandlerList().subscribe(player, this);
    }

    public void dispose() {
        HandlerList.unregisterAll(this);
        PosePluginAPI.getAPI().getPersonalHandlerList().unsubscribe(player, this);
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
        handleBlockBreak(event);
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getX() != event.getFrom().getX()
                || event.getTo().getY() != event.getFrom().getY()
                || event.getTo().getZ() != event.getFrom().getZ()) {

        }
    }

    protected abstract void updateBoundingBox();

    protected abstract void updatePose(Player receiver);

    protected abstract void handleBlockBreak(BlockBreakEvent event);

    @Override
    public void tick() {
        if (!BlockPositionUtils.getAbove(player.getLocation()).getBlock().getType().isSolid())
            updateBoundingBox();
    }
}
