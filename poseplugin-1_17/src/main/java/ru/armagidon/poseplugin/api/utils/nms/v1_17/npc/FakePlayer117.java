package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.toBedLocation;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.toBlockPosition;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.NPCMetadataEditor117.setBit;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.NPCSynchronizer117.getFixedRotation;


@ToolPackage(mcVersion = "1.17")
public class FakePlayer117 extends FakePlayer<SynchedEntityData>
{
    /*Scheme
      on startup - initiate - load some data, executes once
      broadcast spawn
      on end - remove npc
      erase all data - destroy
      */

    private static final String DEEP_DIVE_PACKET_CATCHER = "PPPCatcher";

    //Constants
    public static EntityDataAccessor<Byte> OVERLAYS;
    public static EntityDataAccessor<net.minecraft.world.entity.Pose> POSE;
    public static EntityDataAccessor<Byte> ENTITY_LIVING_TAGS;
    public static EntityDataAccessor<Byte> MAIN_HAND;

    /**Main data*/
    private @Getter final ServerPlayer fake;

    /**Data**/
    private final @Getter SynchedEntityData watcher;
    private final Bed bedData;

    /**Packets*/
    private final ClientboundPlayerInfoPacket addNPCData;
    private final ClientboundPlayerInfoPacket removeNPCData;
    private ClientboundAddPlayerPacket spawner;
    private ClientboundMoveEntityPacket.PosRot movePacket;
    private final ClientboundRemoveEntityPacket destroy;

