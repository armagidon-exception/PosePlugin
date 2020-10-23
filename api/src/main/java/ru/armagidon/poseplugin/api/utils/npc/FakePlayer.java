package ru.armagidon.poseplugin.api.utils.npc;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface FakePlayer extends Tickable
{

    Map<Player, FakePlayer> FAKE_PLAYERS = new HashMap<>();

    void initiate();

    void broadCastSpawn();

    void spawnToPlayer(Player player);

    default void remove(){
        getTrackers().forEach(this::removeToPlayer);
    }

    void removeToPlayer(Player player);

    void destroy();

    void animation(byte id);

    void swingHand(boolean main);

    /**Setters and getters*/
    default void setInvisible(boolean invisible){
        getMetadataAccessor().setInvisible(invisible);
        getMetadataAccessor().merge(true);
        updateNPC();
    }

    default boolean isInvisible(){
        return getMetadataAccessor().isInvisible();
    }

    boolean isHeadRotationEnabled();

    void setHeadRotationEnabled(boolean headRotationEnabled);

    boolean isSynchronizationOverlaysEnabled();

    void setSynchronizationOverlaysEnabled(boolean SynchronizationOverlaysEnabled);

    void setSynchronizationEquipmentEnabled(boolean SynchronizationEquipmentEnabled);

    boolean isSwingAnimationEnabled();

    void setSwingAnimationEnabled(boolean swingAnimationEnabled);

    int getViewDistance();

    void setViewDistance(int viewDistance);

    default boolean isHandActive(){
        return getMetadataAccessor().isHandActive();
    }

    HandType getActiveHand();

    void setActiveHand(HandType mode);

    default void disableHands(){
        getMetadataAccessor().disableHand();
        getMetadataAccessor().merge(true);
        updateNPC();
    }

    default void updateNPC(){
        broadCastSpawn();
    }

    FakePlayerCustomEquipmentManager getCustomEquipmentManager();

    FakePlayerMetadataAccessor getMetadataAccessor();

    Set<Player> getTrackers();

    void setPosition(double x, double y, double z);

    boolean isSynchronizationEquipmentEnabled();

    @SneakyThrows
    static FakePlayer createNew(Player parent, Pose pose){
        Constructor<?> constructor = Class.forName("ru.armagidon.poseplugin.api.utils.npc.FakePlayer_" + ReflectionTools.nmsVersion()).getDeclaredConstructor(Player.class, Pose.class);
        return  (FakePlayer) constructor.newInstance(parent, pose);
    }
}
