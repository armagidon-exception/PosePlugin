package ru.armagidon.sit.utils.nms.impl;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.armagidon.sit.utils.nms.SwimAnimation;

import static ru.armagidon.sit.utils.Utils.sendPacket;

public class SwimAnimation_1_15 implements SwimAnimation
{

    @Override
    public void play(Player receiver, Player target) {
        EntityPlayer entityPlayer = ((CraftPlayer)target).getHandle();
        DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.set(DataWatcherRegistry.s.a(6), EntityPose.SWIMMING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(),watcher,true);
        sendPacket(receiver,metadata);
    }
}
