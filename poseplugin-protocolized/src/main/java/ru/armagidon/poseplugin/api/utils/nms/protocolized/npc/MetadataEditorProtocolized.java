package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.misc.MetaDataKeyRings;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCMetadataEditor;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityMetadata;
import ru.armagidon.poseplugin.api.utils.versions.Version;

import java.util.Optional;


public class MetadataEditorProtocolized extends NPCMetadataEditor<WrappedDataWatcher>
{

    private WrapperPlayServerEntityMetadata metadata;

    //Constants
    private final WrappedDataWatcher.Serializer BYTE = WrappedDataWatcher.Registry.get(Byte.class);
    private static final MetaDataKeyRings keyRings = new MetaDataKeyRings();

    private static final MetaDataKeyRings.KeyRing ENTITY_FLAGS;
    private static final MetaDataKeyRings.KeyRing POSE;
    private static final MetaDataKeyRings.KeyRing LIVING_TAGS;
    private static final MetaDataKeyRings.KeyRing BED_POS;
    public static final MetaDataKeyRings.KeyRing OVERLAYS;
    private static final MetaDataKeyRings.KeyRing MAIN_HAND;

    static {
        initKeyRings();
        ENTITY_FLAGS = keyRings.getKeyRing("ENTITY_FLAGS");
        POSE = keyRings.getKeyRing("POSE");
        LIVING_TAGS = keyRings.getKeyRing("LIVING_TAGS");
        BED_POS = keyRings.getKeyRing("BED_POS");
        OVERLAYS = keyRings.getKeyRing("OVERLAYS");
        MAIN_HAND = keyRings.getKeyRing("MAIN_HAND");
    }

    public MetadataEditorProtocolized(FakePlayer<WrappedDataWatcher> npc) {
        super(npc);
    }

    private static void initKeyRings() {
        keyRings.generateKeyRing("ENTITY_FLAGS",
                ImmutableMap.<Version, Integer>builder()
                        .put(Version.v1_15, 0)
                        .put(Version.v1_16, 0)
                        .put(Version.v1_17, 0).build());
        keyRings.generateKeyRing("POSE",
                ImmutableMap.<Version, Integer>builder()
                        .put(Version.v1_15, 6)
                        .put(Version.v1_16, 6)
                        .put(Version.v1_17, 6).build());
        keyRings.generateKeyRing("LIVING_TAGS",
                ImmutableMap.<Version, Integer>builder()
                        .put(Version.v1_15, 7)
                        .put(Version.v1_16, 7)
                        .put(Version.v1_17, 8).build());
        keyRings.generateKeyRing("BED_POS",
                ImmutableMap.<Version, Integer>builder()
                        .put(Version.v1_15, 13)
                        .put(Version.v1_16, 13)
                        .put(Version.v1_17, 14).build());
        keyRings.generateKeyRing("OVERLAYS",
                ImmutableMap.<Version, Integer>builder()
                        .put(Version.v1_15, 16)
                        .put(Version.v1_16, 16)
                        .put(Version.v1_17, 17).build());
        keyRings.generateKeyRing("MAIN_HAND",
                ImmutableMap.<Version, Integer>builder()
                        .put(Version.v1_15, 17)
                        .put(Version.v1_16, 17)
                        .put(Version.v1_17, 18).build());


    }

    @Override
    public byte getLivingEntityTags() {
        return dataWatcher.getByte(LIVING_TAGS.getKey());
    }

    @Override
    public void setLivingEntityTags(byte tags) {
        dataWatcher.setObject(LIVING_TAGS.getKey(), BYTE, tags);
    }

    @Override
    public void showPlayer(Player receiver) {
        if(metadata != null){
            metadata.sendPacket(receiver);
        }
    }

    @Override
    public void setPose(Pose pose) {
        dataWatcher.setObject(POSE.getKey(), WrappedDataWatcher.Registry.get(EnumWrappers.getEntityPoseClass()), EnumWrappers.EntityPose.values()[pose.ordinal()].toNms());
    }

    @Override
    public void setBedPosition(Location location) {
        BlockPosition bp = new BlockPosition(location.clone().toVector().setY(0));
        dataWatcher.setObject(BED_POS.getKey(), WrappedDataWatcher.Registry.getBlockPositionSerializer(true), Optional.of(BlockPosition.getConverter().getGeneric(bp)));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible != flag) {
            byte value = dataWatcher.getByte(ENTITY_FLAGS.getKey());
            dataWatcher.setObject(ENTITY_FLAGS.getKey(), BYTE, FakePlayerUtils.setBit(value, 5, flag));
            this.invisible = flag;
        }
    }

    @Override
    public void setOverlays(byte overlays) {
        dataWatcher.setObject(OVERLAYS.getKey(), BYTE, overlays, false);
    }

    @Override
    public void setActiveHand(boolean main) {
        setMainHand(main);
        byte data = dataWatcher.getByte(LIVING_TAGS.getKey());//npc.getWatcher().get(BYTE.a(LIVING_TAGS.getKey()));
        if(!isHandActive()){
            data = FakePlayerUtils.setBit(data, 0,true);
        }
        dataWatcher.setObject(LIVING_TAGS.getKey(), BYTE, FakePlayerUtils.setBit(data, 1, false));
    }

    @Override
    public void disableHand() {
        byte data = dataWatcher.getByte(LIVING_TAGS.getKey());
        if(!isHandActive()) return;
        dataWatcher.setObject(LIVING_TAGS.getKey(), BYTE, FakePlayerUtils.setBit(data, 0, false));
    }

    @Override
    public Pose getPose() {
        return Pose.values()[((Enum<?>) dataWatcher.getObject(POSE.getKey())).ordinal()];
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
        byte data = dataWatcher.getByte(LIVING_TAGS.getKey());
        return FakePlayerUtils.isKthBitSet(data, 1);
    }

    @Override
    public void setMainHand(boolean right) {
        dataWatcher.setObject(MAIN_HAND.getKey(), BYTE, (byte) (right ? 127 : 0));
    }

    @Override
    public HandType whatHandIsMain() {
        byte data = dataWatcher.getByte(MAIN_HAND.getKey());
        return data > 0 ? HandType.RIGHT : HandType.LEFT;
    }

    @Override
    public void update() {
        fakePlayer.getTrackers().forEach(this::showPlayer);
    }
}
