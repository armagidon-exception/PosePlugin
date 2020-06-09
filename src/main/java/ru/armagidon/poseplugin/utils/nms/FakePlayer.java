package ru.armagidon.poseplugin.utils.nms;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.misc.BlockCache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static ru.armagidon.poseplugin.utils.nms.FakePlayerUtils.*;
import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class FakePlayer
{
    private final Player parent;
    private final EntityPlayer fake;
    private final BlockPosition bedPos;
    private final Location parentLocation;
    private final BlockCache cache;
    private final BlockFace face;

    private BukkitTask syncTask;
    private final BukkitTask tickStarter;

    private Slime hitbox;

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
        this.face = BlockFace.valueOf(getDirection(getParent().getLocation().getYaw()).name());

        this.headrotation = headrotation;
        this.invulnerable = invulnerable;
        this.swingHand = swingHand;

        FAKE_PLAYERS.put(parent, this);
        if(getParent().getGameMode().equals(GameMode.SURVIVAL)||getParent().getGameMode().equals(GameMode.ADVENTURE)) {
            if (getParent().getWorld().getPVP() && !invulnerable) {
                hitbox = createHitBox(getParent(), getParent().getLocation());
            }
        }
        //Load hitBox and start ticking
        tickStarter = Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), ()->{
            //Updating
            syncTask = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(), ()-> Bukkit.getOnlinePlayers().forEach(this::tick),0,1);
        }, 10);
        fake.setPositionRotation(parentLocation.getX(), parentLocation.getY(), parentLocation.getZ(),parentLocation.getYaw(), parentLocation.getPitch());
    }

    //How to spawn lying player
    //Create player info
    //Create spawn packet
    //Create metadata packet
    //Create move packet
    //Create remove player info packet

    public void spawnToPlayer(Player receiver) {
        sendPacket(receiver, spawnFakeBedPacket(parent,bedPos, bedBlockAccess(EnumDirection.valueOf(face.name()))));
        DataWatcher watcher = cloneDataWatcher(parent, fake);
        sendPacket(receiver, new PacketPlayOutNamedEntitySpawn(fake));
        fake.setInvisible(true);
        sendPacket(receiver, new PacketPlayOutEntityMetadata(fake.getId(), fake.getDataWatcher(), false));
        layPlayer(receiver,watcher);
        sendPacket(receiver, move(fake.getId()));
        fake.setInvisible(false);
        sendPacket(receiver, new PacketPlayOutEntityMetadata(fake.getId(), fake.getDataWatcher(), false));
    }

    public void broadCastSpawn() {

        PacketPlayOutNamedEntitySpawn spawner = new PacketPlayOutNamedEntitySpawn(fake);

        PacketPlayOutBlockChange fakeBed = spawnFakeBedPacket(parent, bedPos, bedBlockAccess(EnumDirection.valueOf(face.name())));

        DataWatcher watcher = cloneDataWatcher(parent, fake);

        EntityPlayer f = new EntityPlayer(fake.getMinecraftServer(), fake.getWorldServer(), fake.getProfile(), new PlayerInteractManager(fake.getWorldServer())) {
            public void sendMessage(IChatBaseComponent ichatbasecomponent) {
            }


            public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
            }
        };
        f.e(fake.getId());

        NMSUtils.setField("datawatcher", f, watcher, Entity.class);

        f.entitySleep(bedPos);
        PacketPlayOutEntityMetadata layPacket = new PacketPlayOutEntityMetadata(f.getId(), f.getDataWatcher(), false);

        Bukkit.getOnlinePlayers().forEach(receiver -> {
            sendPacket(receiver, fakeBed);
            sendPacket(receiver, spawner);
            fake.setInvisible(true);
            sendPacket(receiver, new PacketPlayOutEntityMetadata(fake.getId(), fake.getDataWatcher(), false));

            sendPacket(receiver, layPacket);

            sendPacket(receiver, move(fake.getId()));
            fake.setInvisible(false);
            sendPacket(receiver, new PacketPlayOutEntityMetadata(fake.getId(), fake.getDataWatcher(), false));
        });

    }

    public void broadCastRemove(){

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fake.getId());
        Bukkit.getOnlinePlayers().forEach(receiver-> {
            sendPacket(receiver, destroy);
            cache.restore(receiver);
        });

        if(!tickStarter.isCancelled()) tickStarter.cancel();
        if(syncTask!=null) syncTask.cancel();
        if(hitbox!=null) hitbox.remove();
        FAKE_PLAYERS.remove(this);

    }

    private void tick(Player receiver) {
        //Send fakeBed
        sendPacket(receiver, spawnFakeBedPacket(parent, bedPos, bedBlockAccess(EnumDirection.valueOf(face.name()))));
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
        if(getParent().getWorld().getPVP()&&!invulnerable) {
            if (hitbox != null) {
                if (!(getParent().getGameMode().equals(GameMode.ADVENTURE) || getParent().getGameMode().equals(GameMode.SURVIVAL))) {
                    hitbox.remove();
                    hitbox = null;
                }
            } else {
                if (getParent().getGameMode().equals(GameMode.ADVENTURE) || getParent().getGameMode().equals(GameMode.SURVIVAL)) {
                    hitbox = createHitBox(getParent(), getParent().getLocation());
                }
            }
        }
        if(hitbox!=null){
            hitbox.setHealth(parent.getHealth());
        }
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

    void swingHand(Player receiver, boolean mainHand) {
        if(swingHand) {
            int animation = mainHand ? 0 : 3;
            PacketPlayOutAnimation ani = new PacketPlayOutAnimation(fake, animation);
            sendPacket(receiver, ani);
        }
    }

    private Player getParent() {
        return parent;
    }

    private void updateEquipment(Player receiver){

        for (EnumItemSlot slot:EnumItemSlot.values()){

            PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(fake.getId(), slot, getEquipmentBySlot(parent.getEquipment(), slot));
            sendPacket(receiver, eq);
        }

    }

    private void look(float YAW, Player receiver) {
        short vel = 0;
        byte yaw = getFixedRotation(YAW);

        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLook = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fake.getId(),
                vel, vel, vel, yaw, getFixedRotation(90), true);

        sendPacket(receiver, moveLook);

    }

    public void handleHitBox(EntityDamageByEntityEvent e) {
        if(!invulnerable&&getParent().getWorld().getPVP()){

            if(e.getEntity().getType().equals(EntityType.SLIME)){

                Slime slime = (Slime) e.getEntity();
                if(slime.getCustomName()!=null&&slime.getCustomName().equalsIgnoreCase(getParent().getUniqueId().toString())){

                    if(getParent().getGameMode().equals(GameMode.SURVIVAL)||getParent().getGameMode().equals(GameMode.ADVENTURE)){
                        getParent().damage(e.getDamage(), e.getDamager());
                        Bukkit.getOnlinePlayers().forEach(o -> {
                            animation(o, (byte)2);
                            if(o==parent) return;
                            o.playSound(getParent().getLocation(), Sound.ENTITY_PLAYER_HURT,1,1);
                        });
                    }
                }
            }
        }
    }
}
