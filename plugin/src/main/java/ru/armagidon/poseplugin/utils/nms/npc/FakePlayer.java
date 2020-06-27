package ru.armagidon.poseplugin.utils.nms.npc;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.TickModule;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.utils.nms.HitBox;

import java.util.HashMap;
import java.util.Map;

public interface FakePlayer extends Tickable
{
    Map<Player, FakePlayer> FAKE_PLAYERS = new HashMap<>();



    /**Main methods*/

    void spawnToPlayer(Player player);

    void remove();

    void removeToPlayer(Player player);

    void broadCastSpawn();

    void animation(byte id);

    void swingHand(boolean main);

    TickModule move();

    HitBox getHitBox();

    /**Setters and getters*/
    void setInvisible(boolean invisible);

    boolean isInvisible();

    void setInvulnerable(boolean invulnerable);

    boolean isInvulnerable();

    boolean isHeadRotationEnabled();

    void setHeadRotationEnabled(boolean headRotationEnabled);

    boolean isUpdateOverlaysEnabled();

    void setUpdateOverlaysEnabled(boolean updateOverlaysEnabled);

    void setUpdateEquipmentEnabled(boolean updateEquipmentEnabled);

    boolean isSwingAnimationEnabled();

    void setSwingAnimationEnabled(boolean swingAnimationEnabled);

    int getViewDistance();

    void setViewDistance(int viewDistance);

    void checkGameMode(GameMode newGameMode);
}
