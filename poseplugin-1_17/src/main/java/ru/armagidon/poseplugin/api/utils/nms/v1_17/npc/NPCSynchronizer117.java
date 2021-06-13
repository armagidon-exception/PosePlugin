package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.EntityEquipment;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.NPCMetadataEditor117.OVERLAYS;


public class NPCSynchronizer117 extends NPCSynchronizer<SynchedEntityData> {

    private byte pOverlays;

    public NPCSynchronizer117(FakePlayer117 npc) {
        super(npc);
        this.pOverlays = ((ServerPlayer)asNMSCopy(npc.getParent())).getEntityData().get(OVERLAYS);
    }

    public void syncEquipment(){
        List<Pair<EquipmentSlot, ItemStack>> slots =
                Arrays.stream(EquipmentSlot.values()).map(slot-> {
                    if( !slot.equals(EquipmentSlot.OFFHAND) && !slot.equals(EquipmentSlot.MAINHAND) ) {
                        org.bukkit.inventory.ItemStack i = getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot);
                        //TODO implement NBTModifier for 1.17 NBTModifier.remove(i, PosePluginAPI.NBT_TAG);
                        //PosePluginAPI.pluginTagClear.pushThrough(i);
                        return Pair.of(slot, CraftItemStack.asNMSCopy(i));
                    } else {
                        return Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot)));
                    }
                }).collect(Collectors.toList());
        ClientboundSetEquipmentPacket eq = new ClientboundSetEquipmentPacket(fakePlayer.getId(), slots);
        fakePlayer.getTrackers().forEach(r -> FakePlayer117.sendPacket(r,eq));
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
        ClientboundRotateHeadPacket rotation = new ClientboundRotateHeadPacket(((FakePlayer117)fakePlayer).getFake(), getFixedRotation(fakePlayer.getParent().getLocation().getYaw()));
        ClientboundMoveEntityPacket.PosRot lookPacket = new ClientboundMoveEntityPacket.PosRot(fakePlayer.getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(fakePlayer.getParent().getLocation().getYaw()), (byte) 0, true);
        fakePlayer.getTrackers().forEach(p -> {
            FakePlayer117.sendPacket(p, lookPacket);
            FakePlayer117.sendPacket(p, rotation);
        });
    }

    public static org.bukkit.inventory.ItemStack getEquipmentBySlot(EntityEquipment e, Enum<?> slot){
        org.bukkit.inventory.ItemStack item;
        if (!slot.getDeclaringClass().getSimpleName().equalsIgnoreCase("EnumItemSlot"))
            return new org.bukkit.inventory.ItemStack(Material.AIR);
        item = switch (slot.name()) {
            case "HEAD" -> e.getHelmet();
            case "CHEST" -> e.getChestplate();
            case "LEGS" -> e.getLeggings();
            case "FEET" -> e.getBoots();
            case "OFFHAND" -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
        return item;
    }

    public static byte getFixedRotation(float var1){
        return (byte) round(var1 * 256.0F / 360.0F);
    }

    private static float round(float input){
        int output = (int)input;
        return input < (float)output ? output - 1 : output;
    }
}
