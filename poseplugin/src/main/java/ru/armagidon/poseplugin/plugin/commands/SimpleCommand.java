package ru.armagidon.poseplugin.plugin.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleCommand
{
    private final PluginCommand command;

    private final Map<String, Executor> subCommands = new HashMap<>();

    private Executor executor;

    @SneakyThrows
    private SimpleCommand(String name) {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        this.command = constructor.newInstance(name, PosePlugin.getInstance());

        command.setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (subCommands.size() == 0) {
                    return executor.execute(player,label, args);
                } else {
                    if (args.length == 0) {
                        return false;
                    } else {
                        Optional<Executor> actionOptional = Optional.ofNullable(subCommands.get(args[0].toLowerCase(Locale.ROOT)));
                        actionOptional.ifPresent(action -> action.execute(player, label, Arrays.copyOfRange(args, 1, args.length)));
                        return actionOptional.isPresent();
                    }
                }
            } else {
                return false;
            }
        });

        command.setTabCompleter((sender, command, label, args) ->
                subCommands.keySet().stream().filter(sub -> sub.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList()));


    }

    public void permission(String permission) {
        if (Bukkit.getPluginManager().getPermission(permission) == null) {
            Bukkit.getPluginManager().addPermission(new Permission(permission));
        }
        command.setPermission(permission);
    }

    public void permissionMessage(String permissionMessage) {
        command.setPermissionMessage(permissionMessage);
    }

    public void usage(String usage) {
        command.setUsage(ChatColor.translateAlternateColorCodes('&', usage));
    }

    public void executor(Executor executor) {
        this.executor = executor;
    }

    private void putSubCommands(Map<String, Executor> sub) {
        subCommands.putAll(sub);
    }

    public PluginCommand getCommand() {
        return command;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }


    @Getter
    @AllArgsConstructor
    private static class CommandData
    {
        private final String[] args;
        private final Player player;
    }

    @FunctionalInterface
    public interface Executor {
        Executor EMPTY = (sender, label, args) -> false;
        boolean execute(Player sender, String label, String[] args);
    }

    @FunctionalInterface
    public interface TabCompleter {
        List<String> complete(Player player, String[] args);
    }


    public static class Builder {

        private String permission, permissionMessage, usage;
        private Executor executor = Executor.EMPTY;

        private final Map<String, Executor> subCommands = new HashMap<>();

        private final String name;

        public Builder(String name) {
            this.name = name;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder permissionMessage(String permissionMessage) {
            this.permissionMessage = permissionMessage;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder subCommand(String sub, Executor executor) {
            subCommands.put(sub, executor);
            return this;
        }

        public void registerIf(Predicate<String> predicate) {
            if (predicate.test(name)) {
                register();
            }
        }

        public void register() {
            SimpleCommand command = new SimpleCommand(name);
            command.putSubCommands(subCommands);

            command.permission(permission);
            command.permissionMessage(permissionMessage);
            command.usage(usage);
            command.executor(executor);

            CommandMap commandMap = PosePluginAPI.getAPI().getCoreWrapper().getCommandMap();
            commandMap.register(name, "poseplugin", command.getCommand());

        }

    }
}
