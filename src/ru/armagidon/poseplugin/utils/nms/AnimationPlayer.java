package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public interface AnimationPlayer
{
    public void play(Player target, Player receiver, Pose pose);
}
