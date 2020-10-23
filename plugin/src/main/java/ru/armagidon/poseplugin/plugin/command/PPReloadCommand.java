package ru.armagidon.poseplugin.plugin.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;

public class PPReloadCommand extends PosePluginCommand
{

    public PPReloadCommand() {
        super("ppreload");
    }

    @Override
    protected boolean execute(Player player, String label, String[] args) {
        Bukkit.getPluginManager().disablePlugin(PosePlugin.getInstance());
        PosePlugin.getInstance().reloadConfig();
        PosePlugin.getInstance().message().reload();
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
        Bukkit.getPluginManager().enablePlugin(PosePlugin.getInstance());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8&l[&b&l&nPosePlugin&8&l]&a Plugin reloaded!"));
        return true;
    }
}
