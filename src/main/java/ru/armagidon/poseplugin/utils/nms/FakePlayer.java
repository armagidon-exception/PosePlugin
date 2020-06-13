package ru.armagidon.poseplugin.utils.nms;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.utils.misc.BlockCache;
import ru.armagidon.poseplugin.utils.misc.ticking.TickModule;
import ru.armagidon.poseplugin.utils.misc.ticking.Tickable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class FakePlayer implements Tickable
{
    public static final Map<Player, FakePlayer> FAKE_PLAYERS = new HashMap<>();
    private final Player parent;
    private final EntityPlayer fake;
    private final Location parentLocation;
    private Slime hitBox = null;

    /**Flags**/
    private boolean invulnerable;
    private boolean invisible;

    /**Data**/
    private final DataWatcher watcher;
    private EnumDirection direction;
    private byte pOverlays;
    private final BlockCache cache;

    /**Packets*/
    private final PacketPlayOutBlockChange fakeBedPacket;
    private final PacketPlayOutPlayerInfo addNPC;
    private final PacketPlayOutNamedEntitySpawn spawner;
    private final PacketPlayOutEntityMetadata updateMetadata;
    private final PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook movePacket;
    private final BlockPosition bedPos;

    public FakePlayer(Player parent) {
        this.parent = parent;
        this.fake = createNPC(parent);
        Location bedLoc = parent.getLocation().clone().toVector().setY(0).toLocation(parent.getWorld());
        this.cache = new BlockCache(bedLoc.getBlock().getType(), bedLoc.getBlock().getBlockData(), bedLoc);
        this.bedPos = new BlockPosition(bedLoc.getBlockX(), bedLoc.getBlockY(), bedLoc.getBlockZ());

        this.movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0,(short)2,(short)0,(byte)0,(byte)0, true);

        this.direction = getDirection(parent.getLocation().clone().getYaw());

        this.fakeBedPacket = new PacketPlayOutBlockChange(fakeBed(this.direction), bedPos);
        this.addNPC = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake);

        parentLocation = parent.getLocation().clone();
        fake.setPositionRotation(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ(), parentLocation.getYaw(), parentLocation.getPitch());
        this.spawner = new PacketPlayOutNamedEntitySpawn(fake);

        this.watcher = cloneDataWatcher();

        //Set skin overlays
        setMetadata(watcher);

        this.updateMetadata = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);
        checkGameMode(parent.getGameMode());

        FakePlayer.FAKE_PLAYERS.put(parent,this);
    }

    /**Main methods*/
    public void broadCastSpawn(){
        Bukkit.getOnlinePlayers().forEach(receiver->{
            sendPacket(receiver, addNPC);
            sendPacket(receiver, spawner);
            sendPacket(receiver, fakeBedPacket);
            sendPacket(receiver, updateMetadata);
            sendPacket(receiver, movePacket);
        });

    }

    public void remove(){
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        Bukkit.getOnlinePlayers().forEach(online->{
            sendPacket(online, destroy);
            cache.restore(online);
        });
        if(hitBox!=null) hitBox.remove();
        FAKE_PLAYERS.remove(this);
    }

    public void spawnToPlayer(Player receiver){
        sendPacket(receiver, addNPC);
        sendPacket(receiver, spawner);
        sendPacket(receiver, fakeBedPacket);
        sendPacket(receiver, updateMetadata);
        sendPacket(receiver, movePacket);
    }

    private void setMetadata(DataWatcher watcher){
        byte overlays = asNMSCopy(parent).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        pOverlays = overlays;
        watcher.set(DataWatcherRegistry.a.a(16), overlays);
        watcher.set(DataWatcherRegistry.m.a(13), Optional.of(bedPos));
        watcher.set(DataWatcherRegistry.s.a(6),EntityPose.SLEEPING);
    }

    /** Tickers **/
    @Override
    public void tick() {
        if(hitBox!=null) hitBox.setHealth(hitBox.getMaxHealth());
        updateEquipment();
        Bukkit.getOnlinePlayers().forEach(p->sendPacket(p,fakeBedPacket));
    }

    private void updateEquipment(){
        for (EnumItemSlot slot:EnumItemSlot.values()){
            org.bukkit.inventory.ItemStack eq = getEquipmentBySlot(parent.getEquipment(), slot);
            PacketPlayOutEntityEquipment eqPacket = new PacketPlayOutEntityEquipment(fake.getId(), slot, CraftItemStack.asNMSCopy(eq));
            Bukkit.getOnlinePlayers().forEach(receiver -> sendPacket(receiver, eqPacket));
        }

    }

    public void updateOverlays(){
        byte overlays = asNMSCopy(parent).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays!=pOverlays){
            pOverlays = overlays;
            watcher.set(DataWatcherRegistry.a.a(16),pOverlays);
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(fake.getId(), watcher, false);
            Bukkit.getOnlinePlayers().forEach(p-> sendPacket(p, packet));
        }
    }

    public TickModule tickLook(){
        float sub;
        switch (direction){
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
        return ()->{
            PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(), (short) 0,(short)0,(short)0, getFixedRotation(parent.getLocation().getYaw()-sub),(byte)0, true);
            Bukkit.getOnlinePlayers().forEach(p->sendPacket(p, lookPacket));
        };

    }


    //Meta info
    public void setInvisible(boolean invisible){
        if(this.invisible!=invisible) {
            byte value = asNMSCopy(parent).getDataWatcher().get(DataWatcherRegistry.a.a(0));
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

    public void setInvulnerable(boolean invulnerable){
        this.invulnerable = invulnerable;
    }


    /**Handle events**/
    public void damageByPlayer(EntityDamageByEntityEvent event){
        if(!invulnerable&&hitBox!=null){
            LivingEntity slime = (LivingEntity) event.getEntity();
            if(slime.equals(hitBox)) {
                if(event.getDamager().equals(parent)) {
                    event.setCancelled(true);
                    return;
                }
                parent.damage(event.getDamage(), event.getDamager());
                Bukkit.getOnlinePlayers().forEach(p->
                        p.playSound(parentLocation, Sound.ENTITY_PLAYER_HURT, 1,1));
                animation((byte)2);
            }
        }
    }

    public void hitWithSpectralArrow(ProjectileHitEvent event){
        Entity hit = event.getHitEntity();
        if(hit==null) return;
        Projectile arrow = event.getEntity();
        if(arrow.getType().equals(EntityType.SPECTRAL_ARROW)){
            SpectralArrow a = (SpectralArrow) arrow;
            if(hit.equals(hitBox)||hit.equals(parent)){
                a.setGlowingTicks(0);
            }
        }
    }

    public void checkGameMode(GameMode mode){
        if(parent.getWorld().getPVP()&&!invulnerable){
            switch (mode){
                case CREATIVE:
                    if(hitBox!=null){
                        hitBox.remove();
                        hitBox = null;
                    }
                    break;
                case SURVIVAL:
                case ADVENTURE:
                    if(hitBox==null){
                        hitBox = createHitBox(parent, parentLocation);
                    }
                    break;
            }
        }
    }

    public void onBurnDamage(EntityDamageEvent event){
        if(hitBox!=null&&event.getEntity().equals(hitBox)){
            switch (event.getCause()){
                case FIRE:
                case FIRE_TICK:
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
        }
    }

    public void onBurn(EntityCombustEvent event){
        if(hitBox!=null&&event.getEntity().equals(hitBox)){
            parent.setFireTicks(event.getDuration()*20);
        }
    }



    /** Some useful staff **/
    private EntityPlayer createNPC(Player parent){
        CraftWorld world = (CraftWorld) parent.getWorld();
        CraftServer server = (CraftServer) Bukkit.getServer();
        EntityPlayer parentVanilla = asNMSCopy(parent);

        GameProfile profile = new GameProfile(parent.getUniqueId(), parent.getName());
        profile.getProperties().putAll(parentVanilla.getProfile().getProperties());

        return new EntityPlayer(server.getServer(), world.getHandle(), profile, new PlayerInteractManager(world.getHandle())){
            @Override
            public void sendMessage(IChatBaseComponent ichatbasecomponent) {}

            @Override
            public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {}
        };

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

    private static float transform(float rawyaw){
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

    private DataWatcher cloneDataWatcher(){
        EntityHuman human = new EntityHuman(((CraftPlayer)parent).getHandle().getWorld(), fake.getProfile()) {
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

    public static EntityPlayer asNMSCopy(Player player){
        return ((CraftPlayer)player).getHandle();
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
        return eq;
    }

    private byte getFixedRotation(float var1){ return (byte) (var1 * 256.0F / 360.0F); }

    private Slime createHitBox(Player parent, Location location){
        Location spawnLoc = location.clone();
        if(location.getWorld()==null) return null;
        return location.getWorld().spawn(spawnLoc,Slime.class,(slime)->{
            slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            slime.setGravity(false);
            slime.setInvulnerable(true);
            slime.setAI(false);
            slime.setSize(1);
            AttributeInstance slimeAttribute = slime.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            AttributeInstance playerAttribute = parent.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if(slimeAttribute!=null&&playerAttribute!=null) slimeAttribute.setBaseValue(playerAttribute.getValue());
            slime.setHealth(parent.getHealth());
        });
    }

    public void swingHand(boolean mainHand) {
        PacketPlayOutAnimation animation = new PacketPlayOutAnimation(fake, mainHand?0:3);
        Bukkit.getOnlinePlayers().forEach(p->sendPacket(p,animation));
    }

    public void animation(byte id){
        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fake, id);
        Bukkit.getOnlinePlayers().forEach(p->sendPacket(p,status));
    }

    public boolean isInvisible() {
        return invisible;
    }
}
