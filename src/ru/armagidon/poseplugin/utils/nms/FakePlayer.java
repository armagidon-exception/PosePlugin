package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

public interface FakePlayer
{
    void spawn(Player receiver);
    void remove(Player receiver);
    void rotateHead(Player receiver, float pitch, float yaw);
    void changeEquipment(Player receiver, EntityEquipment equipment);
    BlockFace getFace();
}
