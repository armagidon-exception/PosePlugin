package ru.armagidon.poseplugin.api.poses;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;

public interface IPluginPose
{
    PropertyMap getProperties();
    void initiate();
    void play(Player receiver);
    void stop();
    EnumPose getPose();
}
