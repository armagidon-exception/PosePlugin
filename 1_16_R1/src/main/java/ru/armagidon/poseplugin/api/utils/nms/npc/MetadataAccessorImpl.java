package ru.armagidon.poseplugin.api.utils.nms.npc;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer_v1_16_R1.FakePlayerStaff.*;

public class MetadataAccessorImpl implements MetadataAccessor
{

    private final FakePlayer_v1_16_R1 npc;
    private PacketPlayOutEntityMetadata metadata;
    private boolean invisible;
    private final DataWatcher watcher;

    public MetadataAccessorImpl(FakePlayer_v1_16_R1 npc) {
        this.npc = npc;
        this.watcher = npc.getWatcher();
    }

    @Override
    public void showPlayer(Player receiver) {
        if(metadata!=null){
            NMSUtils.sendPacket(receiver, metadata);
        }
    }

    @Override
    public void setPose(Pose pose) {
        npc.getWatcher().set(DataWatcherRegistry.s.a(6), EntityPose.values()[pose.ordinal()]);
    }

    @Override
    public void setBedPosition(Location location) {
        Location bedLoc = location.clone().toVector().setY(0).toLocation(npc.getParent().getWorld());
        npc.getWatcher().set(DataWatcherRegistry.m.a(13), Optional.of(toBlockPosition(bedLoc)));
    }

    @Override
    public void setInvisible(boolean flag) {
        if(this.invisible!=flag) {
            byte value = ((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(0));
            npc.getWatcher().set(DataWatcherRegistry.a.a(0), setBit(value, 5,flag));
            this.invisible = flag;
        }
    }

    @Override
    public void setOverlays(byte overlays) {
        npc.getWatcher().set(DataWatcherRegistry.a.a(16), overlays);
    }

    @Override
    public void setActiveHand(boolean main) {
        byte data = npc.getWatcher().get(DataWatcherRegistry.a.a(7));
        if(!isHandActive()){
            data = setBit(data, 0,true);
        }
        boolean active = isKthBitSet(npc.getWatcher().get(DataWatcherRegistry.a.a(7)),2);
        if(active==main) return;
        npc.getWatcher().set(DataWatcherRegistry.a.a(7),setBit(data,1,!main));
    }

    @Override
    public void disableHand() {
        byte data = npc.getWatcher().get(DataWatcherRegistry.a.a(7));
        if(!isHandActive()) return;
        npc.getWatcher().set(DataWatcherRegistry.a.a(7),setBit(data, 0, false));
    }

    @Override
    public Pose getPose() {
        return Pose.values()[npc.getWatcher().get(DataWatcherRegistry.s.a(6)).ordinal()];
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public void merge(boolean resend) {
        metadata = new PacketPlayOutEntityMetadata(npc.getFake().getId(), this.watcher, resend);
    }

    @Override
    public boolean isHandActive() {
        byte data = npc.getWatcher().get(DataWatcherRegistry.a.a(7));
        return isKthBitSet(data, 1);
    }

}
