package ru.armagidon.poseplugin.utils.nms.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;

import static ru.armagidon.poseplugin.utils.nms.NMSUtils.*;

public interface AnimationPlayer
{
        static void play(Player target, Player receiver, Pose pose) {
            try {
                Object nmsplayer = NMSUtils.getHandle(target).invoke(target);
                @SuppressWarnings("unchecked")
                Object nmspose = Enum.valueOf(getEnumClass("EntityPose"),pose.name()); //EntityPose
                Object datawatcher = nmsplayer.getClass().getMethod("getDataWatcher").invoke(nmsplayer); //DataWatcher

                getMethod(datawatcher, "set", getDataWatcherObject().getClass(), Object.class).invoke(datawatcher, getDataWatcherObject(),nmspose);

                int id = (int) nmsplayer.getClass().getMethod("getId").invoke(nmsplayer);
                Object packet = createNMSObject("PacketPlayOutEntityMetadata",new Class[]{int.class,getNmsClass("DataWatcher"),boolean.class}, id, datawatcher, true);
                sendPacket(receiver, packet);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        static Object getDataWatcherObject() throws Exception {
            Object ser = getNmsClass("DataWatcherRegistry").getDeclaredField("s").get(null);
            return getNmsClass("DataWatcherSerializer").getDeclaredMethod("a",int.class).invoke(ser,6);
        }
}