    public FakePlayer117(Player parent, Pose pose) {
        super(parent, pose);


        //Create EntityPlayer instance
        this.fake = createNPC(parent);

        //Get Location of fake bed
        this.bedLoc = toBedLocation(parent.getLocation());
        //Cache original type of block
        this.cache = new BlockCache(bedLoc.getBlock().getBlockData(), bedLoc);

        //Create single instances of packet for optimisation purposes. So server won't need to create tons of copies of the same packet.

        Direction direction = getDirection(parent.getLocation().clone().getYaw());

        bedData = (Bed) Bukkit.createBlockData(Material.WHITE_BED);
        bedData.setFacing(CraftBlock.notchToBlockFace(direction));
        bedData.setPart(Bed.Part.HEAD);

        //Create packet instance of fake bed(could've used sendBlockChange but im crazy and it will recreate copies of the same packet)
        //Create packet instance of NPC 's data
        this.addNPCData = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, fake);
        this.removeNPCData = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, fake);

        //Set location of NPC
        Location parentLocation = parent.getLocation().clone();
        fake.setPos(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ());
        //Create instance of npc
        this.spawner = new ClientboundAddPlayerPacket(fake);

        //Create data watcher to modify entity metadata
        this.watcher = cloneDataWatcher(parent, fake.getGameProfile());
        //Create instance of the packet with this data
        this.metadataAccessor = new NPCMetadataEditor117(this);
        //Set metadata
        setMetadata();
        this.npcSynchronizer = new NPCSynchronizer117(this);

        this.inventory = new NPCInventory117(this);

        this.destroy = new ClientboundRemoveEntityPacket(fake.getId());

        //Create instance of move packet to pop up npc a little
        this.movePacket = new ClientboundMoveEntityPacket.PosRot(fake.getId(), (short) 0,(short)2,(short)0,(byte)0,(byte)0, true);

    }

    //Spawn methods
    /**Main methods*/
    public void broadCastSpawn(){
        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(parent.getWorld()))
                .filter(p -> p.getLocation().distanceSquared(parent.getLocation()) <= Math.pow(viewDistance, 2)).collect(Collectors.toSet());
        trackers.addAll(detectedPlayers);
        trackers.forEach(this::spawnToPlayer);
        if (isDeepDiveEnabled()) activateDeepDive();
    }

    public void spawnToPlayer(Player receiver){
        sendPacket(receiver, addNPCData);
        sendPacket(receiver, spawner);
        fakeBed(receiver);

        inventory.show(receiver);
        metadataAccessor.showPlayer(receiver);

        sendPacket(receiver, movePacket);
        if(isHeadRotationEnabled()) {
            setHeadRotationEnabled(false);
            PosePluginAPI.getAPI().getTickManager().later(() ->
                    setHeadRotationEnabled(true), 10);
        }
        PosePluginAPI.getAPI().getTickManager().later(() ->
                sendPacket(receiver, removeNPCData), 5);
    }

    //Remove methods
    public void removeToPlayer(Player player){
        sendPacket(player, destroy);
        cache.restore(player);
    }

    @SneakyThrows
    private void setMetadata(){
        //Save current overlay bit mask
        int overlays = ((ServerPlayer) asNMSCopy(parent)).getEntityData().get(OVERLAYS);
        metadataAccessor.setInvisible(parent.hasPotionEffect(PotionEffectType.INVISIBILITY));
        //Set pose to the NPC
        metadataAccessor.setPose(pose);
        //Set current overlays to the NPC
        metadataAccessor.setOverlays((byte) overlays);
        //Set BedLocation to NPC if its pose is SLEEPING
        if(metadataAccessor.getPose().equals(Pose.SLEEPING))
            metadataAccessor.setBedPosition(bedLoc);
        else if (metadataAccessor.getPose().equals(Pose.SPIN_ATTACK)){
            metadataAccessor.setLivingEntityTags(setBit(metadataAccessor.getLivingEntityTags(), 2, true));
        }
        metadataAccessor.merge(true);

    }

    /** Tickers **/
    @Override
    public void tick() {
        //Get players nearby
        Set<Player> detectedPlayers = BlockPositionUtils.getNear(getViewDistance(), parent);

        //Check if some of them aren't trackers
        for (Player detectedPlayer : detectedPlayers) {
            if(!this.trackers.contains(detectedPlayer)){
                spawnToPlayer(detectedPlayer);
                trackers.add(detectedPlayer);
            }
        }
        //Check if some of trackers aren't in view distance
        for (Player tracker : this.trackers) {
            if(!detectedPlayers.contains(tracker)){
                removeToPlayer(tracker);
                trackers.remove(tracker);
            }
        }

        if(isSynchronizationEquipmentEnabled()) npcSynchronizer.syncEquipment();
        if(isSynchronizationOverlaysEnabled()) npcSynchronizer.syncOverlays();
        if(isHeadRotationEnabled()) npcSynchronizer.syncHeadRotation();

        trackers.forEach(this::fakeBed);
    }

    public void swingHand(boolean mainHand) {
        if(isSwingAnimationEnabled()) {
            ClientboundAnimatePacket animation = new ClientboundAnimatePacket(fake, mainHand ? 0 : 3);
            trackers.forEach(p -> sendPacket(p, animation));
        }
    }

    public void animation(byte id){
        ClientboundEntityEventPacket status = new ClientboundEntityEventPacket(fake, id);
        trackers.forEach(p-> sendPacket(p, status));
    }

    @Override
    public boolean isHandActive() {
        return metadataAccessor.isHandActive();
    }

    @Override
    public void setActiveHand(HandType type) {
        metadataAccessor.setActiveHand(type.getHandModeFlag());
        metadataAccessor.merge(true);
        metadataAccessor.update();
        activeHand = type;
    }

    @Override
    public void teleport(Location destination) {
        getTrackers().forEach(t -> cache.restore(t));
        Location bedLoc = toBedLocation(destination);

        cache.setLocation(bedLoc);
        cache.setData(bedLoc.getBlock().getBlockData());

        this.bedLoc.setX(destination.getX());
        this.bedLoc.setY(destination.getY());
        this.bedLoc.setZ(destination.getZ());


        getMetadataAccessor().setBedPosition(bedLoc);

        getMetadataAccessor().merge(true);

        fake.setPos(destination.getX(), destination.getY(), destination.getZ());

        spawner = new ClientboundAddPlayerPacket(fake);

        updateNPC();

        getTrackers().forEach(t -> sendPacket(t, movePacket));

    }

    @Override
    public int getId() {
        return fake.getId();
    }

    @Override
    public SynchedEntityData getDataWatcher() {
        return watcher;
    }

    @Override
    public void setLocationRotation(double x, double y, double z, float pitch, float yaw) {
        fake.setPos(x, y, z);
        fake.setYRot(pitch);
        fake.setXRot(yaw);
        this.movePacket = new ClientboundMoveEntityPacket.PosRot(fake.getId(), (short) 0,(short)2,(short)0, getFixedRotation(yaw), getFixedRotation(pitch), true);
    }

    @Override
    public void setRotation(float pitch, float yaw) {
        fake.setYRot(pitch);
        fake.setXRot(yaw);
        this.movePacket = new ClientboundMoveEntityPacket.PosRot(fake.getId(), (short) 0,(short)2,(short)0, getFixedRotation(yaw), getFixedRotation(pitch), true);
    }

    @Override
    public Location getPosition() {
        return new Location(parent.getWorld(), fake.getX(), fake.getY(), fake.getZ(), fake.getYRot(), fake.getXRot());
    }

    @Override
    protected void activateDeepDive() {
        ServerPlayer vanilla = NMSUtils.asNMSCopy(parent);
        vanilla.connection.connection.channel.pipeline().addBefore("packet_handler", DEEP_DIVE_PACKET_CATCHER, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof ServerboundInteractPacket packet) {

                    Field f = ReflectionTools.getPropertyField(int.class, ServerboundInteractPacket.class);
                    int id = (int) f.get(packet);

                    if (id == vanilla.getId())
                        return;
                }
                super.channelRead(ctx, msg);
            }
        });
        FakePlayer117.sendPacket(parent, new ClientboundSetCameraPacket(fake));
    }

    @Override
    protected void deactivateDeepDive() {
        ServerPlayer vanilla = NMSUtils.asNMSCopy(parent);
        FakePlayer117.sendPacket(parent, new ClientboundSetCameraPacket(vanilla));
        ChannelPipeline pipeline = vanilla.connection.connection.channel.pipeline();
        if (pipeline.get(DEEP_DIVE_PACKET_CATCHER) != null) {
            pipeline.remove(DEEP_DIVE_PACKET_CATCHER);
        }
    }

    private SynchedEntityData cloneDataWatcher(Player parent, GameProfile profile){
        net.minecraft.world.entity.player.Player human = new net.minecraft.world.entity.player.Player(((CraftPlayer)parent).getHandle().getLevel(),
                toBlockPosition(parent.getLocation(), BlockPos.class),0, profile) {
            static {
                FakePlayer117.POSE = DATA_POSE;
                FakePlayer117.OVERLAYS = DATA_PLAYER_MODE_CUSTOMISATION;
                FakePlayer117.MAIN_HAND = DATA_PLAYER_MAIN_HAND;
                FakePlayer117.ENTITY_LIVING_TAGS = DATA_LIVING_ENTITY_FLAGS;
            }

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        return human.getEntityData();
    }

    private void fakeBed(Player tracker){
        tracker.sendBlockChange(cache.getLocation(), bedData);
    }

    private ServerPlayer createNPC(Player parent) {
        CraftWorld world = (CraftWorld) parent.getWorld();
        CraftServer server = (CraftServer) Bukkit.getServer();
        ServerPlayer parentVanilla = asNMSCopy(parent);

        GameProfile profile = new GameProfile(UUID.randomUUID(), parent.getName());
        profile.getProperties().putAll(parentVanilla.getGameProfile().getProperties());

        return new ServerPlayer(server.getServer(), world.getHandle(), profile){
            @Override
            public void sendMessage(Component ichatbasecomponent, UUID uuid) {}
        };
    }
    
    public static void sendPacket(Player receiver, Packet<?> packet) {
        ((CraftPlayer)receiver).getHandle().connection.send(packet);
    }

    @SneakyThrows
    private static Direction getDirection(float angle) {
        return CraftBlock.blockFaceToNotch(BlockPositionUtils.yawToFace(angle).getOppositeFace());
    }

}
