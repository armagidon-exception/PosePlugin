package ru.armagidon.poseplugin.api.utils.nms;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.Tickable;

public interface PlayerHider extends Tickable
{
    void hide(Player player);
    void show(Player player);
    boolean isHidden(Player player);
}
