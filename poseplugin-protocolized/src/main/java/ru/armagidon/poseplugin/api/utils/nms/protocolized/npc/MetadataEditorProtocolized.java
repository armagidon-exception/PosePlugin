package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCMetadataEditor;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityMetadata;

import java.util.Optional;


public class MetadataEditorProtocolized extends NPCMetadataEditor<WrappedDataWatcher>
{

    private WrapperPlayServerEntityMetadata metadata;

    //Constants
    private final WrappedDataWatcher.Serializer BYTE = WrappedDataWatcher.Registry.get(Byte.class);

    public MetadataEditorProtocolized(FakePlayer<WrappedDataWatcher> npc) {
        super(npc);
    }

    @Override
    public byte getLivingEntityTags() {
        return dataWatcher.getByte(7);
    }

    @Override
    public void setLivingEntityTags(byte tags) {
        dataWatcher.setObject(7, BYTE, tags);
    }

    @Override
    public void showPlayer(Player receiver) {
        if(metadata != null){
            metadata.sendPacket(receiver);
        }
    }

    @Override
    public void setPose(Pose pose) {
        dataWatcher.setObject(6, WrappedDataWatcher.Registry.get(EnumWrappers.getEntityPoseClass()), EnumWrappers.EntityPose.values()[pose.ordinal()].toNms());
    }

    @Override
    public void setBedPosition(Location location) {
        BlockPosition bp = new BlockPosition(location.clone().toVector().setY(0));
        dataWatcher.setObject(13, WrappedDataWatcher.Registry.getBlockPositionSerializer(true), Optional.of(BlockPosition.getConverter().getGeneric(bp)));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible != flag) {
            byte value = dataWatcher.getByte(0);
            dataWatcher.setObject(0, BYTE, FakePlayerUtils.setBit(value, 5, flag));
            this.invisible = flag;
        }
    }

    @Override
    public void setOverlays(byte overlays) {
        dataWatcher.setObject(16, BYTE, overlays, false);
    }

    @Override
    public void setActiveHand(boolean main) {
        setMainHand(main);
        byte data = dataWatcher.getByte(7);//npc.getWatcher().get(BYTE.a(7));
        if(!isHandActive()){
            data = FakePlayerUtils.setBit(data, 0,true);
        }
        dataWatcher.setObject(7, BYTE, FakePlayerUtils.setBit(data, 1, false));
    }

    @Override
    public void disableHand() {
        byte data = dataWatcher.getByte(7);
        if(!isHandActive()) return;
        dataWatcher.setObject(7, BYTE, FakePlayerUtils.setBit(data, 0, false));
    }

    @Override
    public Pose getPose() {
        return Pose.values()[((Enum<?>) dataWatcher.getObject(6)).ordinal()];
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public void merge(boolean resend) {
        metadata = new WrapperPlayServerEntityMetadata();
        metadata.setMetadata(dataWatcher.getWatchableObjects());
        metadata.setEntityID(fakePlayer.getId());
    }

    @Override
    public boolean isHandActive() {
        byte data = dataWatcher.getByte(7);
        return FakePlayerUtils.isKthBitSet(data, 1);
    }

    @Override
    public void setMainHand(boolean right) {
        dataWatcher.setObject(17, BYTE, (byte) (right ? 127 : 0));
    }

    @Override
    public HandType whatHandIsMain() {
        byte data = dataWatcher.getByte(17);
        return data == 127 ? HandType.RIGHT : HandType.LEFT;
    }

    @Override
    public void update() {
        fakePlayer.getTrackers().forEach(this::showPlayer);
    }
}
