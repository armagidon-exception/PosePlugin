package ru.armagidon.poseplugin.api.utils.npc;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public abstract class FakePlayer implements Tickable, Listener
{

    /**Flags**/
    private boolean created = false;
    private @Getter @Setter boolean headRotationEnabled;
    private @Getter @Setter boolean synchronizationOverlaysEnabled;
    private @Getter @Setter boolean synchronizationEquipmentEnabled;
    private @Getter @Setter boolean swingAnimationEnabled;

    protected @Getter final Player parent;
    protected BlockCache cache;
    protected final Pose pose;
    protected Location bedLoc;
    protected FakePlayerSynchronizer npcUpdater;
    protected @Getter FakePlayerCustomEquipmentManager customEquipmentManager;
    protected @Getter FakePlayerMetadataAccessor metadataAccessor;

    /**Tracking**/
    //All players that tracks this npc
    protected @Getter final Set<Player> trackers = ConcurrentHashMap.newKeySet();
    protected @Getter @Setter int viewDistance = 20;

    protected final Map<Player, FakePlayer> FAKE_PLAYERS = new HashMap<>();
    protected @Getter HandType activeHand = HandType.RIGHT;

    protected FakePlayer(Player parent, Pose pose) {
        this.pose = pose;
        this.parent = parent;
    }

    public abstract void broadCastSpawn();

    public abstract void spawnToPlayer(Player player);

    public final void remove(){
        getTrackers().forEach(this::removeToPlayer);
    }

    public abstract void removeToPlayer(Player player);

    //Initiate method. Uses to initiate entity spawn. Use before spawn.
    public final void initiate() {
        created = true;
        //Add this NPC to NPC Registry
        FAKE_PLAYERS.put(parent,this);
        //Register this NPC object as ticker.
        PosePluginAPI.getAPI().getTickingBundle().addToTickingBundle(getClass(), this);
        //Add all players nearby to trackers list
        trackers.addAll(VectorUtils.getNear(getViewDistance(), parent));
    }

    //Destroy method. Uses to fully delete NPC from server
    public final void destroy() {
        created = false;
        trackers.clear();
        PosePluginAPI.getAPI().getTickingBundle().removeFromTickingBundle(getClass(), this);
        HandlerList.unregisterAll(this);
        FAKE_PLAYERS.remove(this);
    }

    public abstract void animation(byte id);

    public abstract void swingHand(boolean main);

    /**Setters and getters*/
    public final void setInvisible(boolean invisible){
        getMetadataAccessor().setInvisible(invisible);
        getMetadataAccessor().merge(true);
        updateNPC();
    }

    public boolean isInvisible(){
        return getMetadataAccessor().isInvisible();
    }

    public boolean isHandActive(){
        return getMetadataAccessor().isHandActive();
    }

    public abstract void setActiveHand(HandType mode);

    public final void disableHands(){
        getMetadataAccessor().disableHand();
        getMetadataAccessor().merge(true);
        updateNPC();
    }

    public final void updateNPC(){
        if (created)
            broadCastSpawn();
    }

    public abstract void setPosition(double x, double y, double z);

    @SneakyThrows
    public static FakePlayer createNew(Player parent, Pose pose){
        String path = String.format("ru.armagidon.poseplugin.api.utils.npc.%s.FakePlayer", ReflectionTools.nmsVersion());
        return (FakePlayer) Class.forName(path).getDeclaredConstructor(Player.class, Pose.class).newInstance(parent, pose);
    }
}
