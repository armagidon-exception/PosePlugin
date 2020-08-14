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
import ru.armagidon.poseplugin.api.poses.point.PointPose;
import ru.armagidon.poseplugin.api.poses.reap.ReapPose;
import ru.armagidon.poseplugin.api.poses.wave.WavePose;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.property.Property;
import ru.armagidon.poseplugin.config.ConfigConstants;
import ru.armagidon.poseplugin.plugin.messaging.Message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginCommands
{
    private final CommandExecutor exExecutor = (sender, c, lbl, args) -> {
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
        if(args.length!=1) return false;
        String sub = args[0];
        EnumPose pose;
        if(c.getName().equalsIgnoreCase("wave")){
            pose = EnumPose.WAVING;
        } else if(c.getName().equalsIgnoreCase("point")){
            pose = EnumPose.POINTING;
        } else {
            pose = EnumPose.REAPING;
        }
        Class clazz;
        if(pose.equals(EnumPose.WAVING)){
            clazz= WavePose.WaveMode.class;
        } else if(pose.equals(EnumPose.POINTING)){
            clazz = PointPose.PointMode.class;
        } else {
            clazz = ReapPose.ReapMode.class;
        }
        return checkMode(p, pose, sub, clazz);
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
        }
        return true;
    };
    private final CommandExecutor reloadExecutor = (s,c,l,a)->{
        Bukkit.getPluginManager().disablePlugin(PosePlugin.getInstance());
        PosePlugin.getInstance().reloadConfig();
        PosePlugin.getInstance().message().reload();
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
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
    private final PosePluginCommand reap;

    public PluginCommands() {
        point = new PosePluginCommand("point",exExecutor);
        point.setUsage("§9/point [right/left/off]");
        point.setTabCompleter((s,c,l,a)-> Stream.of("left","right","off").filter(st->st.startsWith(a[0])).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList()));
        wave = new PosePluginCommand("wave",exExecutor);
        wave.setUsage("§9/wave [right/left/off]");
        wave.setTabCompleter((s,c,l,a)-> Stream.of("left","right","off").filter(st->st.startsWith(a[0])).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList()));
        ppreload = new PosePluginCommand("ppreload",reloadExecutor);
        ppreload.setPermission("poseplugin.admin");
        lay = new PosePluginCommand("lay",executor);
        swim = new PosePluginCommand("swim",executor);
        sit = new PosePluginCommand("sit",executor);
        reap = new PosePluginCommand("reap",exExecutor);
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
            if(ConfigConstants.isReapEnabled()) map.register("reap","poseplugin",reap);
        } catch (NoSuchMethodError error){
            PosePlugin.getInstance().getLogger().severe("To use commands use Paper or its forks");
        }
    }

    public void unregisterAll(){
        CommandMap map = Bukkit.getCommandMap();

        Arrays.asList("sit","swim","lay","wave","point","ppreload","reap").forEach(cmd->{
            map.getKnownCommands().remove("poseplugin:"+cmd);
            map.getKnownCommands().remove(cmd);
        });
    }

    private <T extends Enum<T>> boolean checkMode(PosePluginPlayer player, EnumPose pose, String mode, Class<T> clazz){
        List<String> args = Arrays.asList("left", "right","off");
        if(!containsIgnoreCase(mode,args)) return false;
        if(!player.getPoseType().equals(pose)){
            if(mode.equalsIgnoreCase("off")) return true;
            if(!VectorUtils.onGround(player.getHandle())){
                PosePlugin.getInstance().message().send(Message.IN_AIR, player.getHandle());
                return true;
            }
            player.changePose(pose);
        } else {
            if(mode.equalsIgnoreCase("off")) {
                PluginPose.callStopEvent(player.getPoseType(), player, StopAnimationEvent.StopCause.STOPPED);
                return true;
            }
        }
        Property<T> property = player.getPose().getProperties().getProperty("mode",clazz);
        Enum<T> m = Enum.valueOf(clazz, mode.toUpperCase());
        if (!property.getValue().equals(m)) {
            property.setValue((T) m);
        }
        return true;
    }

    private boolean containsIgnoreCase(String s, List<String> list){
        for (String s1 : list) {
            if(s1.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}
