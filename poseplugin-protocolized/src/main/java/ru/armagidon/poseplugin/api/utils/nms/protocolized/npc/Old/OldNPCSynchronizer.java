package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.PacketContainer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityHeadRotation;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerRelEntityMoveLook;

import java.util.Arrays;

import static ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.FakePlayerUtils.getFixedRotation;

public class OldNPCSynchronizer extends NPCSynchronizer<WrappedDataWatcher> {

    private byte pOverlays;

    public OldNPCSynchronizer(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
        this.pOverlays = fakePlayer.getDataWatcher().getByte(16);
    }

    public void updateEquipment(){
        PacketContainer<WrapperPlayServerEntityEquipment> container = resetEquipment(fakePlayer.getParent().getEquipment());
        fakePlayer.getTrackers().forEach(container::send);
    }

    private PacketContainer<WrapperPlayServerEntityEquipment> resetEquipment(EntityEquipment equipment){
        WrapperPlayServerEntityEquipment[] eq = Arrays.stream(EnumWrappers.ItemSlot.values()).
                map(slot -> {
                    ItemStack itemStack = OldNPCSynchronizer.this.getEquipmentBySlot(equipment, slot);
                    if (slot != EnumWrappers.ItemSlot.MAINHAND && slot != EnumWrappers.ItemSlot.OFFHAND) {
                        NBTModifier.remove(itemStack, PosePluginAPI.NBT_TAG);
                    }
                    WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment();
                    packet.setEntityID(fakePlayer.getId());
                    packet.setItem(itemStack);
                    packet.setSlot(slot);
                    return packet;
                }).
                toArray(WrapperPlayServerEntityEquipment[]::new);
        return new PacketContainer<>(eq);
    }

    public void updateOverlays(){
        byte overlays = WrappedDataWatcher.getEntityWatcher(fakePlayer.getParent()).getByte(16);/*((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));*/
        if(overlays != pOverlays){
            pOverlays = overlays;
            fakePlayer.getMetadataAccessor().setOverlays(pOverlays);
            fakePlayer.getMetadataAccessor().merge(false);
            fakePlayer.getTrackers().forEach(p -> fakePlayer.getMetadataAccessor().showPlayer(p));
        }
    }

    public void updateHeadRotation() {
        WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation(/*npc.getFake(), getFixedRotation(npc.getParent().getLocation().getYaw())*/);
        rotation.setEntityID(fakePlayer.getId());
        rotation.setHeadYaw(getFixedRotation(fakePlayer.getParent().getLocation().getYaw()));
        WrapperPlayServerRelEntityMoveLook lookPacket = new WrapperPlayServerRelEntityMoveLook(/*npc.getFake().getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(npc.getParent().getLocation().getYaw()), (byte) 0, true*/);
        lookPacket.setEntityID(fakePlayer.getId());
        lookPacket.setYaw(fakePlayer.getParent().getLocation().getYaw());
        lookPacket.setOnGround(true);
        fakePlayer.getTrackers().forEach(p -> {
            lookPacket.sendPacket(p);
            rotation.sendPacket(p);
        });
    }

    private ItemStack getEquipmentBySlot(EntityEquipment e, EnumWrappers.ItemSlot slot){
        return switch (slot) {
            case HEAD -> e.getHelmet();
            case CHEST -> e.getChestplate();
            case LEGS -> e.getLeggings();
            case FEET -> e.getBoots();
            case OFFHAND -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
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
