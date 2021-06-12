package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.EntityEquipment;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;


public class NPCSynchronizer117 extends NPCSynchronizer<DataWatcher> {

    private byte pOverlays;

    public NPCSynchronizer117(FakePlayer117 npc) {
        super(npc);
        this.pOverlays = ((EntityPlayer)asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
    }

    public void syncEquipment(){
        List<Pair<EnumItemSlot, ItemStack>> slots =
                Arrays.stream(EnumItemSlot.values()).map(slot-> {
                    if( !slot.equals(EnumItemSlot.a) && !slot.equals(EnumItemSlot.b) ) {
                        org.bukkit.inventory.ItemStack i = getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot);
                        NBTModifier.remove(i, PosePluginAPI.NBT_TAG);
                        //PosePluginAPI.pluginTagClear.pushThrough(i);
                        return Pair.of(slot, CraftItemStack.asNMSCopy(i));
                    } else {
                        return Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot)));
                    }
                }).collect(Collectors.toList());
        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(fakePlayer.getId(), slots);
        fakePlayer.getTrackers().forEach(r -> FakePlayer117.sendPacket(r,eq));
    }

    public void syncOverlays(){
        byte overlays = ((EntityPlayer) NMSUtils.asNMSCopy(fakePlayer.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays != pOverlays){
            pOverlays = overlays;
            fakePlayer.getMetadataAccessor().setOverlays(pOverlays);
            fakePlayer.getMetadataAccessor().merge(false);
            fakePlayer.getTrackers().forEach(p -> fakePlayer.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(((FakePlayer117)fakePlayer).getFake(), getFixedRotation(fakePlayer.getParent().getLocation().getYaw()));
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(fakePlayer.getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(fakePlayer.getParent().getLocation().getYaw()), (byte) 0, true);
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
