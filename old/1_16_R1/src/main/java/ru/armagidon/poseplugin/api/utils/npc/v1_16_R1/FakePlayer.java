package ru.armagidon.poseplugin.api.utils.npc.v1_16_R1;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.npc.HandType;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.*;

public abstract class FakePlayer extends ru.armagidon.poseplugin.api.utils.npc.FakePlayer
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

    /**Packets*/
    private PacketPlayOutBlockChange fakeBedPacket;
    private final PacketPlayOutPlayerInfo addNPC;
    private PacketPlayOutNamedEntitySpawn spawner;
    private final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;
    private final PacketPlayOutEntityDestroy destroy;

    public FakePlayer(Player parent, Pose pose) {
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

        EnumDirection direction = (EnumDirection) getDirection(parent.getLocation().clone().getYaw());

        //Create packet instance of fake bed(could've used sendBlockChange but im crazy and it will recreate copies of the same packet)
        this.fakeBedPacket = new PacketPlayOutBlockChange(fakeBed(direction), (BlockPosition) toBlockPosition(bedLoc));
        //Create packet instance of NPC 's data
        this.addNPC = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake);

        //Set location of NPC
        Location parentLocation = parent.getLocation().clone();
        fake.setPositionRotation(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ(), parentLocation.getYaw(), parentLocation.getPitch());
        //Create instance of npc
        this.spawner = new PacketPlayOutNamedEntitySpawn(fake);

        //Create data watcher to modify entity metadata
        this.watcher = cloneDataWatcher(parent, fake.getProfile());
        //Create instance of the packet with this data
        this.metadataAccessor = new FakePlayerMetadataAccessorImpl(this);
        //Set metadata
        setMetadata();
        this.npcUpdater = new FakePlayerSynchronizerImpl(this);

        this.customEquipmentManager = new CustomEquipmentManagerImpl(this);
        this.destroy  = new PacketPlayOutEntityDestroy(fake.getId());

    }

    //Spawn methods
    /**Main methods*/
    public void broadCastSpawn(){
        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p-> p.getWorld().equals(parent.getWorld()))
                .filter(p-> p.getLocation().distanceSquared(parent.getLocation())<=Math.pow(viewDistance,2)).collect(Collectors.toSet());
        trackers.addAll(detectedPlayers);
        Bukkit.getOnlinePlayers().forEach(receiver-> NMSUtils.sendPacket(receiver, addNPC));
        trackers.forEach(this::spawnToPlayer);
    }

    public void spawnToPlayer(Player receiver){
        NMSUtils.sendPacket(receiver, spawner);
        NMSUtils.sendPacket(receiver, fakeBedPacket);
        customEquipmentManager.showEquipment(receiver);
        metadataAccessor.showPlayer(receiver);
        NMSUtils.sendPacket(receiver, movePacket);
        if(isHeadRotationEnabled()) {
            setHeadRotationEnabled(false);
            PosePluginAPI.getAPI().getTickManager().later(() ->
                    setHeadRotationEnabled(true), 10);
        }
    }

    //Remove methods
    public void removeToPlayer(Player player){
        NMSUtils.sendPacket(player, destroy);
        cache.restore(player);
    }

    private void setMetadata(){
        //Save current overlay bit mask
        byte overlays = ((EntityPlayer)asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(16));
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
            NMSUtils.sendPacket(tracker,fakeBedPacket);
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
            trackers.forEach(p -> NMSUtils.sendPacket(p, animation));
        }
    }

    public void animation(byte id){
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fake, id);
        trackers.forEach(p-> NMSUtils.sendPacket(p,status));
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

        fakeBedPacket = new PacketPlayOutBlockChange(fakeBed((EnumDirection) getDirection(parent.getLocation().getYaw())),
                (BlockPosition) toBlockPosition(bedLoc));

        this.bedLoc.setX(destination.getX());
        this.bedLoc.setY(destination.getY());
        this.bedLoc.setZ(destination.getZ());


        getMetadataAccessor().setBedPosition(bedLoc);

        getMetadataAccessor().merge(true);

        fake.setPosition(destination.getX(), destination.getY(), destination.getZ());

        spawner = new PacketPlayOutNamedEntitySpawn(fake);

        updateNPC();

        getTrackers().forEach(t -> NMSUtils.sendPacket(t, movePacket));

    }

    private DataWatcher cloneDataWatcher(Player parent, GameProfile profile){
        EntityHuman human = new EntityHuman(((CraftPlayer)parent).getHandle().getWorld(), (BlockPosition) toBlockPosition(parent.getLocation()), profile) {
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

    private IBlockAccess fakeBed(EnumDirection direction){
        return new IBlockAccess() {
            @Nullable
            @Override
            public TileEntity getTileEntity(BlockPosition blockPosition) {
                return null;
            }

            @Override
            public IBlockData getType(BlockPosition blockPosition) {
                return Blocks.WHITE_BED.getBlockData().set(BlockBed.PART, BlockPropertyBedPart.HEAD).set(BlockBed.FACING, direction);
            }

            @Override
            public Fluid getFluid(BlockPosition blockPosition) {
                return null;
            }
        };
    }

    private EntityPlayer createNPC(Player parent) {
        CraftWorld world = (CraftWorld) parent.getWorld();
        CraftServer server = (CraftServer) Bukkit.getServer();
        EntityPlayer parentVanilla= (EntityPlayer) NMSUtils.asNMSCopy(parent);
        GameProfile profile = new GameProfile(parent.getUniqueId(), parent.getName());
        profile.getProperties().putAll(parentVanilla.getProfile().getProperties());

        return new EntityPlayer(server.getServer(), world.getHandle(), profile, new PlayerInteractManager(world.getHandle())){

            @Override
            public void sendMessage(IChatBaseComponent ichatbasecomponent, UUID uuid) {}

            @Override
            public void sendMessage(IChatBaseComponent[] iChatBaseComponents) {}

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
}
