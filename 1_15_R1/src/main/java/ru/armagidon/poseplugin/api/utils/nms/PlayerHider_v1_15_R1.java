package ru.armagidon.poseplugin.api.utils.nms;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PlayerHider_v1_15_R1 implements PlayerHider, Listener {

    private final Map<Player, PacketPlayOutEntityMetadata> hiddenPlayers;
    private final Map<Player, Set<PacketPlayOutEntityEquipment>> equipmentPackets;

    private PlayerHider_v1_15_R1() {
        this.hiddenPlayers = Maps.newHashMap();
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
        equipmentPackets = new HashMap<>();
    }

    @Override
    public void hide(Player player) {
        if(hiddenPlayers.containsKey(player)) return;
        EntityPlayer vanilla = (EntityPlayer) NMSUtils.asNMSCopy(player);
        PacketPlayOutEntityMetadata ppoem = new PacketPlayOutEntityMetadata(vanilla.getId(),vanilla.getDataWatcher(), false);
        hiddenPlayers.put(player, ppoem);

        final net.minecraft.server.v1_15_R1.ItemStack air = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));
        Set<PacketPlayOutEntityEquipment> slots = Sets.newHashSet();
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            slots.add(new PacketPlayOutEntityEquipment(player.getEntityId(),slot,air));
        }
        equipmentPackets.put(player, slots);


    }

    @Override
    public void show(Player player) {
        if(!hiddenPlayers.containsKey(player)) return;
        NMSUtils.setInvisible(player, false);
        PacketPlayOutEntityMetadata ppoem = hiddenPlayers.get(player);
        Bukkit.getOnlinePlayers().forEach(p-> NMSUtils.sendPacket(p, ppoem));
        hiddenPlayers.remove(player);
        equipmentPackets.remove(player);
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            PacketPlayOutEntityEquipment equipment = new PacketPlayOutEntityEquipment(player.getEntityId(), slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(player.getEquipment(), slot)));
            Bukkit.getOnlinePlayers().forEach(p->{
                if(!p.getUniqueId().equals(player.getUniqueId())){
                    NMSUtils.sendPacket(p, equipment);
                }
            });
        }
    }

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }

    @Override
    public void tick() {
        hiddenPlayers.forEach(this::accept);
    }

    private org.bukkit.inventory.ItemStack getEquipmentBySlot(EntityEquipment e, EnumItemSlot slot){
        org.bukkit.inventory.ItemStack eq;
        switch (slot){
            case HEAD:
                eq = e.getHelmet();
                break;
            case CHEST:
                eq = e.getChestplate();
                break;
            case LEGS:
                eq = e.getLeggings();
                break;
            case FEET:
                eq = e.getBoots();
                break;
            case OFFHAND:
                eq = e.getItemInOffHand();
                break;
            default:
                eq = e.getItemInMainHand();
        }
        return eq;
    }

    private void accept(Player player, PacketPlayOutEntityMetadata ppoem) {
        NMSUtils.setInvisible(player, true);
        Bukkit.getOnlinePlayers().forEach(p -> {
            NMSUtils.sendPacket(p, ppoem);
            if (!p.getUniqueId().equals(player.getUniqueId())) {
                equipmentPackets.get(player).forEach(packet ->
                        NMSUtils.sendPacket(p, packet));
            }
        });
    }
}
