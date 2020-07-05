package ru.armagidon.poseplugin.api.utils.nms.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.sendPacket;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer_v1_15_R1.FakePlayerStaff.*;

public class FakePlayer_v1_15_R1 implements FakePlayer
{

    /**Main data*/
    private final Player parent;
    private final EntityPlayer fake;

    /**Flags**/
    private boolean invisible;
    private boolean headRotationEnabled;
    private boolean updateOverlaysEnabled;
    private boolean updateEquipmentEnabled;
    private boolean swingAnimationEnabled;

    /**Data**/
    private final DataWatcher watcher;
    private byte pOverlays;
    private final BlockCache cache;
    private Pose pose;

    /**Tracking**/
    //All players that tracks this npc
    private final Set<Player> trackers = ConcurrentHashMap.newKeySet();
    private int viewDistance = 20;

    /**Packets*/
    private final PacketPlayOutBlockChange fakeBedPacket;
    private final PacketPlayOutPlayerInfo addNPC;
    private final PacketPlayOutNamedEntitySpawn spawner;
    private final PacketPlayOutEntityMetadata updateMetadata;
    private final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;
    private final BlockPosition bedPos;

    FakePlayer_v1_15_R1(Player parent, Pose pose) {
        this.pose = pose;
        this.parent = parent;
        this.fake = createNPC(parent);
        Location bedLoc = parent.getLocation().clone().toVector().setY(0).toLocation(parent.getWorld());
        this.cache = new BlockCache(bedLoc.getBlock().getType(), bedLoc.getBlock().getBlockData(), bedLoc);
        this.bedPos = new BlockPosition(bedLoc.getBlockX(), bedLoc.getBlockY(), bedLoc.getBlockZ());

        this.movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0,(short)2,(short)0,(byte)0,(byte)0, true);

        EnumDirection direction = getDirection(parent.getLocation().clone().getYaw());

        this.fakeBedPacket = new PacketPlayOutBlockChange(fakeBed(direction), bedPos);
        this.addNPC = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake);

        Location parentLocation = parent.getLocation().clone();
        fake.setPositionRotation(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ(), parentLocation.getYaw(), parentLocation.getPitch());
        this.spawner = new PacketPlayOutNamedEntitySpawn(fake);

        this.watcher = cloneDataWatcher(parent, fake.getProfile());

        this.updateMetadata = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);


    }

    /**Main methods*/

    public void initiate(){
        //Set skin overlays
        setPose(watcher);
        FAKE_PLAYERS.put(parent,this);
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
    }

    public void destroy(){
        FAKE_PLAYERS.remove(this);
        PosePluginAPI.getAPI().getTickManager().removeTickModule(this);
    }

    public void broadCastSpawn(){

        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p-> p.getWorld().equals(parent.getWorld()))
                .filter(p-> p.getLocation().distanceSquared(parent.getLocation())<=Math.pow(viewDistance,2)).collect(Collectors.toSet());
        trackers.addAll(detectedPlayers);
        Bukkit.getOnlinePlayers().forEach(receiver-> sendPacket(receiver, addNPC));
        trackers.forEach(this::spawnToPlayer);

    }

    public void removeToPlayer(Player player){
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        sendPacket(player, destroy);
        cache.restore(player);
    }

    public void remove(){
        trackers.forEach(this::removeToPlayer);
    }

    public void spawnToPlayer(Player receiver){
        sendPacket(receiver, spawner);
        sendPacket(receiver, fakeBedPacket);
        sendPacket(receiver, updateMetadata);
        sendPacket(receiver, movePacket);
    }

    private void setPose(DataWatcher watcher){
        byte overlays = ((EntityPlayer)asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        pOverlays = overlays;
        watcher.set(DataWatcherRegistry.s.a(6),EntityPose.values()[pose.ordinal()]);
        watcher.set(DataWatcherRegistry.a.a(16), overlays);
        if(pose.ordinal()==EntityPose.SLEEPING.ordinal())
            watcher.set(DataWatcherRegistry.m.a(13), Optional.of(bedPos));
    }

    /** Tickers **/
    @Override
    public void tick() {
        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p-> p.getWorld().equals(parent.getWorld())).filter(p-> p.getLocation().distanceSquared(parent.getLocation())<=Math.pow(viewDistance,2)).collect(Collectors.toSet());

        for (Player detectedPlayer : detectedPlayers) {
             if(!this.trackers.contains(detectedPlayer)){
                 trackers.add(detectedPlayer);
                 spawnToPlayer(detectedPlayer);
             }
        }
        for (Player tracker : this.trackers) {
            if(!detectedPlayers.contains(tracker)){
                trackers.remove(tracker);
                removeToPlayer(tracker);
            }
        }

        if(isHeadRotationEnabled()) tickLook();
        if(isUpdateOverlaysEnabled()) updateOverlays();

        trackers.forEach(p->sendPacket(p,fakeBedPacket));
    }

    private void updateOverlays(){
        byte overlays = ((EntityPlayer)asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays!=pOverlays){
            pOverlays = overlays;
            watcher.set(DataWatcherRegistry.a.a(16),pOverlays);
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);
            trackers.forEach(p-> sendPacket(p, packet));
        }
    }

    private void tickLook(){
        PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(fake, getFixedRotation(parent.getLocation().getYaw()));
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0,(short)0,(short)0, getFixedRotation(parent.getLocation().getYaw()),(byte)0, true);
        trackers.forEach(p->{
            sendPacket(p, lookPacket);
            sendPacket(p, rotation);
        });
    }

