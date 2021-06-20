package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityHeadRotation;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerRelEntityMoveLook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.getEquipmentBySlot;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.getFixedRotation;


public class NPCSynchronizerProtocolized extends NPCSynchronizer<WrappedDataWatcher>
{

    private byte pOverlays;

    public NPCSynchronizerProtocolized(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
        this.pOverlays = fakePlayer.getDataWatcher().getByte(16);
    }

    public void syncOverlays(){
        byte overlays = WrappedDataWatcher.getEntityWatcher(fakePlayer.getParent()).getByte(16);
        if(overlays != pOverlays){
            pOverlays = overlays;
            fakePlayer.getMetadataAccessor().setOverlays(pOverlays);
            fakePlayer.getMetadataAccessor().merge(false);
            fakePlayer.getTrackers().forEach(p -> fakePlayer.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        fakePlayer.setRotation(fakePlayer.getParent().getLocation().getPitch(), fakePlayer.getParent().getLocation().getYaw());

        WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation();
        rotation.setEntityID(fakePlayer.getId());
        rotation.setHeadYaw(getFixedRotation(fakePlayer.getPosition().getYaw()));
        WrapperPlayServerRelEntityMoveLook lookPacket = new WrapperPlayServerRelEntityMoveLook();
        lookPacket.setEntityID(fakePlayer.getId());
        lookPacket.setYaw(fakePlayer.getPosition().getYaw());
        lookPacket.setPitch(fakePlayer.getPosition().getPitch());
        lookPacket.setOnGround(true);
        fakePlayer.getTrackers().forEach(p -> {
            lookPacket.sendPacket(p);
            rotation.sendPacket(p);
        });
    }

    public void syncEquipment(){
        List<Map.Entry<EquipmentSlot, ItemStack>> slots = Arrays.stream(EquipmentSlot.values()).filter(slot -> !ignoredSlots.contains(slot)).map(slot -> {
                    if( !slot.equals(EquipmentSlot.HAND) && !slot.equals(EquipmentSlot.OFF_HAND) ) {
                        ItemStack i = getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot);
                        return Map.entry(slot, i);
                    } else {
                        return Map.entry(slot, getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot));
                    }
                }).collect(Collectors.toList());

        fakePlayer.getInventory().setPiecesOfEquipment(slots);
        fakePlayer.getInventory().update();
    }
}
