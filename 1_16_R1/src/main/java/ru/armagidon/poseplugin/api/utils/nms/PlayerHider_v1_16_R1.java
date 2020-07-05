package ru.armagidon.poseplugin.api.utils.nms;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.sendPacket;

public final class PlayerHider_v1_16_R1 implements PlayerHider, Listener {

    private final Map<Player, PacketPlayOutEntityMetadata> hiddenPlayers;
    private final Map<Player, PacketPlayOutEntityEquipment> equipmentPackets;

    PlayerHider_v1_16_R1() {
        this.hiddenPlayers = Maps.newHashMap();
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
        equipmentPackets = new HashMap<>();
    }

    @Override
    public void hide(Player player) {
        EntityPlayer vanilla = (EntityPlayer) NMSUtils.asNMSCopy(player);
        hiddenPlayers.put(player, new PacketPlayOutEntityMetadata(vanilla.getId(),vanilla.getDataWatcher(), false));

        final ItemStack air = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));
        List<Pair<EnumItemSlot, ItemStack>> slots = Lists.newArrayList();
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            slots.add(Pair.of(slot, air));
        }
        equipmentPackets.put(player, new PacketPlayOutEntityEquipment(player.getEntityId(), slots));

    }

    @Override
    public void show(Player player) {
        NMSUtils.setInvisible(player, false);
        List<Pair<EnumItemSlot, ItemStack>> slots=
                Arrays.stream(EnumItemSlot.values()).map(slot->Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(player.getEquipment(), slot)))).collect(Collectors.toList());
        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(player.getEntityId(), slots);
        Bukkit.getOnlinePlayers().forEach(p->{
            NMSUtils.sendPacket(p, hiddenPlayers.get(player));
            if(!p.getUniqueId().equals(player.getUniqueId())){
                sendPacket(p,eq);
            }
        } );
        hiddenPlayers.remove(player);
    }

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }

    @Override
    public void tick() {
        hiddenPlayers.forEach((hidden,ppoem)-> {
            PacketPlayOutEntityEquipment eq = equipmentPackets.get(hidden);
            NMSUtils.setInvisible(hidden, true);
            Bukkit.getOnlinePlayers().forEach(p-> {
                NMSUtils.sendPacket(p, ppoem);
                if(!p.getUniqueId().equals(hidden.getUniqueId())){
                    NMSUtils.sendPacket(p, eq);
                }
            });
        });
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
}
