package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
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
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.*;


@ToolPackage(mcVersion = "1.17")
public class FakePlayer117 extends FakePlayer<DataWatcher>
{
    /*Scheme
      on startup - initiate - load some data, executes once
      broadcast spawn
      on end - remove npc
      erase all data - destroy
      */

    /**Main data*/
    private @Getter final EntityPlayer fake;

    /**Data**/
    private final @Getter DataWatcher watcher;
    private final Bed bedData;

    /**Packets*/
    //TODO change it with sendBlockUpdate
    private final PacketPlayOutPlayerInfo addNPC;
    private PacketPlayOutNamedEntitySpawn spawner;
    private final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;
    private final PacketPlayOutEntityDestroy destroy;

    public FakePlayer117(Player parent, Pose pose) {
        super(parent, pose);


        //Create EntityPlayer instance
        this.fake = createNPC(parent);

        //Get Location of fake bed
        this.bedLoc = toBedLocation(parent.getLocation());
        //Cache original type of block
        this.cache = new BlockCache(bedLoc.getBlock().getBlockData(), bedLoc);

        //Create single instances of packet for optimisation purposes. So server won't need to create tons of copies of the same packet.

        //Create instance of move packet to pop up npc a little
        this.movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0,(short)2,(short)0,(byte)0,(byte)0, true);

        EnumDirection direction = getDirection(parent.getLocation().clone().getYaw());

        bedData = (Bed) Bukkit.createBlockData(Material.WHITE_BED);
        bedData.setFacing(CraftBlock.notchToBlockFace(direction));
        bedData.setPart(Bed.Part.HEAD);

