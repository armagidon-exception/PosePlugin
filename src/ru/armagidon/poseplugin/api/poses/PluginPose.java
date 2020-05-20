package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalListener;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;

public abstract class PluginPose implements IPluginPose, Listener, PersonalListener
{
    private final Player player;
    private Block under;

    public PluginPose(Player target) {
        this.player = target;
        this.under = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.DOWN);
    }

    public Player getPlayer() {
        return player;
    }

    protected PosePluginPlayer getPosePluginPlayer(){
        return PosePlugin.getInstance().getPosePluginPlayer(player.getName());
    }

    public void play(Player receiver, boolean log){
        if(log) getPlayer().sendMessage(getPose().getMessage());
        Bukkit.getPluginManager().registerEvents(this, PosePlugin.getInstance());
    }

    public void stop(boolean log){
        if(log) getPlayer().sendMessage(EnumPose.STANDING.getMessage());
        HandlerList.unregisterAll(this);
        getPosePluginPlayer().setPose(new StandingPose());
    }

    public abstract EnumPose getPose();

    public static void callStopEvent(EnumPose pose, PosePluginPlayer player, boolean log){
        StopAnimationEvent stopevent = new StopAnimationEvent(pose, player, log);
        Bukkit.getPluginManager().callEvent(stopevent);
        if(stopevent.isCancelled()) return;
        player.getPose().stop(stopevent.isLog());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().equals(under)) {
            callStopEvent(getPose(), getPosePluginPlayer(),true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().equals(under)) {
            callStopEvent(getPose(), getPosePluginPlayer(),true);
        }
    }
}
