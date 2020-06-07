package ru.armagidon.poseplugin.utils.nms;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.Field;

import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public interface AnimationPlayer {
    static void play(Player target, Player receiver, Pose pose) {
        EntityPlayer vanillaplayer = ((CraftPlayer) target).getHandle();
        EntityHuman human = new EntityHuman(vanillaplayer.getWorld(), vanillaplayer.getProfile()) {
            public boolean isSpectator() {
                return false;
            }

            public boolean isCreative() {
                return false;
            }
        };

        human.e(vanillaplayer.getId());
        DataWatcher parentWatcher = vanillaplayer.getDataWatcher();
        byte overlays = parentWatcher.get(DataWatcherRegistry.a.a(16));
        byte arrows = parentWatcher.get(DataWatcherRegistry.a.a(0));
        DataWatcher watcher = human.getDataWatcher();
        watcher.set(DataWatcherRegistry.a.a(16), overlays);
        watcher.set(DataWatcherRegistry.a.a(0), arrows);
        try {
            Field watcherField = Entity.class.getDeclaredField("datawatcher");
            watcherField.setAccessible(true);
            watcherField.set(human, watcher);
        } catch (Exception e) {
            e.printStackTrace();
        }
        human.getDataWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.SWIMMING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(vanillaplayer.getId(), human.getDataWatcher(), false);
        sendPacket(receiver, metadata);
    }
}
