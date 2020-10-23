package ru.armagidon.poseplugin.api.utils.corewrapper;

import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;

public interface CoreWrapper extends Listener
{
    CommandMap getCommandMap();
}
