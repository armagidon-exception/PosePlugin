package ru.armagidon.poseplugin.api.utils.nms.npc;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer_v1_16_R1.FakePlayerStaff.*;

public class FakePlayer_v1_16_R1 implements FakePlayer, Listener
{

    /*Scheme
      on startup - initiate - load some data, executes once
      broadcast spawn
      on end - remove npc
      erase all data - destroy
      */

    /**Main data*/
    private @Getter final Player parent;
    private @Getter final EntityPlayer fake;

    /**Flags**/
    private @Getter boolean invisible;
    private @Getter @Setter boolean headRotationEnabled;
    private @Getter @Setter boolean updateOverlaysEnabled;
    private @Getter @Setter boolean updateEquipmentEnabled;
    private @Getter @Setter boolean swingAnimationEnabled;
    private @Getter HandType activeHand;

    /**Data**/
    private final @Getter DataWatcher watcher;
    private final BlockCache cache;
    private final Pose pose;
    private final Location bedLoc;
    private final FakePlayerUpdater npcUpdater;
    private final @Getter CustomEquipmentInterface customEquipmentInterface;
    private final @Getter MetadataAccessor metadataAccessor;

    /**Tracking**/
    //All players that tracks this npc
    private @Getter final Set<Player> trackers = ConcurrentHashMap.newKeySet();
    private @Getter @Setter int viewDistance = 20;

    /**Packets*/
    private final PacketPlayOutBlockChange fakeBedPacket;
    private final PacketPlayOutPlayerInfo addNPC;
    private final PacketPlayOutNamedEntitySpawn spawner;
    private final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;

    FakePlayer_v1_16_R1(Player parent, Pose pose) {
        this.pose = pose;
        this.parent = parent;

        //Create EntityPlayer instance
        this.fake = createNPC(parent);

        //Get Location of fake bed
        this.bedLoc = parent.getLocation().clone().toVector().setY(0).toLocation(parent.getWorld());
        //Cache original type of block
        this.cache = new BlockCache(bedLoc.getBlock().getBlockData(), bedLoc);

        //Create single instances of packet for optimisation purposes. So server won't need to create tons of copies of the same packet.

        //Create instance of move packet to pop up npc a little
        this.movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0,(short)2,(short)0,(byte)0,(byte)0, true);

        EnumDirection direction = getDirection(parent.getLocation().clone().getYaw());

        //Create packet instance of fake bed(could've used sendBlockChange but im crazy and it will recreate copies of the same packet)
        this.fakeBedPacket = new PacketPlayOutBlockChange(fakeBed(direction), toBlockPosition(bedLoc));
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
        this.metadataAccessor = new MetadataAccessorImpl_v1_16_R1(this);
        //Set metadata
        setMetadata();
        this.npcUpdater = new FakePlayerUpdaterImpl_v1_16_R1(this);

        this.customEquipmentInterface = new CustomEquipmentInterfaceImpl_v1_16_R1(this);

    }

    //Initiate method. Uses to initiate entity spawn. Use before spawn.
    @Override
    public void initiate() {
        //Add this NPC to NPC Registry
        FAKE_PLAYERS.put(parent,this);
        //Register this NPC object as ticker.
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
        //Add all players nearby to trackers list
        trackers.addAll(VectorUtils.getNear(getViewDistance(), parent));
    }

    //Destroy method. Uses to fully delete NPC from server
    @Override
    public void destroy() {
        trackers.clear();
        PosePluginAPI.getAPI().getTickManager().removeTickModule(this);
        HandlerList.unregisterAll(this);
        FAKE_PLAYERS.remove(this);
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
        customEquipmentInterface.showEquipment(receiver);
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
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        NMSUtils.sendPacket(player, destroy);
        cache.restore(player);
    }

    public void remove(){
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        trackers.forEach(online->{
            NMSUtils.sendPacket(online, destroy);
            cache.restore(online);
        });
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
        Set<Player> detectedPlayers = VectorUtils.getNear(getViewDistance(), parent);

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

        if(isUpdateEquipmentEnabled()) updateEquipment();
        if(isUpdateOverlaysEnabled()) updateOverlays();
        if(isHeadRotationEnabled()) updateHeadRotation();

        trackers.forEach(tracker->{
            //Send fake bed
            NMSUtils.sendPacket(tracker,fakeBedPacket);
        });
    }

    private void updateOverlays() {
       npcUpdater.updateOverlays();
    }

    private void updateHeadRotation() {
        npcUpdater.updateHeadRotation();
    }

    private void updateEquipment(){
        npcUpdater.updateEquipment();
    }

    public void updateNPC(){
        broadCastSpawn();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        fake.setPosition(x, y, z);
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

    //Meta info
    public void setInvisible(boolean invisible){
        metadataAccessor.setInvisible(invisible);
        metadataAccessor.merge(true);
        updateNPC();
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
    public void disableHands() {
        metadataAccessor.disableHand();
        metadataAccessor.merge(true);
        updateNPC();
    }

    static class FakePlayerStaff {

        static byte getFixedRotation(float var1){
            return (byte) MathHelper.d(var1 * 256.0F / 360.0F);
        }

        static boolean isKthBitSet(int n, int k)
        {
            return  ((n & (1 << (k - 1))) == 1);
        }

        static org.bukkit.inventory.ItemStack getEquipmentBySlot(EntityEquipment e, EnumItemSlot slot){
            org.bukkit.inventory.ItemStack eq;
            switch (slot){
                case HEAD:
                    eq = e.getHelmet();
                    break;
                case CHEST:
                    eq = e.getChestplate();
                    break;
                case LEGS:
                    eq = e.getLeggings();
                    break;
                case FEET:
                    eq = e.getBoots();
                    break;
                case OFFHAND:
                    eq = e.getItemInOffHand();
                    break;
                default:
                    eq = e.getItemInMainHand();
            }
            return eq;
        }

        static DataWatcher cloneDataWatcher(Player parent, GameProfile profile){
            EntityHuman human = new EntityHuman(((CraftPlayer)parent).getHandle().getWorld(),toBlockPosition(parent.getLocation()), profile) {
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

        static IBlockAccess fakeBed(EnumDirection direction){
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

        static float transform(float rawYaw){
            rawYaw = rawYaw < 0.0F ? 360.0F + rawYaw : rawYaw;
            rawYaw = rawYaw % 360.0F;
            return rawYaw;
        }

        static EnumDirection getDirection(float f) {
            f = transform(f);
            EnumDirection a = null;
            if (f >= 315.0F || f <= 45.0F) {
                a = EnumDirection.NORTH;
            }

            if (f >= 45.0F && f <= 135.0F) {
                a = EnumDirection.EAST;
            }

            if (f >= 135.0F && f <= 225.0F) {
                a = EnumDirection.SOUTH;
            }

            if (f >= 225.0F && f <= 315.0F) {
                a = EnumDirection.WEST;
            }

            return a;
        }

        static EntityPlayer createNPC(Player parent) {
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

        static BlockPosition toBlockPosition(Location location){
            return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        static byte setBit(byte input, int k, boolean flag){
            byte output;
            if(flag){
                output = (byte) (input|(1<<k));
            } else {
                output = (byte) (input&~(1<<k));
            }
            return output;
        }
    }
}
