package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCMetadataEditor;

import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.FakePlayer117.*;


public class NPCMetadataEditor117 extends NPCMetadataEditor<SynchedEntityData>
{
    
    private ClientboundSetEntityDataPacket metadata;
    private boolean invisible;

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
        fakePlayer.getDataWatcher().set(EntityDataSerializers.OPTIONAL_BLOCK_POS.createAccessor(14),
                Optional.of(toBlockPosition(toBedLocation(location))));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible != flag) {
            ((FakePlayer117)fakePlayer).getFake().setSharedFlag(5, flag);
            this.invisible = flag;
        }
    }

    @Override
    public byte getLivingEntityTags() {
        return dataWatcher.get(ENTITY_LIVING_TAGS);
    }

    @Override
    public void setLivingEntityTags(byte tags) {
        fakePlayer.getDataWatcher().set(ENTITY_LIVING_TAGS, tags);
    }

    @Override
    public void setOverlays(byte overlays) {
        fakePlayer.getDataWatcher().set(OVERLAYS, overlays);
    }

    @Override
    public void setActiveHand(boolean main) {
        setMainHand(main);
        byte data = fakePlayer.getDataWatcher().get(ENTITY_LIVING_TAGS);
        if(!isHandActive()){
            data = setBit(data, 0,true);
        }
        setLivingEntityTags(setBit(data,1,false));
    }

    @Override
    public void disableHand() {
        byte data = fakePlayer.getDataWatcher().get(ENTITY_LIVING_TAGS);
        if(!isHandActive()) return;
        fakePlayer.getDataWatcher().set(ENTITY_LIVING_TAGS, setBit(data, 0, false));
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
        byte data = fakePlayer.getDataWatcher().get(ENTITY_LIVING_TAGS);
        return isKthBitSet(data, 1);
    }

    @Override
    public void setMainHand(boolean right) {
        fakePlayer.getDataWatcher().set(MAIN_HAND, (byte)(right ? 127 : 0));
    }

    @Override
    public HandType whatHandIsMain() {
        byte data = fakePlayer.getDataWatcher().get(MAIN_HAND);
        return data == 127 ? HandType.RIGHT : HandType.LEFT;
    }

    public static byte setBit(byte input, int k, boolean flag){
        byte output;
        if (flag) {
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
