package ru.armagidon.poseplugin.api.utils.playerhider.v1_16_R3;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.playerhider.PlayerHider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PlayerHiderImpl extends PlayerHider implements Listener {

    private final Map<Player, Packet<?>[]> hiddenPlayers;

    public PlayerHiderImpl() {
        this.hiddenPlayers = Maps.newHashMap();
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
    }

    @Override
    public void hide(Player player) {
        if(hiddenPlayers.containsKey(player)) {
            return;
        }

        Packet<?>[] packets = new Packet[2];

        EntityPlayer vanilla = (EntityPlayer) NMSUtils.asNMSCopy(player);
        vanilla.setInvisible(true);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(vanilla.getId(),vanilla.getDataWatcher(), true);

        packets[0] = metadata;

        final ItemStack air = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));
        List<Pair<EnumItemSlot, ItemStack>> slots = Lists.newArrayList();
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            slots.add(Pair.of(slot, air));
        }
        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(player.getEntityId(), slots);

        packets[1] = eq;

        Bukkit.getOnlinePlayers().forEach(online->{
            NMSUtils.sendPacket(online, metadata);
            if(!online.getUniqueId().equals(player.getUniqueId())){
                NMSUtils.sendPacket(online, eq);
            }
        });
        hiddenPlayers.put(player, packets);
    }

    @Override
    public void show(Player player) {
        if(!hiddenPlayers.containsKey(player)){
            return;
        }
        EntityPlayer en = (EntityPlayer) NMSUtils.asNMSCopy(player);


        PacketPlayOutEntityEquipment eq = resetEquipment(en.getId(), player.getEquipment());
        PacketPlayOutEntityMetadata metadata = resetInvisible(en);

        Bukkit.getOnlinePlayers().forEach(online->{
            NMSUtils.sendPacket(online, metadata);
            if(!online.getUniqueId().equals(player.getUniqueId())){
                NMSUtils.sendPacket(online, eq);
            }
        });
        hiddenPlayers.remove(player);
    }

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }

    @Override
    public void tick() {
        hiddenPlayers.forEach((hidden,packets) ->
                Bukkit.getOnlinePlayers().forEach(online->{
            if(!hidden.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                NMSUtils.sendPacket(online, packets[0]);
            }
            if(!online.getUniqueId().equals(hidden.getUniqueId())){
                NMSUtils.sendPacket(online, packets[1]);
            }
        }));
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

    private PacketPlayOutEntityEquipment resetEquipment(int id, EntityEquipment equipment){
        List<Pair<EnumItemSlot, ItemStack>> slots=
                Arrays.stream(EnumItemSlot.values()).map(slot->Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(equipment, slot)))).collect(Collectors.toList());
        return new PacketPlayOutEntityEquipment(id, slots);
    }

    private PacketPlayOutEntityMetadata resetInvisible(EntityPlayer en){
        en.setInvisible(en.hasEffect(MobEffects.INVISIBILITY));
        return new PacketPlayOutEntityMetadata(en.getId(), en.getDataWatcher(), true);
    }
}
