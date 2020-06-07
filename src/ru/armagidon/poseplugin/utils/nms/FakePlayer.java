package ru.armagidon.poseplugin.utils.nms;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.misc.BlockCache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class FakePlayer
{
    private final Player parent;
    private final EntityPlayer fake;
    private final BlockPosition bedPos;
    private final Location parentLocation;
    private final BlockCache cache;
    private final IBlockAccess access;
    private final BlockFace face;

    private BukkitTask syncTask;
    private final BukkitTask tickStarter;

    //Config fields
    private boolean swingHand;
    private final boolean invulnerable;
    private final boolean headrotation;

    static Map<Player, FakePlayer> FAKE_PLAYERS = new HashMap<>();

    public FakePlayer(Player parent, boolean headrotation, boolean invulnerable, boolean swingHand) {
        this.parent = parent;
        this.fake = createNPC(parent);
        this.parentLocation= parent.getLocation().clone();

        Location bedLoc = bedLocation(parentLocation);
        this.cache = new BlockCache(bedLoc.getBlock().getType(), bedLoc.getBlock().getBlockData(), bedLoc);
        this.bedPos = new BlockPosition(bedLoc.getX(), bedLoc.getY(),bedLoc.getZ());
        this.access = this.bedBlockAccess();
        this.face = BlockFace.valueOf(getDirection(getParent().getLocation().getYaw()).name());

        this.headrotation = headrotation;
        this.invulnerable = invulnerable;
        this.swingHand = swingHand;

        FAKE_PLAYERS.put(parent, this);

        //Load hitBox and start ticking
        tickStarter = Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), ()->{
            //Updating
            syncTask = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(), ()->{
                Bukkit.getOnlinePlayers().forEach(this::tick);
            },0,1);
        }, 10);
        fake.setPositionRotation(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ(),parentLocation.getYaw(), parentLocation.getPitch());
    }

    //How to spawn lying player
    //Create player info
    //Create spawn packet
    //Create metadata packet
    //Create move packet
    //Create remove player info packet

    public void spawn(Player receiver) {

        sendPacket(receiver, getInfoPacket());
        sendPacket(receiver, spawnFakeBedPacket(bedPos));
        DataWatcher watcher = cloneDataWatcher();
        sendPacket(receiver, new PacketPlayOutNamedEntitySpawn(fake));
        fake.setInvisible(true);
        sendPacket(receiver, new PacketPlayOutEntityMetadata(fake.getId(), fake.getDataWatcher(), false));
        layPlayer(receiver,watcher);
        sendPacket(receiver, move());
        fake.setInvisible(false);
        sendPacket(receiver, new PacketPlayOutEntityMetadata(fake.getId(), fake.getDataWatcher(), false));
    }

    public void remove(Player receiver) {
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        sendPacket(receiver, destroy);
        if(!tickStarter.isCancelled()) tickStarter.cancel();
        if(syncTask!=null) syncTask.cancel();
        cache.restore(receiver);
        FAKE_PLAYERS.remove(this);
    }

    private void tick(Player receiver) {
        //Send fakeBed
        sendPacket(receiver, spawnFakeBedPacket(bedPos));
        //Update armor etc.
        updateEquipment(receiver);
        //Set position for npc
        fake.setPosition(parentLocation.getX(),parentLocation.getY(),parentLocation.getZ());
        //Look
        float sub;
        switch (face){
            case NORTH:
                sub = 0;
                break;
            case SOUTH:
                sub = 180;
                break;
            case WEST:
                sub = -90;
                break;
            default:
                sub = 90;
                break;
        }
            if (headrotation) {
                look(getParent().getLocation().getYaw() - sub, receiver);
            }
    }

    private DataWatcher cloneDataWatcher(){
        EntityHuman human = new EntityHuman(fake.getWorld(), fake.getProfile()) {

            public boolean isSpectator() {
                return false;
            }


            public boolean isCreative() {
                return false;
            }
        };

        human.e(fake.getId());
        EntityPlayer vanillaplayer = ((CraftPlayer) parent).getHandle();
        DataWatcher parentwatcher = vanillaplayer.getDataWatcher();
        byte overlays = parentwatcher.get(DataWatcherRegistry.a.a(16));
        byte arrows = parentwatcher.get(DataWatcherRegistry.a.a(0));
        DataWatcher watcher =human.getDataWatcher();
        watcher.set(DataWatcherRegistry.a.a(16), overlays);
        watcher.set(DataWatcherRegistry.a.a(0),arrows);
        try{
            Field watcherField = Entity.class.getDeclaredField("datawatcher");
            watcherField.setAccessible(true);
            watcherField.set(human, watcher);
        } catch (Exception e){
            e.printStackTrace();
        }
        return human.getDataWatcher();
    }

    private PacketPlayOutBlockChange spawnFakeBedPacket(BlockPosition location){
        return new PacketPlayOutBlockChange(access, location);
    }

    private PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook move(){
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short)0,(short) 2,(short) 0,(byte)0,(byte)0,false);
    }

    private byte getFixedRotation(float var1){ return (byte) (var1 * 256.0F / 360.0F); }

    private IBlockAccess bedBlockAccess() {
        final Float f = parent.getEyeLocation().getYaw();
        return new IBlockAccess() {

            public IBlockData getType(BlockPosition arg0) {
                EnumDirection a = getDirection(f);
                return Blocks.WHITE_BED.getBlockData().set(BlockBed.FACING, a).set(BlockBed.PART, BlockPropertyBedPart.HEAD);
            }

            public TileEntity getTileEntity(BlockPosition arg0) {
                return null;
            }

            public Fluid getFluid(BlockPosition arg0) {
                return null;
            }
        };
    }

    private ItemStack getEquipmentBySlot(EntityEquipment e, EnumItemSlot slot){
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
        return CraftItemStack.asNMSCopy(eq);
    }

    private float transform(float rawyaw){
        rawyaw = rawyaw < 0.0F ? 360.0F + rawyaw : rawyaw;
        rawyaw = rawyaw % 360.0F;
        return rawyaw;
    }

    private EnumDirection getDirection(float f) {
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

    private Location bedLocation(Location location){
        Location l = location.clone();
        l.setY(0);
        return l;
    }

    private GameProfile cloneProfile(Player parent){
        EntityPlayer vanillaplayer = ((CraftPlayer)parent).getHandle();
        GameProfile gameProfile = new GameProfile(parent.getUniqueId(), parent.getName());
        gameProfile.getProperties().putAll(vanillaplayer.getProfile().getProperties());
        return gameProfile;
    }

    private EntityPlayer createNPC(Player parent) {
        EntityPlayer vanillaplayer = ((CraftPlayer) parent).getHandle();
        World world = vanillaplayer.getWorld();
        return new EntityPlayer(Objects.requireNonNull(world.getMinecraftServer()), vanillaplayer.getWorldServer(), cloneProfile(parent), new PlayerInteractManager(vanillaplayer.getWorldServer())) {


            public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
            }


            public void sendMessage(IChatBaseComponent ichatbasecomponent) {
            }


            public boolean isCreative() {
                return false;
            }
        };
    }

    private PacketPlayOutPlayerInfo getInfoPacket(){
        PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake);
        return info;
    }

    private void layPlayer(Player receiver, DataWatcher watcher){
        try {
            EntityPlayer f = new EntityPlayer(fake.getMinecraftServer(), fake.getWorldServer(), fake.getProfile(), new PlayerInteractManager(fake.getWorldServer())) {
                public void sendMessage(IChatBaseComponent ichatbasecomponent) {
                }


                public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
                }
            };
            f.e(fake.getId());
            Field dW = Entity.class.getDeclaredField("datawatcher");
            dW.setAccessible(true);
            dW.set(f,watcher);
            f.entitySleep(bedPos);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(f.getId(), f.getDataWatcher(), false);
            sendPacket(receiver, metadata);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void animation(Player receiver, byte id) {
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fake, id);
        sendPacket(receiver,status);
    }

    public void swingHand(Player receiver, boolean mainHand) {
        if(swingHand) {
            int animation = mainHand ? 0 : 3;
            PacketPlayOutAnimation ani = new PacketPlayOutAnimation(fake, animation);
            sendPacket(receiver, ani);
        }
    }

    public Player getParent() {
        return parent;
    }

    private void updateEquipment(Player receiver){

        for (EnumItemSlot slot:EnumItemSlot.values()){

            PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(fake.getId(), slot, getEquipmentBySlot(parent.getEquipment(), slot));
            sendPacket(receiver, eq);
        }

    }

    public void look(float YAW, Player receiver) {
        short vel = 0;
        byte yaw = getFixedRotation(YAW);

        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(),
                vel, vel, vel, yaw, getFixedRotation(90), true);

        sendPacket(receiver, moveLook);

    }

    public void handleHitBox(EntityDamageByEntityEvent e) {
        if(!invulnerable&&getParent().getWorld().getPVP()){

            if(e.getEntity().getType().equals(EntityType.ARMOR_STAND)){

                ArmorStand stand = (ArmorStand) e.getEntity();
                if(stand.getPassengers().contains(getParent())){

                    if(getParent().getGameMode().equals(GameMode.SURVIVAL)||getParent().getGameMode().equals(GameMode.ADVENTURE)){

                        getParent().damage(e.getDamage(), e.getDamager());

                    }
                }
            }
        }
    }
}
