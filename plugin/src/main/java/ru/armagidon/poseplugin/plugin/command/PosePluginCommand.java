package ru.armagidon.poseplugin.plugin.command;

import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.PosePlugin;

import java.lang.reflect.Constructor;
import java.util.List;

public abstract class PosePluginCommand
{

    private final PluginCommand command;

    @SneakyThrows
    public PosePluginCommand(@NotNull String name) {

        Constructor<PluginCommand> pluginCMDConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        pluginCMDConstructor.setAccessible(true);
        command = pluginCMDConstructor.newInstance(name, PosePlugin.getInstance());
        command.setExecutor((sender, command, label, args) -> {
            if(!(sender instanceof Player)) return true;
            return execute((Player) sender,label, args);
        });
        command.setTabCompleter((sender, command, alias, args) -> {
            if(sender instanceof Player) return tabComplete(((Player) sender),alias, args);
            return ImmutableList.of();
        });

        setPermission("poseplugin.command." + name.toLowerCase());
        setPermissionMessage("§cYou can't do this for some reason! Sorry :(");
    }

    protected abstract boolean execute(Player player,String label,String[] args);

    protected List<String> tabComplete(Player player,String alias, String[] args) {return ImmutableList.of();}

    public void setPermission(String permission){
        command.setPermission(permission);
    }

    public void setPermissionMessage(String permissionMessage){
        command.setPermissionMessage(permissionMessage);
    }

    public void setUsage(String usage){
        command.setUsage(usage);
    }

    public PluginCommand getCommand() {
        return command;
    }
}
