package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcherSerializer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityPose;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCMetadataEditor;

import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.FakePlayer117.toBlockPosition;


public class NPCMetadataEditor117 extends NPCMetadataEditor<DataWatcher>
{
    
    private PacketPlayOutEntityMetadata metadata;
    private boolean invisible;

    //Constants
    private final DataWatcherSerializer<Byte> BYTE = DataWatcherRegistry.a;

    public NPCMetadataEditor117(FakePlayer117 npc) {
        super(npc);
    }

    @Override
    public void showPlayer(Player receiver) {
        if(metadata != null){
            FakePlayer117.sendPacket(receiver, metadata);
        }
    }

    @Override
    public void setPose(Pose pose) {
        fakePlayer.getDataWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.values()[pose.ordinal()]);
    }

    @Override
    public void setBedPosition(Location location) {
        Location bedLoc = location.clone().toVector().setY(0).toLocation(fakePlayer.getParent().getWorld());
        fakePlayer.getDataWatcher().set(DataWatcherRegistry.m.a(13), Optional.of(toBlockPosition(bedLoc)));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible != flag) {
            byte value = ((EntityPlayer) NMSUtils.asNMSCopy(fakePlayer.getParent())).getDataWatcher().get(BYTE.a(0));
            fakePlayer.getDataWatcher().set(BYTE.a(0), setBit(value, 5,flag));
            this.invisible = flag;
        }
    }

    @Override
    public void setOverlays(byte overlays) {
        fakePlayer.getDataWatcher().set(DataWatcherRegistry.a.a(16), overlays);
    }

    @Override
    public void setActiveHand(boolean main) {
        setMainHand(main);
        byte data = fakePlayer.getDataWatcher().get(BYTE.a(7));
        if(!isHandActive()){
            data = setBit(data, 0,true);
        }
        fakePlayer.getDataWatcher().set(BYTE.a(7),setBit(data,1,false));
    }

    @Override
    public void disableHand() {
        byte data = fakePlayer.getDataWatcher().get(DataWatcherRegistry.a.a(7));
        if(!isHandActive()) return;
        fakePlayer.getDataWatcher().set(DataWatcherRegistry.a.a(7),setBit(data, 0, false));
    }

    @Override
    public Pose getPose() {
        return Pose.values()[fakePlayer.getDataWatcher().get(DataWatcherRegistry.s.a(6)).ordinal()];
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public void merge(boolean resend) {
        metadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), this.dataWatcher, resend);
    }

    @Override
    public boolean isHandActive() {
        byte data = fakePlayer.getDataWatcher().get(DataWatcherRegistry.a.a(7));
        return isKthBitSet(data, 1);
    }

    @Override
    public void setMainHand(boolean right) {
        fakePlayer.getDataWatcher().set(BYTE.a(17),(byte)(right ? 127 : 0));
    }

    @Override
    public HandType whatHandIsMain() {
        byte data = fakePlayer.getDataWatcher().get(BYTE.a(17));
        return data == 127 ? HandType.RIGHT : HandType.LEFT;
    }

    public static byte setBit(byte input, int k, boolean flag){
        byte output;
        if(flag){
            output = (byte) (input | (1 << k));
        } else {
            output = (byte) (input & ~(1 << k));
        }
        return output;
    }

    public static boolean isKthBitSet(int n, int k)
    {
        return  ((n & (1 << (k - 1))) == 1);
    }

}
