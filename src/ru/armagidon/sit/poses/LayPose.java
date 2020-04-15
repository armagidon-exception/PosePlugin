package ru.armagidon.sit.poses;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.utils.VectorUtils;

import static ru.armagidon.sit.utils.Utils.*;
import static ru.armagidon.sit.utils.VectorUtils.faceToYaw;
import static ru.armagidon.sit.utils.VectorUtils.yawToFace;

public class LayPose extends PluginPose
{

    private EntityPlayer fake;
    private float yaw = 0; //Lay direction
    private BlockFace face;

    public LayPose(Player player) {
        super(player);
        EntityPlayer e = ((CraftPlayer) player).getHandle();
        fake = new EntityPlayer(e.getMinecraftServer(),e.getWorldServer(),e.getProfile(),new PlayerInteractManager(e.getWorldServer()));
    }

    @Override
    public void play(Player receiver,boolean log) {
        getPlayer().setCollidable(COLLIDABLE);
        if(receiver==null){
            VectorUtils.getNear(100,getPlayer()).forEach(this::playAnimation);
        } else {
            playAnimation(receiver);
        }
        if(log){
            getPlayer().sendMessage(LAY);
        }
    }

    @Override
    public void stop(boolean log) {
        getPlayer().setCollidable(true);
        Bukkit.getOnlinePlayers().forEach(this::stopAnimation);
        getPlayers().get(getPlayer().getName()).setPose(new StandingPose(getPlayer()));
        if(log){
            getPlayer().sendMessage(STAND);
        }
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    @Override
    public void move(PlayerMoveEvent event) {
        VectorUtils.getNear(100,getPlayer()).forEach(this::moveHead);
    }

    public void playAnimation(Player receiver) {
        if(receiver.getUniqueId().equals(getPlayer().getUniqueId())) return;
        receiver.hidePlayer(SitPlugin.getInstance(),getPlayer());
        //spawn npc
        spawnPlayer(receiver);
        //lay down
        layPlayer(receiver);
        //Set lay direction
        rotate(receiver);
    }

    public void stopAnimation(Player receiver){
        receiver.showPlayer(SitPlugin.getInstance(),getPlayer());
        if(receiver.getUniqueId().equals(getPlayer().getUniqueId())) return;
        sendPacket(receiver,new PacketPlayOutEntityDestroy(fake.getId()));
    }

    //Spawn player's npc
    private void spawnPlayer(Player receiver){
        Location eye = getPlayer().getEyeLocation();
        Location l = getPlayer().getLocation();
        fake.setPositionRotation(l.getX(),l.getY(),l.getZ(),faceToYaw(yawToFace(eye.getYaw())),eye.getPitch());
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(fake);
        sendPacket(receiver,new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, fake));
        sendPacket(receiver,packetPlayOutNamedEntitySpawn);
        fake.setHeadRotation(90);
        rotate(receiver);
    }

    //Lay player down
    private void layPlayer(Player receiver){
        DataWatcher watcher = fake.getDataWatcher();
        watcher.set(DataWatcherRegistry.a.a(16), (byte)127);
        watcher.set(DataWatcherRegistry.s.a(6),EntityPose.SLEEPING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fake.getId(),watcher,false);
        sendPacket(receiver,metadata);
    }

    //Get fixed location
    private byte getFixRotation(float var1){ return (byte) ((int) (var1 * 256.0F / 360.0F)); }

    //Set lay direction
    private void rotate(Player receiver){
        this.face = yawToFace(getPlayer().getEyeLocation().getYaw());
        float yaw = faceToYaw(yawToFace(getPlayer().getEyeLocation().getYaw()))+45;
        byte fixed_yaw = getFixRotation(yaw);
        PacketPlayOutEntity.PacketPlayOutEntityLook look = new PacketPlayOutEntity.PacketPlayOutEntityLook(fake.getId(),fixed_yaw, (byte) 0, true);
        sendPacket(receiver,look);
        this.yaw=yaw;
    }

    //Move fake's head
    public void moveHead(Player receiver){
        //Move pitch
        PacketPlayOutEntity.PacketPlayOutEntityLook look = new PacketPlayOutEntity.PacketPlayOutEntityLook(fake.getId(),getFixRotation(this.yaw), getFixRotation(getPlayer().getEyeLocation().getPitch()), true);
        sendPacket(receiver,look);
        //Move yaw
        PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(fake,getFixRotation(constrainYaw()));
        sendPacket(receiver,rotation);
    }

    private float constrainYaw(){
        float f = getPlayer().getEyeLocation().getYaw();
        switch (face){
            case EAST:
                if(f<=-135) return -135-90;
                else if(f>-45) return -45-90;
                else break;
            case SOUTH:
                if(f<=-45) return -45-90;
                else if(f>45) return 45-90;
                else break;
            case WEST:
                if(f<=45) return 45-90;
                else if(f>135) return 135-90;
                else break;
            case NORTH:
                if(f<=135) return 135-90;
                else if(f>-135) return -135-90;
                else break;
        }
        return f-90;
    }
}