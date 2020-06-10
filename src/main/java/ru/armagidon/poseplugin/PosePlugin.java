package ru.armagidon.poseplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventDispatcher;
import ru.armagidon.poseplugin.utils.misc.Debugger;
import ru.armagidon.poseplugin.utils.misc.EventListener;
import ru.armagidon.poseplugin.utils.misc.PluginLogger;
import ru.armagidon.poseplugin.utils.misc.UpdateChecker;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;
import ru.armagidon.poseplugin.utils.misc.messaging.Messages;
import ru.armagidon.poseplugin.utils.misc.packetManagement.PacketReaderManager;
import ru.armagidon.poseplugin.utils.misc.packetManagement.readers.SwingPacketReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class PosePlugin extends JavaPlugin implements Listener
{
    private static PosePlugin instance;

    public static PosePlugin getInstance() {
        return instance;
    }
    private Map<String, PosePluginPlayer> players = new HashMap<>();
    public static UpdateChecker checker;
    private Messages messages;
    private FileConfiguration config;

    private ServerStatus status;

    private PacketReaderManager manager;

    private Debugger debugger;

    @Override
    public void onEnable() {
        instance = this;
        this.debugger = new Debugger();
        debugger.setEnabled(false);

        status = ServerStatus.ENABLING;
        this.config = getConfig();
        //Load locale files
        try {
            this.messages = new Messages(config.getString("locale", "en"));
        } catch (IllegalArgumentException e){
            Bukkit.getPluginManager().disablePlugin(this);
            getLogger().severe(e.getMessage());
        }

        manager = new PacketReaderManager();
        //Init commands
        initCommands();
        //Register events
        getServer().getPluginManager().registerEvents(new EventListener(players),this);
        getServer().getPluginManager().registerEvents(new PersonalEventDispatcher(),this);
        //Save config
        saveDefaultConfig();
        //Check for updates
        checkForUpdates();
        //Ticking
        tickTask();
        initPacketReaders();
        Bukkit.getOnlinePlayers().forEach(manager::inject);

    }

    private void initPacketReaders(){

        manager.registerPacketReader(new SwingPacketReader());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            PosePluginPlayer p = players.get(sender.getName());
            EnumPose pose;
            if (command.getName().equalsIgnoreCase("sit"))
                pose = EnumPose.SITTING;
            else if (command.getName().equalsIgnoreCase("lay"))
                pose = EnumPose.LYING;
            else if(command.getName().equalsIgnoreCase("swim")){
                if(isSwimEnabled())
                    pose = EnumPose.SWIMMING;
                else {
                    messages.send(Message.ANIMATION_DISABLED, sender);
                    return true;
                }
            } else return true;
            if(p.getPoseType().equals(pose)){
                p.getPose().stop(true);
                return true;
            }
            if(!onGround(p.getPlayer())){
                messages.send(Message.IN_AIR, sender);
                return true;
            }
            p.changePose(pose);
        }
        return true;
    }

    @Override
    public void onDisable() {
        status = ServerStatus.SHUTTING_DOWN;
        players.forEach((s,p)-> p.getPose().stop(false));
        Bukkit.getOnlinePlayers().forEach(manager::eject);
    }

    private void initCommands(){
        TabCompleter c = (commandSender, command, s, strings) -> new ArrayList<>();
        PluginCommand sit =getCommand("sit");
        PluginCommand lay =getCommand("lay");
        PluginCommand swim =getCommand("swim");
        if(sit!=null) {
            sit.setExecutor(this);
            sit.setTabCompleter(c);
        }
        if(lay!=null) {
            lay.setExecutor(this);
            lay.setTabCompleter(c);
        }
        if(swim!=null) {
            swim.setExecutor(this);
            swim.setTabCompleter(c);
        }
    }

    public Debugger getDebugger() {
        return debugger;
    }

    private boolean onGround(Player player){
        Location location = player.getLocation();
        return !location.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)&&player.isOnGround();
    }

    public boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }

    @Override
    public Logger getLogger() {
        return new PluginLogger(this);
    }

    public PosePluginPlayer getPosePluginPlayer(String player) {
        return players.get(player);
    }

    public Messages message(){
        return messages;
    }

    private boolean isSwimEnabled(){
        return config.getBoolean("swim.enabled");
    }

    private void checkForUpdates(){
        if(config.getBoolean("check-for-updates")){
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    public ServerStatus getStatus() {
        return status;
    }

    private void tickTask(){
        Bukkit.getScheduler().runTaskTimer(this,(task)->{
            if(status.equals(ServerStatus.SHUTTING_DOWN)) {
                task.cancel();
            }
            players.values().forEach(player-> {
                if(!player.getPoseType().equals(EnumPose.STANDING)) {
                    player.getPose().tick();
                }
            });

        },0,1);
    }

    public PacketReaderManager getPacketReaderManager() {
        return manager;
    }

    public enum ServerStatus{
        ENABLING,
        SHUTTING_DOWN
    }
}
