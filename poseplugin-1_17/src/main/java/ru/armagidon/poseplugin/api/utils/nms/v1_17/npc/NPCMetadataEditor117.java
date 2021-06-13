package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCMetadataEditor;

import java.lang.reflect.Field;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.FakePlayer117.toBlockPosition;


public class NPCMetadataEditor117 extends NPCMetadataEditor<SynchedEntityData>
{
    
    private ClientboundSetEntityDataPacket metadata;
    private boolean invisible;

    //Constants
    private static final EntityDataSerializer<Byte> BYTE = EntityDataSerializers.BYTE;
    public static EntityDataAccessor<Byte> DISPLAYING;
    public static EntityDataAccessor<Byte> OVERLAYS;
    public static EntityDataAccessor<net.minecraft.world.entity.Pose> POSE;
    public static EntityDataAccessor<Byte> ACTIVATE_HAND;
    public static EntityDataAccessor<Optional<BlockPos>> BED_POSITION;

    static {
        try {
            Field overlaysF = net.minecraft.world.entity.player.Player.class.getDeclaredField("DATA_PLAYER_MODE_CUSTOMISATION");
            overlaysF.setAccessible(true);
            OVERLAYS = (EntityDataAccessor<Byte>) overlaysF.get(null);

            Field poseF = Entity.class.getDeclaredField("ad");
            poseF.setAccessible(true);
            POSE = (EntityDataAccessor<net.minecraft.world.entity.Pose>) poseF.get(null);

            Field activeHandF = net.minecraft.world.entity.player.Player.class.getDeclaredField("DATA_PLAYER_MAIN_HAND");
            activeHandF.setAccessible(true);
            ACTIVATE_HAND = BYTE.createAccessor(7);

            Field bedPosF = LivingEntity.class.getDeclaredField("SLEEPING_POS_ID");
            bedPosF.setAccessible(true);
            BED_POSITION = (EntityDataAccessor<Optional<BlockPos>>) bedPosF.get(null);

            Field displayingF = Entity.class.getDeclaredField("DATA_SHARED_FLAGS_ID");
            displayingF.setAccessible(true);
            DISPLAYING = (EntityDataAccessor<Byte>) displayingF.get(null);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        fakePlayer.getDataWatcher().set(POSE, net.minecraft.world.entity.Pose.values()[pose.ordinal()]);
    }

    @Override
    public void setBedPosition(Location location) {
        Location bedLoc = location.clone().toVector().setY(0).toLocation(fakePlayer.getParent().getWorld());
        fakePlayer.getDataWatcher().set(BED_POSITION, Optional.of(toBlockPosition(bedLoc)));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible != flag) {
            byte value = ((ServerPlayer) NMSUtils.asNMSCopy(fakePlayer.getParent())).getEntityData().get(DISPLAYING);
            fakePlayer.getDataWatcher().set(DISPLAYING, setBit(value, 5,flag));
            this.invisible = flag;
        }
    }

    @Override
    public void setOverlays(byte overlays) {
        fakePlayer.getDataWatcher().set(OVERLAYS, overlays);
    }

    @Override
    public void setActiveHand(boolean main) {
        setMainHand(main);
        byte data = fakePlayer.getDataWatcher().get(ACTIVATE_HAND);
        if(!isHandActive()){
            data = setBit(data, 0,true);
        }
        fakePlayer.getDataWatcher().set(ACTIVATE_HAND, setBit(data,1,false));
    }

    @Override
    public void disableHand() {
        byte data = fakePlayer.getDataWatcher().get(ACTIVATE_HAND);
        if(!isHandActive()) return;
        fakePlayer.getDataWatcher().set(ACTIVATE_HAND,setBit(data, 0, false));
    }

    @Override
    public Pose getPose() {
        return Pose.values()[fakePlayer.getDataWatcher().get(POSE).ordinal()];
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public void merge(boolean resend) {
        metadata = new ClientboundSetEntityDataPacket(fakePlayer.getId(), this.dataWatcher, resend);
    }

    @Override
    public boolean isHandActive() {
        byte data = fakePlayer.getDataWatcher().get(ACTIVATE_HAND);
        return isKthBitSet(data, 1);
    }

    @Override
    public void setMainHand(boolean right) {
        fakePlayer.getDataWatcher().set(BYTE.createAccessor(17),(byte)(right ? 127 : 0));
    }

    @Override
    public HandType whatHandIsMain() {
        byte data = fakePlayer.getDataWatcher().get(BYTE.createAccessor(17));
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