        //Create packet instance of fake bed(could've used sendBlockChange but im crazy and it will recreate copies of the same packet)
        //Create packet instance of NPC 's data
        this.addNPC = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, fake);

        //Set location of NPC
        Location parentLocation = parent.getLocation().clone();
        fake.setPositionRotation(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ(), parentLocation.getYaw(), parentLocation.getPitch());
        //Create instance of npc
        this.spawner = new PacketPlayOutNamedEntitySpawn(fake);

        //Create data watcher to modify entity metadata
        this.watcher = cloneDataWatcher(parent, fake.getProfile());
        //Create instance of the packet with this data
        this.metadataAccessor = new NPCMetadataEditor117(this);
        //Set metadata
        setMetadata();
        this.npcUpdater = new NPCSynchronizer117(this);

        this.customEquipmentManager = new NPCInventory117(this);

        this.destroy = new PacketPlayOutEntityDestroy(fake.getId());

    }

    //Spawn methods
    /**Main methods*/
    public void broadCastSpawn(){
        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(parent.getWorld()))
                .filter(p -> p.getLocation().distanceSquared(parent.getLocation()) <= Math.pow(viewDistance,2)).collect(Collectors.toSet());
        trackers.addAll(detectedPlayers);
        Bukkit.getOnlinePlayers().forEach(receiver -> sendPacket(receiver, addNPC));
        trackers.forEach(this::spawnToPlayer);
    }

    public void spawnToPlayer(Player receiver){
        sendPacket(receiver, spawner);
        fakeBed();
        customEquipmentManager.showEquipment(receiver);
        metadataAccessor.showPlayer(receiver);
        sendPacket(receiver, movePacket);
        if(isHeadRotationEnabled()) {
            setHeadRotationEnabled(false);
            PosePluginAPI.getAPI().getTickManager().later(() ->
                    setHeadRotationEnabled(true), 10);
        }
    }

    //Remove methods
    public void removeToPlayer(Player player){
        sendPacket(player, destroy);
        cache.restore(player);
    }

    private void setMetadata(){
        //Save current overlay bit mask
        byte overlays = ((EntityPlayer) asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        //Set pose to the NPC
        metadataAccessor.setPose(pose);
        //Set current overlays to the NPC
        metadataAccessor.setOverlays(overlays);
        //Set BedLocation to NPC if its pose is SLEEPING
        if(metadataAccessor.getPose().equals(Pose.SLEEPING))
            metadataAccessor.setBedPosition(bedLoc);
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

        if(isSynchronizationEquipmentEnabled()) updateEquipment();
        if(isSynchronizationOverlaysEnabled()) updateOverlays();
        if(isHeadRotationEnabled()) updateHeadRotation();

        trackers.forEach(tracker->{
            //Send fake bed
            fakeBed();
        });
    }

    private void updateOverlays() {
        npcUpdater.syncOverlays();
    }

    private void updateHeadRotation() {
        npcUpdater.syncHeadRotation();
    }

    private void updateEquipment(){
        npcUpdater.syncEquipment();
    }

    public void swingHand(boolean mainHand) {
        if(isSwingAnimationEnabled()) {
            PacketPlayOutAnimation animation = new PacketPlayOutAnimation(fake, mainHand ? 0 : 3);
            trackers.forEach(p -> sendPacket(p, animation));
        }
    }

    public void animation(byte id){
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fake, id);
        trackers.forEach(p-> sendPacket(p,status));
    }

    @Override
    public boolean isHandActive() {
        return metadataAccessor.isHandActive();
    }

    @Override
    public void setActiveHand(HandType type) {
        metadataAccessor.setActiveHand(type.getHandModeFlag());
        metadataAccessor.merge(true);
        updateNPC();
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

        fake.setPosition(destination.getX(), destination.getY(), destination.getZ());

        spawner = new PacketPlayOutNamedEntitySpawn(fake);

        updateNPC();

        getTrackers().forEach(t -> sendPacket(t, movePacket));

    }

    @Override
    public int getId() {
        return fake.getId();
    }

    @Override
    public DataWatcher getDataWatcher() {
        return watcher;
    }

    private DataWatcher cloneDataWatcher(Player parent, GameProfile profile){
        EntityHuman human = new EntityHuman(((CraftPlayer)parent).getHandle().getWorld(), (BlockPosition) toBlockPosition(parent.getLocation()),0, profile) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        return human.getDataWatcher();
    }

    private void fakeBed(){
        parent.sendBlockChange(cache.getLocation(), bedData);
    }

    private EntityPlayer createNPC(Player parent) {
        CraftWorld world = (CraftWorld) parent.getWorld();
        CraftServer server = (CraftServer) Bukkit.getServer();
        EntityPlayer parentVanilla= (EntityPlayer) asNMSCopy(parent);
        GameProfile profile = new GameProfile(parent.getUniqueId(), parent.getName());
        profile.getProperties().putAll(parentVanilla.getProfile().getProperties());

        return new EntityPlayer(server.getServer(), world.getHandle(), profile){

            @Override
            public void sendMessage(IChatBaseComponent ichatbasecomponent, UUID uuid) {}

            @Override
            protected void collideNearby() {}

            @Override
            public void collide(Entity entity) {}

            @Override
            public boolean isCollidable() {
                return false;
            }
        };
    }
    
    public static void sendPacket(Player receiver, Packet<?> packet) {
        ((CraftPlayer)receiver).getHandle().b.sendPacket(packet);
    }

    @SneakyThrows
    private static EnumDirection getDirection(float angle) {
        //angle = unsignAngle(angle);

        return CraftBlock.blockFaceToNotch(BlockPositionUtils.yawToFace(angle));

        /*Class ENUM_DIRECTION = ReflectionTools.getEnum("EnumDirection");

        if (angle >= 315.0F || angle <= 45.0F) {
            return EnumDirection.c;
        } else if (angle >= 45.0F && angle <= 135.0F) {
            //a = EnumDirection.EAST;
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "EAST");
        } else if (angle >= 135.0F && angle <= 225.0F) {
            //a = EnumDirection.SOUTH;
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "SOUTH");
        } else if (angle >= 225.0F && angle <= 315.0F) {
            //a = EnumDirection.WEST;
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "WEST");
        } else {
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "NORTH");
        }*/
    }
}
