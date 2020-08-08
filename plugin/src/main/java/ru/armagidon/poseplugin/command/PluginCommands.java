package ru.armagidon.poseplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.config.ConfigConstants;
import ru.armagidon.poseplugin.plugin.messaging.Message;

public class PluginCommands
{

    private final CommandExecutor exExecutor = (sender, c, lbl, args) -> {
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
        EnumPose pose;
        if(c.getName().equalsIgnoreCase("point")){
            pose = EnumPose.POINTING;
        }
        else if(c.getName().equalsIgnoreCase("wave")){
            pose = EnumPose.WAVING;
        } else return true;
        if(p.getPoseType().equals(pose)){
            PluginPose.callStopEvent(p.getPoseType(), p, StopAnimationEvent.StopCause.STOPPED);
            return true;
        }
        if(!VectorUtils.onGround(p.getHandle())){
            PosePlugin.getInstance().message().send(Message.IN_AIR, sender);
            return true;
        }
        p.changePose(pose);
        return true;
    };
    private final CommandExecutor executor = (sender, command, lbl, args)->{
        if(sender instanceof Player) {
            PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
            EnumPose pose;
            if (command.getName().equalsIgnoreCase("sit"))
                pose = EnumPose.SITTING;
            else if (command.getName().equalsIgnoreCase("lay"))
                pose = EnumPose.LYING;
            else if(command.getName().equalsIgnoreCase("swim")) {
                pose = EnumPose.SWIMMING;
            } else {
                return true;
            }
            if(p.getPoseType().equals(pose)){
                PluginPose.callStopEvent(p.getPoseType(), p, StopAnimationEvent.StopCause.STOPPED);
                return true;
            }
            if(!VectorUtils.onGround(p.getHandle())){
                PosePlugin.getInstance().message().send(Message.IN_AIR, sender);
                return true;
            }
            p.changePose(pose);
        }
        return true;
    };
    private final CommandExecutor reloadExecutor = (s,c,l,a)->{
        Bukkit.getPluginManager().disablePlugin(PosePlugin.getInstance());
        PosePlugin.getInstance().reloadConfig();
        Bukkit.getPluginManager().enablePlugin(PosePlugin.getInstance());
        s.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8&l[&b&l&nPosePlugin&8&l]&a Plugin reloaded!"));
        return true;
    };

    private final PosePluginCommand sit;
    private final PosePluginCommand swim;
    private final PosePluginCommand lay;
    private final PosePluginCommand ppreload;
    private final PosePluginCommand wave;
    private final PosePluginCommand point;

    public PluginCommands() {

        point = new PosePluginCommand("point",exExecutor);
        wave = new PosePluginCommand("wave",exExecutor);
        ppreload = new PosePluginCommand("ppreload",reloadExecutor);
        ppreload.setPermission("poseplugin.admin");
        lay = new PosePluginCommand("lay",executor);
        swim = new PosePluginCommand("swim",executor);
        sit = new PosePluginCommand("sit",executor);
    }

    public void initCommands(){
        try{
            CommandMap map = Bukkit.getServer().getCommandMap();
            map.register("sit", "poseplugin", sit);
            if(ConfigConstants.isSwimEnabled()) map.register("swim","poseplugin",swim);
            map.register("lay","poseplugin",lay);
            map.register("ppreload","poseplugin",ppreload);
            if(ConfigConstants.isWaveEnabled()) map.register("wave","poseplugin",wave);
            if(ConfigConstants.isPointEnabled()) map.register("point","poseplugin",point);
        } catch (NoSuchMethodError error){
            PosePlugin.getInstance().getLogger().severe("To use commands use Paper or its forks");
        }
    }
}
