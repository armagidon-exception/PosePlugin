package ru.armagidon.poseplugin.api.utils.npc.protocolized;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerMetadataAccessor;
import ru.armagidon.poseplugin.api.utils.npc.HandType;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityMetadata;

import javax.swing.plaf.synth.Region;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.isKthBitSet;
import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.setBit;

public class MetadataAccessorProtocolized implements FakePlayerMetadataAccessor
{

    private final FakePlayer npc;
    private WrapperPlayServerEntityMetadata metadata;
    private boolean invisible;
    private final WrappedDataWatcher watcher;

    //Constants
    private final WrappedDataWatcher.Serializer BYTE = WrappedDataWatcher.Registry.get(Byte.class);

    public MetadataAccessorProtocolized(FakePlayer npc) {
        this.npc = npc;
        this.watcher = npc.getDataWatcher();
    }

    @Override
    public void showPlayer(Player receiver) {
        if(metadata != null){
            metadata.sendPacket(receiver);
        }
    }

    @Override
    public void setPose(Pose pose) {
        watcher.setObject(6, WrappedDataWatcher.Registry.get(EnumWrappers.getEntityPoseClass()), EnumWrappers.EntityPose.values()[pose.ordinal()].toNms());
        //npc.getWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.values()[pose.ordinal()]);
    }

    @Override
    public void setBedPosition(Location location) {
        BlockPosition bp = new BlockPosition(location.clone().toVector().setY(0));
        watcher.setObject(13, WrappedDataWatcher.Registry.getBlockPositionSerializer(true), Optional.of(BlockPosition.getConverter().getGeneric(bp)));
        //npc.getWatcher().set(DataWatcherRegistry.m.a(13), Optional.of((BlockPosition) toBlockPosition(bedLoc)));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible != flag) {
            byte value = watcher.getByte(0);
            watcher.setObject(0, BYTE, setBit(value, 5, flag));
            this.invisible = flag;
        }
    }

    @Override
    public void setOverlays(byte overlays) {
        watcher.setObject(16, BYTE, overlays, false);
    }

    @Override
    public void setActiveHand(boolean main) {
        setMainHand(main);
        byte data = watcher.getByte(7);//npc.getWatcher().get(BYTE.a(7));
        if(!isHandActive()){
            data = setBit(data, 0,true);
        }
        watcher.setObject(7, BYTE, setBit(data, 1, false));
        //npc.getWatcher().set(BYTE.a(7),setBit(data,1,false));
    }

    @Override
    public void disableHand() {
        byte data = watcher.getByte(7);//npc.getWatcher().get(DataWatcherRegistry.a.a(7));
        if(!isHandActive()) return;
        //npc.getWatcher().set(DataWatcherRegistry.a.a(7),setBit(data, 0, false));
        watcher.setObject(7, BYTE, setBit(data, 0, false));
    }

    @Override
    public Pose getPose() {
        return Pose.values()[((Enum<?>)watcher.getObject(6)).ordinal()/*npc.getWatcher().get(DataWatcherRegistry.s.a(6)).ordinal()*/];
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public void merge(boolean resend) {
        metadata = new WrapperPlayServerEntityMetadata();
        metadata.setMetadata(watcher.getWatchableObjects());
        metadata.setEntityID(npc.getId());
        if (resend) {
            metadata.getMetadata().forEach(obj -> obj.setDirtyState(true));
        }
        //metadata = new PacketPlayOutEntityMetadata(npc.getFake().getId(), this.watcher, resend);
    }

    @Override
    public boolean isHandActive() {
        byte data = watcher.getByte(7);
        return isKthBitSet(data, 1);
    }

    @Override
    public void setMainHand(boolean right) {
        watcher.setObject(17, BYTE, (byte) (right ? 127 : 0));
        //npc.getWatcher().set(BYTE.a(17),(byte)(right ? 127 : 0));
    }

    @Override
    public HandType whatHandIsMain() {
        byte data = watcher.getByte(17);
        return data == 127 ? HandType.RIGHT : HandType.LEFT;
    }
}
