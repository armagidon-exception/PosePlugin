package ru.armagidon.poseplugin.api.utils.nms.scoreboard;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@AllArgsConstructor
public abstract class NameTagHider
{
    protected final Plugin plugin;

    public abstract void hideTag(Player player);
    public abstract void showTag(Player player);
}
