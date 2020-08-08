package ru.armagidon.poseplugin.command;

import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PosePluginCommand extends Command
{

    private @NotNull final CommandExecutor executor;
    private @NotNull @Setter TabCompleter tabCompleter;

    public PosePluginCommand(@NotNull String name, CommandExecutor executor) {
        super(name);
        this.executor = executor;
        tabCompleter = (s,c,l,a)->new ArrayList<>();
        usageMessage = "";
        setPermission("poseplugin.command."+name.toLowerCase());
        setPermissionMessage("§cYou can't do this for some reason! Sorry :(");
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args) {
        boolean success;
        if(!testPermission(commandSender)) return false;
        success = executor.onCommand(commandSender,this, label, args);
        if (!success && this.usageMessage.length() > 0) {
            String[] var5 = this.usageMessage.replace("<command>", label).split("\n");
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                String line = var5[var7];
                commandSender.sendMessage(line);
            }
        }
        return success;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> tab = tabCompleter.onTabComplete(sender,this,alias,args);
        return tab!=null?tab:new ArrayList<>();
    }
}
