package ru.armagidon.poseplugin.api.utils.nms.npc;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.Tickable;

import java.util.HashMap;
import java.util.Map;

public interface FakePlayer extends Tickable
{
    Map<Player, FakePlayer> FAKE_PLAYERS = new HashMap<>();

    /**Main methods*/

    void initiate();

    void broadCastSpawn();

    void spawnToPlayer(Player player);

    void remove();

    void removeToPlayer(Player player);

    void destroy();

    void animation(byte id);

    void swingHand(boolean main);

    /**Setters and getters*/
    void setInvisible(boolean invisible);

    boolean isInvisible();

    boolean isHeadRotationEnabled();

    void setHeadRotationEnabled(boolean headRotationEnabled);

    boolean isUpdateOverlaysEnabled();

    void setUpdateOverlaysEnabled(boolean updateOverlaysEnabled);

    void setUpdateEquipmentEnabled(boolean updateEquipmentEnabled);

    boolean isSwingAnimationEnabled();

    void setSwingAnimationEnabled(boolean swingAnimationEnabled);

    int getViewDistance();

    void setViewDistance(int viewDistance);
}
