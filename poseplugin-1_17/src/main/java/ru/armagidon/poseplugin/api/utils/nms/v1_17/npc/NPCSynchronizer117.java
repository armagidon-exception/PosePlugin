package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.createPacketInstance;
import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.getEquipmentBySlot;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.FakePlayer117.OVERLAYS;


public class NPCSynchronizer117 extends NPCSynchronizer<SynchedEntityData> {

    private byte pOverlays;

    public NPCSynchronizer117(FakePlayer117 npc) {
        super(npc);
        this.pOverlays = ((ServerPlayer)asNMSCopy(npc.getParent())).getEntityData().get(OVERLAYS);
    }

    public void syncEquipment(){
        List<Map.Entry<EquipmentSlot, ItemStack>> slots =
                Arrays.stream(EquipmentSlot.values()).filter(slot -> !ignoredSlots.contains(slot)).map(slot-> {
                    if( !slot.equals(EquipmentSlot.OFF_HAND) && !slot.equals(EquipmentSlot.HAND) ) {
                        ItemStack i = getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot).clone();
                        return Map.entry(EquipmentSlot.values()[slot.ordinal()], i);
                    } else {
                        return Map.entry(EquipmentSlot.values()[slot.ordinal()], getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot));
                    }
                }).collect(Collectors.toList());

        fakePlayer.getInventory().setPiecesOfEquipment(slots);
        fakePlayer.getInventory().update();
    }

    public void syncOverlays(){
        byte overlays = ((ServerPlayer) NMSUtils.asNMSCopy(fakePlayer.getParent())).getEntityData().get(OVERLAYS);
        if(overlays != pOverlays){
            pOverlays = overlays;
            fakePlayer.getMetadataAccessor().setOverlays(pOverlays);
            fakePlayer.getMetadataAccessor().merge(false);
            fakePlayer.getTrackers().forEach(p -> fakePlayer.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        fakePlayer.setRotation(fakePlayer.getParent().getLocation().getPitch(), fakePlayer.getParent().getLocation().getYaw());
        ClientboundRotateHeadPacket rotation = new ClientboundRotateHeadPacket(((FakePlayer117)fakePlayer).getFake(), getFixedRotation(((FakePlayer117) fakePlayer).getFake().getXRot()));
        ClientboundMoveEntityPacket.PosRot lookPacket = new ClientboundMoveEntityPacket.PosRot(fakePlayer.getId(),
                (short) 0,
                (short) 0,
                (short) 0,
                getFixedRotation(((FakePlayer117) fakePlayer).getFake().getXRot()),
                getFixedRotation(((FakePlayer117) fakePlayer).getFake().getYRot()), true);
        fakePlayer.getTrackers().forEach(p -> {
            FakePlayer117.sendPacket(p, lookPacket);
            FakePlayer117.sendPacket(p, rotation);
        });
    }

    public static byte getFixedRotation(float var1){
        return (byte) round(var1 * 256.0F / 360.0F);
    }

    private static float round(float input){
        int output = (int) input;
        return input < (float)output ? output - 1 : output;
    }
}
