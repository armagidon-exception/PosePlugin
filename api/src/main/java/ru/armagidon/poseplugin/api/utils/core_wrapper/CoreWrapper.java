package ru.armagidon.poseplugin.api.utils.core_wrapper;

import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;

public interface CoreWrapper extends Listener
{
    CommandMap getCommandMap();
}
