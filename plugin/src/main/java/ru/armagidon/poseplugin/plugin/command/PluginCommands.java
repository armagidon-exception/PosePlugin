package ru.armagidon.poseplugin.plugin.command;

import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.plugin.configuration.Config;

import java.util.Arrays;

public class PluginCommands
{

    private final PosePluginCommand sit;
    private final PosePluginCommand swim;
    private final PosePluginCommand lay;
    private final PosePluginCommand ppreload;
    private final PosePluginCommand wave;
    private final PosePluginCommand point;
    private final PosePluginCommand handshake;
    private final PosePluginCommand pray;

    public PluginCommands() {
        {
            ppreload = new PPReloadCommand();
            ppreload.setPermission("poseplugin.admin");
        }
        {
            lay = new SimpleCommands("lay");
            swim = new SimpleCommands("swim");
            sit = new SimpleCommands("sit");
            pray = new SimpleCommands("pray");
        }
        {
            wave = new ExperimentalPoseCommand("wave");
            point = new ExperimentalPoseCommand("point");
            handshake = new ExperimentalPoseCommand("handshake");
        }
        {
            {
                handshake.setUsage(PosePlugin.getInstance().messages().getColorized("handshake.usage"));
            }
            {
                point.setUsage(PosePlugin.getInstance().messages().getColorized("point.usage"));
            }
            {
                wave.setUsage(PosePlugin.getInstance().messages().getColorized("wave.usage"));
            }
        }
    }

    public void initCommands(){
        CommandMap map = PosePluginAPI.getAPI().getCoreWrapper().getCommandMap();
        map.register("sit", "poseplugin", sit.getCommand());
        map.register("lay","poseplugin",lay.getCommand());
        map.register("ppreload","poseplugin",ppreload.getCommand());

        Config cfg = PosePlugin.getInstance().getCfg();
        if (cfg.getBoolean("swim.enabled") ) map.register("swim","poseplugin",swim.getCommand());
        if (cfg.getBoolean("x-mode")) {
            if (cfg.getBoolean("wave.enabled")) map.register("wave", "poseplugin", wave.getCommand());
            if (cfg.getBoolean("point.enabled")) map.register("point", "poseplugin", point.getCommand());
            if (cfg.getBoolean("handshake.enabled")) map.register("handshake", "poseplugin", handshake.getCommand());
            if (cfg.getBoolean("pray.enabled")) map.register("pray", "poseplugin", pray.getCommand());
        }
    }

    public void unregisterAll(){
        CommandMap map = PosePluginAPI.getAPI().getCoreWrapper().getCommandMap();
        Arrays.asList(sit,swim,lay,ppreload,wave,point,handshake,pray).forEach(cmd ->
                cmd.getCommand().unregister(map));
    }
}
