package ru.armagidon.poseplugin.api.poses;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.property.Property;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;

public interface IPluginPose
{
    default <T> Property<T> getProperty(EnumPoseOption<T> option){
        return getProperties().getProperty(option.mapper(), option.getTypeClass());
    }

    default <T> void setProperty(EnumPoseOption<T> option, T value){
        getProperties().getProperty(option.mapper(), option.getTypeClass()).setValue(value);
    }

    PropertyMap getProperties();
    void initiate();
    void play(Player receiver);
    void stop();
    EnumPose getType();
}
