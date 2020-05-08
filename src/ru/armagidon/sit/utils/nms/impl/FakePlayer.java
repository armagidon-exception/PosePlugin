package ru.armagidon.sit.utils.nms.impl;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public interface FakePlayer
{
    void spawn(Player receiver);
    void remove(Player receiver);
    void rotateHead(Player receiver, float pitch, float yaw);
    BlockFace getFace();
}
