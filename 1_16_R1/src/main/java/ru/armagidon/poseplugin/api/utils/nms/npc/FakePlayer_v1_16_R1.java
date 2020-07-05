package ru.armagidon.poseplugin.api.utils.nms.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.sendPacket;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer_v1_16_R1.FakePlayerStaff.*;

public class FakePlayer_v1_16_R1 implements FakePlayer, Listener
{

    /*Scheme
      on startup - initiate - load some data, executes once
      broadcast spawm
      on end - remove npc
      erase all data - destroy
      */



    /**Main data*/
    private final Player parent;
    private final EntityPlayer fake;

    /**Flags**/
    private @Getter boolean invisible;
    private @Getter @Setter boolean headRotationEnabled;
    private @Getter @Setter boolean updateOverlaysEnabled;
    private @Getter @Setter boolean updateEquipmentEnabled;
    private @Getter @Setter boolean swingAnimationEnabled;

    /**Data**/
    private final DataWatcher watcher;
    private byte pOverlays;
    private final BlockCache cache;
    private final Pose pose;

    /**Tracking**/
    //All players that tracks this npc
    private final Set<Player> trackers = ConcurrentHashMap.newKeySet();
    private @Getter @Setter int viewDistance = 20;

    /**Packets*/
    private final PacketPlayOutBlockChange fakeBedPacket;
    private final PacketPlayOutPlayerInfo addNPC;
    private final PacketPlayOutNamedEntitySpawn spawner;
    private final PacketPlayOutEntityMetadata updateMetadata;
    private final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;
    private final BlockPosition bedPos;

    FakePlayer_v1_16_R1(Player parent, Pose pose) {
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
        setMetadata(watcher);
        this.updateMetadata = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);

    }

    @Override
    public void initiate() {
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
        FAKE_PLAYERS.put(parent,this);
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);

        trackers.addAll(VectorUtils.getNear(getViewDistance(), parent));

    }

    @Override
    public void destroy() {
        trackers.clear();
        PosePluginAPI.getAPI().getTickManager().removeTickModule(this);
        HandlerList.unregisterAll(this);
        FAKE_PLAYERS.remove(this);
    }

    /**Main methods*/
    public void broadCastSpawn(){
        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p-> p.getWorld().equals(parent.getWorld()))
                .filter(p-> p.getLocation().distanceSquared(parent.getLocation())<=Math.pow(viewDistance,2)).collect(Collectors.toSet());
        trackers.addAll(detectedPlayers);
        Bukkit.getOnlinePlayers().forEach(receiver-> NMSUtils.sendPacket(receiver, addNPC));
        trackers.forEach(this::spawnToPlayer);
    }

    public void removeToPlayer(Player player){
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        NMSUtils.sendPacket(player, destroy);
        cache.restore(player);
    }

    public void remove(){
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        Bukkit.getOnlinePlayers().forEach(online->{
            NMSUtils.sendPacket(online, destroy);
            cache.restore(online);
        });
    }

    public void spawnToPlayer(Player receiver){
        NMSUtils.sendPacket(receiver, spawner);
        NMSUtils.sendPacket(receiver, fakeBedPacket);
        NMSUtils.sendPacket(receiver, updateMetadata);
        NMSUtils.sendPacket(receiver, movePacket);
    }

    private void setMetadata(DataWatcher watcher){
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
        Set<Player> detectedPlayers = VectorUtils.getNear(getViewDistance(), parent);

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
        if(isUpdateEquipmentEnabled()) updateEquipment();

        trackers.forEach(p-> NMSUtils.sendPacket(p,fakeBedPacket));
    }

    private void updateOverlays() {
        byte overlays = ((EntityPlayer) NMSUtils.asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays!=pOverlays){
            pOverlays = overlays;
            watcher.set(DataWatcherRegistry.a.a(16),pOverlays);
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);
            trackers.forEach(p-> NMSUtils.sendPacket(p, packet));
        }
    }

    private void tickLook() {
        PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(fake, getFixedRotation(parent.getLocation().getYaw()));
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(parent.getLocation().getYaw()), (byte) 0, true);
        trackers.forEach(p -> {
            NMSUtils.sendPacket(p, lookPacket);
            NMSUtils.sendPacket(p, rotation);
        });
    }

    public void updateEquipment(){
        List<Pair<EnumItemSlot, ItemStack>> slots=
            Arrays.stream(EnumItemSlot.values()).map(slot->Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(parent.getEquipment(), slot)))).collect(Collectors.toList());
        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(fake.getId(), slots);
        trackers.forEach(r->sendPacket(r,eq));
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
        if(this.invisible!=invisible) {
            byte value = ((EntityPlayer) NMSUtils.asNMSCopy(parent)).getDataWatcher().get(DataWatcherRegistry.a.a(0));
            if (invisible) {
                value = (byte) (value | 0x20);
            } else {
                value = (byte) (value & ~(0x20));
            }
            watcher.set(DataWatcherRegistry.a.a(0), value);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);
            Bukkit.getOnlinePlayers().forEach(p -> NMSUtils.sendPacket(p, metadata));
            this.invisible = invisible;
        }
    }

    static class FakePlayerStaff{

        static byte getFixedRotation(float var1){
            return (byte) MathHelper.d(var1 * 256.0F / 360.0F);
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

        static BlockPosition toBlockPosition(Location location){
            return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
    }
}
