package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityHeadRotation;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerRelEntityMoveLook;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.getFixedRotation;

public class NewNPCSynchronizer extends NPCSynchronizer<WrappedDataWatcher> {

    private byte pOverlays;

    public NewNPCSynchronizer(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
        this.pOverlays = fakePlayer.getDataWatcher().getByte(16);/*((EntityPlayer)asNMSCopy(fakePlayer.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));*/
    }

    public void syncEquipment(){
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots =
                Arrays.stream(EnumWrappers.ItemSlot.values()).map(slot -> {
                    if( !slot.equals(EnumWrappers.ItemSlot.MAINHAND) && !slot.equals(EnumWrappers.ItemSlot.OFFHAND) ) {
                        ItemStack i = getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot);
                        NBTModifier.remove(i, PosePluginAPI.NBT_TAG);
                        return new Pair<>(slot, i);
                    } else {
                        return new Pair<>(slot, getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot));
                    }
                }).collect(Collectors.toList());
        WrapperPlayServerEntityEquipment eq = new WrapperPlayServerEntityEquipment(/*npc.getFake().getId(), slots*/);
        eq.setSlotStackPairsList(slots);
        eq.setEntityID(fakePlayer.getId());
        fakePlayer.getTrackers().forEach(eq::sendPacket);
    }

    public void syncOverlays(){
        byte overlays = WrappedDataWatcher.getEntityWatcher(fakePlayer.getParent()).getByte(16);//((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays != pOverlays){
            pOverlays = overlays;
            fakePlayer.getMetadataAccessor().setOverlays(pOverlays);
            fakePlayer.getMetadataAccessor().merge(false);
            fakePlayer.getTrackers().forEach(p -> fakePlayer.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation(/*npc.getFake(), getFixedRotation(npc.getParent().getLocation().getYaw())*/);
        rotation.setEntityID(fakePlayer.getId());
        rotation.setHeadYaw(getFixedRotation(fakePlayer.getParent().getLocation().getYaw()));
        WrapperPlayServerRelEntityMoveLook lookPacket = new WrapperPlayServerRelEntityMoveLook(/*fakePlayer.getFake().getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(fakePlayer.getParent().getLocation().getYaw()), (byte) 0, true*/);
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
}