/*    public TickModule move(){
        return ()-> {
            PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(fake, getFixedRotation(parent.getLocation().getYaw()));
            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short)0, (short)0, (short)0, getFixedRotation(0), getFixedRotation(0), true);
            PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(fake);
            try {
                Field xF = teleport.getClass().getDeclaredField("b");
                xF.setAccessible(true);
                xF.set(teleport, parent.getLocation().getX());
                Field yF = teleport.getClass().getDeclaredField("c");
                yF.setAccessible(true);
                yF.set(teleport, parent.getLocation().getY());
                Field zF = teleport.getClass().getDeclaredField("d");
                zF.setAccessible(true);
                zF.set(teleport, parent.getLocation().getZ());
                Field yawF = teleport.getClass().getDeclaredField("e");
                yawF.setAccessible(true);
                yawF.set(teleport, getFixedRotation(parent.getLocation().getYaw()));
                Field pitchF = teleport.getClass().getDeclaredField("f");
                pitchF.setAccessible(true);
                pitchF.set(teleport, getFixedRotation(parent.getLocation().getPitch()));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            Bukkit.getOnlinePlayers().forEach(p -> {
                sendPacket(p, moveLook);
                sendPacket(p,teleport);
                sendPacket(p, rotation);
            });
        };
    }*/

    //Meta info
    public void setInvisible(boolean invisible){
        if(this.invisible!=invisible) {
            byte value = ((EntityPlayer)asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(0));
            if (invisible) {
                value = (byte) (value | 0x20);
            } else {
                value = (byte) (value & ~(0x20));
            }
            watcher.set(DataWatcherRegistry.a.a(0), value);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);
            Bukkit.getOnlinePlayers().forEach(p -> sendPacket(p, metadata));
            this.invisible = invisible;
        }
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void swingHand(boolean mainHand) {
        if(isSwingAnimationEnabled()) {
            PacketPlayOutAnimation animation = new PacketPlayOutAnimation(fake, mainHand ? 0 : 3);
            trackers.forEach(p -> sendPacket(p, animation));
        }
    }

    public void animation(byte id){
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fake, id);
        trackers.forEach(p->sendPacket(p,status));
    }

    public boolean isHeadRotationEnabled() {
        return headRotationEnabled;
    }

    public void setHeadRotationEnabled(boolean headRotationEnabled) {
        this.headRotationEnabled = headRotationEnabled;
    }

    public boolean isUpdateOverlaysEnabled() {
        return updateOverlaysEnabled;
    }

    public void setUpdateOverlaysEnabled(boolean updateOverlaysEnabled) {
        this.updateOverlaysEnabled = updateOverlaysEnabled;
    }

    public boolean isUpdateEquipmentEnabled() {
        return updateEquipmentEnabled;
    }

    public void setUpdateEquipmentEnabled(boolean updateEquipmentEnabled) {
        this.updateEquipmentEnabled = updateEquipmentEnabled;
    }

    public boolean isSwingAnimationEnabled() {
        return swingAnimationEnabled;
    }

    public void setSwingAnimationEnabled(boolean swingAnimationEnabled) {
        this.swingAnimationEnabled = swingAnimationEnabled;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    static class FakePlayerStaff{

        static byte getFixedRotation(float var1){
            return (byte) MathHelper.d(var1 * 256.0F / 360.0F);
        }

        static ItemStack getEquipmentBySlot(EntityEquipment e, EnumItemSlot slot){
            ItemStack eq;
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
            EntityHuman human = new EntityHuman(((CraftPlayer)parent).getHandle().getWorld(), profile) {
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

        static float transform(float rawyaw){
            rawyaw = rawyaw < 0.0F ? 360.0F + rawyaw : rawyaw;
            rawyaw = rawyaw % 360.0F;
            return rawyaw;
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

        static EntityPlayer createNPC(Player parent){
            CraftWorld world = (CraftWorld) parent.getWorld();
            CraftServer server = (CraftServer) Bukkit.getServer();
            EntityPlayer parentVanilla = (EntityPlayer) asNMSCopy(parent);
            GameProfile profile = new GameProfile(parent.getUniqueId(), parent.getName());
            profile.getProperties().putAll(parentVanilla.getProfile().getProperties());

            return new EntityPlayer(server.getServer(), world.getHandle(), profile, new PlayerInteractManager(world.getHandle())){
                @Override
                public void sendMessage(IChatBaseComponent ichatbasecomponent) {}

                @Override
                public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {}

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
}
