package ru.armagidon.sit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.sit.poses.EnumPose;
import ru.armagidon.sit.utils.BetterChairBridge;
import ru.armagidon.sit.utils.ConfigurationManager;
import ru.armagidon.sit.utils.UpdateChecker;
import ru.armagidon.sit.utils.nms.NMSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ru.armagidon.sit.utils.ConfigurationManager.*;

public class SitPlugin extends JavaPlugin implements Listener
{

    public static BetterChairBridge bridge;

    private static SitPlugin instance;

    public static SitPlugin getInstance() {
        return instance;
    }
    private static Map<String, SitPluginPlayer> players = new HashMap<>();
    public static UpdateChecker checker;
    @Override
    public void onEnable() {
        instance = this;
        new ConfigurationManager();
        getServer().getPluginManager().registerEvents(new ru.armagidon.sit.utils.Listener(players),this);
        TabCompleter c = (commandSender, command, s, strings) -> new ArrayList<>();
        getCommand("sit").setExecutor(this);
        getCommand("sit").setTabCompleter(c);
        getCommand("lay").setExecutor(this);
        getCommand("lay").setTabCompleter(c);
        getCommand("swim").setExecutor(this);
        getCommand("swim").setTabCompleter(c);
        saveDefaultConfig();
        initBridge();
        if((Boolean) get(CHECK_FOR_UPDATED)) {
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            SitPluginPlayer p = players.get(sender.getName());
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
                    p.changePose(EnumPose.SWIM);
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
        if(getServer().getPluginManager().getPlugin("BetterChair")!=null) bridge = new BetterChairBridge(SitPlugin.getInstance());
        System.out.println("RUNNING "+NMSUtils.SpigotVersion.currentVersion().name()+" NMS");
    }
}
