package ru.armagidon.poseplugin.utils.nms.interfaces;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import java.util.HashSet;
import java.util.Set;

public interface FakePlayer
{

    Set<FakePlayer> FAKE_PLAYERS = new HashSet<>();

    void spawn(Player receiver);
    void remove(Player receiver);
    void rotateHead(Player receiver, float pitch, float yaw);
    void changeEquipment(Player receiver, EntityEquipment equipment);
    void animation(Player receiver, byte id);
    void swingHand(Player receiver, boolean mainHand);
    int getId();
    Player getParent();
    BlockFace getFace();
}
