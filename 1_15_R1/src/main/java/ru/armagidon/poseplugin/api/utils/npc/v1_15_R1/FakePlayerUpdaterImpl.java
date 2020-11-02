package ru.armagidon.poseplugin.api.utils.npc.v1_15_R1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.util.PacketContainer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerSynchronizer;

import java.util.Arrays;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.npc.v1_15_R1.FakePlayer.FakePlayerStaff.getEquipmentBySlot;
import static ru.armagidon.poseplugin.api.utils.npc.v1_15_R1.FakePlayer.FakePlayerStaff.getFixedRotation;

public class FakePlayerUpdaterImpl implements FakePlayerSynchronizer {

    private final FakePlayer npc;

    private byte pOverlays;

    public FakePlayerUpdaterImpl(FakePlayer npc) {
        this.npc = npc;
        this.pOverlays = ((EntityPlayer)asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
    }

    public void updateEquipment(){
        PacketContainer<PacketPlayOutEntityEquipment> container = resetEquipment(npc.getFake().getId(), npc.getParent().getEquipment());
        npc.getTrackers().forEach(container::send);
    }

    private PacketContainer<PacketPlayOutEntityEquipment> resetEquipment(int id, EntityEquipment equipment){
        PacketPlayOutEntityEquipment[] eq = Arrays.stream(EnumItemSlot.values()).
                map(slot->{
                    ItemStack itemStack = getEquipmentBySlot(equipment, slot);
                    if( slot != EnumItemSlot.MAINHAND && slot != EnumItemSlot.OFFHAND ) PosePluginAPI.pluginTagClear.pushThrough(itemStack);
                    return new PacketPlayOutEntityEquipment(id, slot, CraftItemStack.asNMSCopy(itemStack));
                }).
                toArray(PacketPlayOutEntityEquipment[]::new);
        return new PacketContainer<>(eq);
    }

    public void updateOverlays(){
        byte overlays = ((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays!=pOverlays){
            pOverlays = overlays;
            npc.getMetadataAccessor().setOverlays(pOverlays);
            npc.getMetadataAccessor().merge(false);
            npc.getTrackers().forEach(p-> npc.getMetadataAccessor().showPlayer(p));
        }
    }

    public void updateHeadRotation() {
        PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(npc.getFake(), getFixedRotation(npc.getParent().getLocation().getYaw()));
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(npc.getFake().getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(npc.getParent().getLocation().getYaw()), (byte) 0, true);
        npc.getTrackers().forEach(p -> {
            NMSUtils.sendPacket(p, lookPacket);
            NMSUtils.sendPacket(p, rotation);
        });
    }

    @Override
    public void syncHeadRotation() {
        updateHeadRotation();
    }

    @Override
    public void syncOverlays() {
        updateOverlays();
    }

    @Override
    public void syncEquipment() {
        updateEquipment();
    }
}
