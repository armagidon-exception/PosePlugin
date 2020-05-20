package ru.armagidon.poseplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.events.StartAnimationEvent;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventDispatcher;
import ru.armagidon.poseplugin.utils.misc.*;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static ru.armagidon.poseplugin.utils.misc.ConfigurationManager.*;

public class PosePlugin extends JavaPlugin implements Listener
{

    public static BetterChairBridge bridge;
    private Logger logger = Logger.getLogger("PosePlugin");
    private static PosePlugin instance;

    public static PosePlugin getInstance() {
        return instance;
    }
    private Map<String, PosePluginPlayer> players = new HashMap<>();
    public static UpdateChecker checker;
    @Override
    public void onEnable() {
        instance = this;
        //Init commands
        initCommands();
        //Register events
        getServer().getPluginManager().registerEvents(new EventListener(players),this);
        getServer().getPluginManager().registerEvents(new PersonalEventDispatcher(),this);
        //Save config
        saveDefaultConfig();
        //BetterChair bridge
        initBridge();
        //Initialize config
        new ConfigurationManager();
        //Check for updates
        if(getBoolean(CHECK_FOR_UPDATED)) {
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            PosePluginPlayer p = players.get(sender.getName());
            if(!onGround(p.getPlayer())){
                sender.sendMessage(getString(IN_AIR));
                return true;
            }
            EnumPose pose;
            if (command.getName().equalsIgnoreCase("sit"))
                pose = EnumPose.SITTING;
            else if (command.getName().equalsIgnoreCase("lay"))
                pose = EnumPose.LYING;
            else if(command.getName().equalsIgnoreCase("swim")){
                if(getBoolean(SWIM_ENABLED))
                    pose = EnumPose.SWIMMING;
                else {
                    sender.sendMessage(getString(ANIMATION_DISABLED));
                    return true;
                }
            } else return true;
            //Call StartAnimationEvent
            StartAnimationEvent event = new StartAnimationEvent(p,pose);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled())return true;
            p.changePose(event.getPose());
        }
        return true;
    }

    @Override
    public void onDisable() {
        players.forEach((s,p)-> p.getPose().stop(false));
        Bukkit.getOnlinePlayers().forEach(p-> Bukkit.getOnlinePlayers().forEach(a-> p.showPlayer(this,a)));
    }

    private void initBridge(){
        if(getBoolean(SIT_WITHOUT_COMMAND)&&getServer().getPluginManager().getPlugin("BetterChair")!=null) bridge = new BetterChairBridge(this);
        getLogger().info("RUNNING " + NMSUtils.SpigotVersion.currentVersion().name() + " NMS");
    }

    private void initCommands(){

        TabCompleter c = (commandSender, command, s, strings) -> new ArrayList<>();
        PluginCommand sit =getCommand("sit");
        PluginCommand lay =getCommand("lay");
        PluginCommand swim =getCommand("swim");
        sit.setExecutor(this);
        sit.setTabCompleter(c);
        lay.setExecutor(this);
        lay.setTabCompleter(c);
        swim.setExecutor(this);
        swim.setTabCompleter(c);
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
}
