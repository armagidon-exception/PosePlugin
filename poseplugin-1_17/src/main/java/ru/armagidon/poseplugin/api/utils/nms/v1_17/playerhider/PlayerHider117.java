package ru.armagidon.poseplugin.api.utils.nms.v1_17.playerhider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.playerhider.PlayerHider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ToolPackage(mcVersion = "1.17")
public final class PlayerHider117 extends PlayerHider implements Listener {

    private final Map<Player, Packet<?>[]> hiddenPlayers;

    public PlayerHider117() {
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
        PacketPlayOutEntityMetadata metadata = resetInvisible(player);

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
        return switch (slot) {
            case f -> e.getHelmet();
            case e -> e.getChestplate();
            case d -> e.getLeggings();
            case c -> e.getBoots();
            case b -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
    }

    private PacketPlayOutEntityEquipment resetEquipment(int id, EntityEquipment equipment){
        List<Pair<EnumItemSlot, ItemStack>> slots=
                Arrays.stream(EnumItemSlot.values()).map(slot->Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(equipment, slot)))).collect(Collectors.toList());
        return new PacketPlayOutEntityEquipment(id, slots);
    }

    private PacketPlayOutEntityMetadata resetInvisible(Player player){
        EntityPlayer en = (EntityPlayer) NMSUtils.asNMSCopy(player);
        en.setInvisible(player.hasPotionEffect(PotionEffectType.INVISIBILITY));
        return new PacketPlayOutEntityMetadata(en.getId(), en.getDataWatcher(), true);
    }
}