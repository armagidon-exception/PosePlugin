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
import ru.armagidon.poseplugin.poses.EnumPose;
import ru.armagidon.poseplugin.utils.BetterChairBridge;
import ru.armagidon.poseplugin.utils.ConfigurationManager;
import ru.armagidon.poseplugin.utils.EventListener;
import ru.armagidon.poseplugin.utils.UpdateChecker;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static ru.armagidon.poseplugin.utils.ConfigurationManager.*;

public class PosePlugin extends JavaPlugin implements Listener
{

    public static BetterChairBridge bridge;
    private Logger logger = Logger.getLogger("PosePlugin");
    private static PosePlugin instance;

    public static PosePlugin getInstance() {
        return instance;
    }
    private static Map<String, PosePluginPlayer> players = new HashMap<>();
    public static UpdateChecker checker;
    @Override
    public void onEnable() {
        instance = this;
        initCommands();
        getServer().getPluginManager().registerEvents(new EventListener(players),this);
        saveDefaultConfig();
        initBridge();
        new ConfigurationManager();
        if((Boolean) get(CHECK_FOR_UPDATED)) {
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            PosePluginPlayer p = players.get(sender.getName());
            if(!onGround(p.getPlayer())){
                sender.sendMessage((String) get(IN_AIR));
                return true;
            }
            if (label.equalsIgnoreCase("sit")) {
                p.changePose(EnumPose.SITTING);
                return true;
            } else if (label.equalsIgnoreCase("lay")) {
                p.changePose(EnumPose.LYING);
                return true;
            } else if(label.equalsIgnoreCase("swim")){
                if((Boolean) get(SWIM_ENABLED)){
                    p.changePose(EnumPose.SWIMMING);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        players.forEach((s,p)-> p.getPose().stop(false));
        Bukkit.getOnlinePlayers().forEach(p-> Bukkit.getOnlinePlayers().forEach(a-> p.showPlayer(this,a)));
    }

    public boolean onGround(Player player){
        Location location = player.getLocation();
        return !location.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)&&player.isOnGround();
    }

    private void initBridge(){
        if(getServer().getPluginManager().getPlugin("BetterChair")!=null) bridge = new BetterChairBridge(PosePlugin.getInstance());
        System.out.println("RUNNING "+NMSUtils.SpigotVersion.currentVersion().name()+" NMS");
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

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
