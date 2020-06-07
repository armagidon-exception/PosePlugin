package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalListener;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;

public abstract class PluginPose implements IPluginPose, Listener, PersonalListener {
    private final Player player;
    private final Block under;
    private final FileConfiguration cfg;

    public PluginPose(Player target) {
        this.player = target;
        this.under = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.DOWN);
        cfg = PosePlugin.getInstance().getConfig();
    }

    public Player getPlayer() {
        return player;
    }

    protected PosePluginPlayer getPosePluginPlayer() {
        return PosePlugin.getInstance().getPosePluginPlayer(player.getName());
    }

    public void play(Player receiver, boolean log) {
        if (log) PosePlugin.getInstance().message().send(getPose().getMessage(), getPlayer());
        Bukkit.getPluginManager().registerEvents(this, PosePlugin.getInstance());
    }

    public void stop(boolean log) {
        if (log) PosePlugin.getInstance().message().send(Message.STAND_UP, getPlayer());
        HandlerList.unregisterAll(this);
        getPosePluginPlayer().setPose(new StandingPose());
    }

    public abstract EnumPose getPose();

    public static void callStopEvent(EnumPose pose, PosePluginPlayer player, boolean log, StopAnimationEvent.StopCause cause) {
        StopAnimationEvent stopEvent = new StopAnimationEvent(pose, player, log, cause);
        Bukkit.getPluginManager().callEvent(stopEvent);
        if (stopEvent.isCancelled()) return;
        player.getPose().stop(stopEvent.isLog());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().equals(under)) {
            callStopEvent(getPose(), getPosePluginPlayer(), true, StopAnimationEvent.StopCause.STOPPED);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().equals(under)) {
            callStopEvent(getPose(), getPosePluginPlayer(), true, StopAnimationEvent.StopCause.STOPPED);
        }
    }


    @PersonalEventHandler
    public void onDamage(EntityDamageEvent event) {
        if (getBoolean("stand-up-when-damaged")) {
            stop(false);
            PosePlugin.getInstance().message().send(getSectionName() + ".damage", getPlayer());
        }
    }

    @PersonalEventHandler
    public void gameMode(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode().equals(GameMode.SPECTATOR))
            callStopEvent(getPose(), getPosePluginPlayer(), true, StopAnimationEvent.StopCause.STOPPED);
    }

    public abstract String getSectionName();

    public boolean getBoolean(String path) {
        return cfg.getBoolean(getSectionName() + "." + path);
    }
}
