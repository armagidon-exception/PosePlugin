package ru.armagidon.poseplugin.utils.nms;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.PosePlugin;

import java.lang.reflect.Field;
import java.util.Objects;

class FakePlayerUtils
{

    static Slime createHitBox(Player parent, Location location){
        Location spawnLoc = location.clone().multiply(0);
        spawnLoc.setY(2);
        if(location.getWorld()==null) return null;
        Slime s =  location.getWorld().spawn(spawnLoc,Slime.class,(slime)->{
            slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            slime.setGravity(false);
            slime.setAI(false);
            slime.setSize(1);
            slime.setCustomNameVisible(false);
            slime.setCustomName(parent.getUniqueId().toString());
            slime.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(parent.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            slime.setHealth(parent.getHealth());
        });
        Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), ()->s.teleport(location.clone().add(0,0.3,0)),5);
        return s;
    }

    static DataWatcher cloneDataWatcher(Player parent, EntityPlayer fake){
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

    static PacketPlayOutBlockChange spawnFakeBedPacket(Player parent, BlockPosition location, IBlockAccess access){
        return new PacketPlayOutBlockChange(access, location);
    }

    static PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook move(int id){
        return new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(id, (short)0,(short) 2,(short) 0,(byte)0,(byte)0,false);
    }

    static byte getFixedRotation(float var1){ return (byte) (var1 * 256.0F / 360.0F); }

    static IBlockAccess bedBlockAccess(EnumDirection direction) {
        return new IBlockAccess() {

            public IBlockData getType(BlockPosition arg0) {
                return Blocks.WHITE_BED.getBlockData().set(BlockBed.FACING, direction).set(BlockBed.PART, BlockPropertyBedPart.HEAD);
            }

            public TileEntity getTileEntity(BlockPosition arg0) {
                return null;
            }

            public Fluid getFluid(BlockPosition arg0) {
                return null;
            }
        };
    }

    private static float transform(float rawyaw){
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

    static ItemStack getEquipmentBySlot(EntityEquipment e, EnumItemSlot slot){
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

    private static GameProfile cloneProfile(Player parent){
        EntityPlayer vanillaplayer = ((CraftPlayer)parent).getHandle();
        GameProfile gameProfile = new GameProfile(parent.getUniqueId(), parent.getName());
        gameProfile.getProperties().putAll(vanillaplayer.getProfile().getProperties());
        return gameProfile;
    }

    static Location bedLocation(Location location){
        Location l = location.clone();
        l.setY(0);
        return l;
    }

    static EntityPlayer createNPC(Player parent) {
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
}
