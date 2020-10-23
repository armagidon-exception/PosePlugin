package ru.armagidon.poseplugin.plugin.command;

import org.bukkit.command.CommandMap;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.plugin.configuration.ConfigConstants;

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

    public PluginCommands() {
        {
            ppreload = new PPReloadCommand();
            ppreload.setPermission("poseplugin.admin");
        }
        {
            lay = new SimpleCommands("lay");
            swim = new SimpleCommands("swim");
            sit = new SimpleCommands("sit");
        }
        {
            wave = new ExperimentalPoseCommand("wave");
            point = new ExperimentalPoseCommand("point");
            handshake = new ExperimentalPoseCommand("handshake");
        }
        {
            {
                handshake.setUsage(PosePlugin.getInstance().message().getMessage("handshake.usage"));
            }
            {
                point.setUsage(PosePlugin.getInstance().message().getMessage("point.usage"));
            }
            {
                wave.setUsage(PosePlugin.getInstance().message().getMessage("wave.usage"));
            }
        }
    }

    public void initCommands(){
        CommandMap map = PosePluginAPI.getAPI().getCoreWrapper().getCommandMap();
        map.register("lay", "poseplugin", lay.getCommand());
        map.register("sit", "poseplugin", sit.getCommand());
        if(ConfigConstants.isSwimEnabled())
            map.register("swim","poseplugin",swim.getCommand());
        map.register("lay","poseplugin",lay.getCommand());
        map.register("ppreload","poseplugin",ppreload.getCommand());
        if(ConfigConstants.isWaveEnabled()) map.register("wave","poseplugin",wave.getCommand());
        if(ConfigConstants.isPointEnabled()) map.register("point","poseplugin",point.getCommand());
        if(ConfigConstants.isHandShakeEnabled()) map.register("handshake","poseplugin",handshake.getCommand());
    }

    public void unregisterAll(){
        CommandMap map = PosePluginAPI.getAPI().getCoreWrapper().getCommandMap();
        Arrays.asList(sit,swim,lay,ppreload,wave,point,handshake).forEach(cmd-> {
            cmd.getCommand().unregister(map);
        });
    }
}
