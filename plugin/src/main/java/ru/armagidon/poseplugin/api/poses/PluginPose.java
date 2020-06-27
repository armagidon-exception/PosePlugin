package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.PosePluginPlayer;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.ticking.TickModule;
import ru.armagidon.poseplugin.api.ticking.TickModuleManager;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PluginPose implements IPluginPose, Listener, PersonalListener, TickModuleManager
{
    private final Player player;
    private Block under;
    private final FileConfiguration cfg;
    private Set<TickModule> tickModules = ConcurrentHashMap.newKeySet();

    public static IPluginPose standing = new StandingPose();

    public PluginPose(Player target) {
        this.player = target;
        this.under = VectorUtils.getBlock(target.getLocation()).getRelative(BlockFace.DOWN);
        cfg = PosePlugin.getInstance().getConfig();
    }

    public Player getPlayer() {
        return player;
    }

    protected final PosePluginPlayer getPosePluginPlayer(){
        return PosePlugin.getInstance().getPosePluginPlayer(player.getName());
    }

    public void play(Player receiver, boolean log){
        if(log) PosePlugin.getInstance().message().send(getPose().getMessage(), getPlayer());
        Bukkit.getPluginManager().registerEvents(this, PosePlugin.getInstance());
    }

    public void stop(boolean log){
        if(log) PosePlugin.getInstance().message().send(Message.STAND_UP, getPlayer());
        HandlerList.unregisterAll(this);
        getPosePluginPlayer().setPose(PluginPose.standing);
    }

    public abstract EnumPose getPose();

    //If event was cancelled - return false
    public static boolean callStopEvent(EnumPose pose, PosePluginPlayer player, boolean log, StopAnimationEvent.StopCause cause){
        StopAnimationEvent stopevent = new StopAnimationEvent(pose, player, log, cause);
        Bukkit.getPluginManager().callEvent(stopevent);
        if(stopevent.isCancelled()) return false;
        player.getPose().stop(stopevent.isLog());
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().equals(under)) {
            callStopEvent(getPose(), getPosePluginPlayer(),true, StopAnimationEvent.StopCause.BLOCK_UPDATE);
        }
    }


    @SuppressWarnings("unused")
    @PersonalEventHandler
    public final void onDamage(EntityDamageEvent event){
        if (getBoolean("stand-up-when-damaged")) {
            callStopEvent(getPose(), getPosePluginPlayer(), false, StopAnimationEvent.StopCause.DAMAGE);
            PosePlugin.getInstance().message().send(getSectionName()+".damage",getPlayer());
        }
    }

    @SuppressWarnings("unused")
    @PersonalEventHandler
    public final void gameMode(PlayerGameModeChangeEvent event){

        if(event.getNewGameMode().equals(GameMode.SPECTATOR)){
            callStopEvent(getPose(),getPosePluginPlayer(), true, StopAnimationEvent.StopCause.GAMEMODE_CHANGE);
        }
    }

    @SuppressWarnings("unused")
    @PersonalEventHandler
    public final void onTeleport(PlayerTeleportEvent event){
        Location from = event.getFrom();
        Location to = event.getTo();
        if(to!=null&&to.distanceSquared(from)>1){
            callStopEvent(getPose(), getPosePluginPlayer(),true, StopAnimationEvent.StopCause.TELEPORT);
        }
    }

    public abstract String getSectionName();

    protected final boolean getBoolean(String path){
        return cfg.getBoolean(getSectionName()+"."+path);
    }

    protected final int getInt(String path) {return cfg.getInt(getSectionName()+"."+path);}

    public final void addTickModule(TickModule module){
        if(module!=null){
            tickModules.add(module);
        } else {
            throw new NullPointerException("Tried to use null tick module in "+getClass().getSimpleName());
        }
    }

    public boolean containsTickModule(TickModule m){
        if(m==null) return false;
        return tickModules.contains(m);
    }

    public void removeTickModule(TickModule module){
        if(module!=null&&tickModules.contains(module)){
            tickModules.remove(module);
        } else {
            throw new NullPointerException("Tried to remove non-existing tick module in" + getClass().getSimpleName());
        }
    }

    protected void initTickModules() {}

    @Override
    public final void tick() {
        tickModules.forEach(module -> {
            try{
                module.tick();
            }catch (Exception e){
                PosePlugin.getInstance().getLogger().severe("While ticking error occurred, Ticking module was disabled!");
                e.printStackTrace();
                removeTickModule(module);
            }
        });
    }

    private static class StandingPose implements IPluginPose
    {

        public StandingPose() {}

        @Override
        public void play(Player receiver, boolean log) {}

        @Override
        public void stop(boolean log) {}

        @Override
        public EnumPose getPose() {
            return EnumPose.STANDING;
        }

        @Override
        public void tick() {
            //Standing pose doesn't tick anything
        }
    }
}
