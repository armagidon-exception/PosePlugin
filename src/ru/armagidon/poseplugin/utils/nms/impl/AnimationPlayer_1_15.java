package ru.armagidon.poseplugin.utils.nms.impl;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.utils.nms.AnimationPlayer;

import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class AnimationPlayer_1_15 implements AnimationPlayer
{

    @Override
    public void play(Player target, Player receiver, Pose pose) {
        EntityPlayer player = ((CraftPlayer)target).getHandle();
        EntityPose nmspose = EntityPose.valueOf(pose.name());
        DataWatcher watcher = player.getDataWatcher();
        watcher.set(DataWatcherRegistry.s.a(6),nmspose);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(player.getId(),watcher,true);
        sendPacket(receiver,metadata);
    }
}
