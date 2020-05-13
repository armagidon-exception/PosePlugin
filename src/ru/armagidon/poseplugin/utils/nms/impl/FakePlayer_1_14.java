package ru.armagidon.poseplugin.utils.nms.impl;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.utils.nms.FakePlayer;

import java.util.Arrays;

import static ru.armagidon.poseplugin.utils.VectorUtils.yawToFace;
import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class FakePlayer_1_14 implements FakePlayer
{
    private final EntityPlayer fake;
    private final Player parent;
    private final float yaw;
    private final BlockFace face;

    public FakePlayer_1_14(Player parent) {
        this.parent = parent;
        EntityPlayer e = ((CraftPlayer) parent).getHandle();
        this.fake = new EntityPlayer(e.getMinecraftServer(),e.getWorldServer(),e.getProfile(),new PlayerInteractManager(e.getWorldServer()));
        this.face = yawToFace(parent.getLocation().getYaw());
        this.yaw = getFixedYaw();
    }

    public void spawn(Player receiver){
        Location location = parent.getLocation();
        fake.setPosition(location.getX(),location.getY(),location.getZ());
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(fake);
        sendPacket(receiver,new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake));
        sendPacket(receiver,packetPlayOutNamedEntitySpawn);
        layPlayer(receiver);
        rotateHead(receiver,location.getPitch(),location.getYaw());
    }

    //Get fixed location
    private byte getFixRotation(float var1){ return (byte) ((int) (var1 * 256.0F / 360.0F)); }

    public void remove(Player receiver){
        sendPacket(receiver,new PacketPlayOutEntityDestroy(fake.getId()));
    }

    public void rotateHead(Player receiver, float pitch, float yaw){
        PacketPlayOutEntity.PacketPlayOutEntityLook look = new PacketPlayOutEntity.PacketPlayOutEntityLook(fake.getId(),getFixRotation(this.yaw), getFixRotation(pitch), true);
        sendPacket(receiver,look);
        PacketPlayOutEntityHeadRotation r = new PacketPlayOutEntityHeadRotation(fake,getFixRotation(yaw-90));
        sendPacket(receiver,r);
    }

    @Override
    public void changeEquipment(Player receiver, EntityEquipment inv) {
        Arrays.stream(EnumItemSlot.values()).forEach(slot -> {
            ItemStack i = getEquipmentBySlot(inv,slot);
            if(i!=null&&i.getType().equals(Material.AIR)){
                PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(fake.getId(),slot, CraftItemStack.asNMSCopy(i));
                sendPacket(receiver,equipment);
            }
        });

    }

    private void layPlayer(Player receiver){
        DataWatcher watcher = fake.getDataWatcher();
        watcher.set(DataWatcherRegistry.a.a(15), (byte)127);
        watcher.set(DataWatcherRegistry.s.a(6), EntityPose.SLEEPING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fake.getId(),watcher,false);
        sendPacket(receiver,metadata);
    }

    private float getFixedYaw(){
        switch (face){
            case SOUTH:
                return -90-45;
            case NORTH:
                return -180-45;
            case EAST:
                return 0;
            case WEST:
                return -45;
            default:
                return 0;
        }
    }

    public BlockFace getFace() {
        return face;
    }

    private ItemStack getEquipmentBySlot(EntityEquipment e, EnumItemSlot slot){
        switch (slot){
            case HEAD:
                return e.getHelmet();
            case CHEST:
                return e.getChestplate();
            case LEGS:
                return e.getLeggings();
            case FEET:
                return e.getBoots();
            case OFFHAND:
                return e.getItemInOffHand();
            default:
                return e.getItemInMainHand();
        }
    }
}
