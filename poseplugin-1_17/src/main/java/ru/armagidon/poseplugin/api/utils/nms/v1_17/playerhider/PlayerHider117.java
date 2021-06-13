package ru.armagidon.poseplugin.api.utils.nms.v1_17.playerhider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
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

        ServerPlayer vanilla = (ServerPlayer) NMSUtils.asNMSCopy(player);
        vanilla.setInvisible(true);
        ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(vanilla.getId(),vanilla.getEntityData(), true);

        packets[0] = metadata;

        final ItemStack air = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));
        List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> slots = Lists.newArrayList();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            slots.add(Pair.of(slot, air));
        }
        ClientboundSetEquipmentPacket eq = new ClientboundSetEquipmentPacket(player.getEntityId(), slots);

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
        ServerPlayer en = (ServerPlayer) NMSUtils.asNMSCopy(player);


        ClientboundSetEquipmentPacket eq = resetEquipment(en.getId(), player.getEquipment());
        ClientboundSetEntityDataPacket metadata = resetInvisible(player);

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
    private org.bukkit.inventory.ItemStack getEquipmentBySlot(EntityEquipment e, EquipmentSlot slot){
        return switch (slot) {
            case HEAD -> e.getHelmet();
            case CHEST -> e.getChestplate();
            case LEGS -> e.getLeggings();
            case FEET -> e.getBoots();
            case OFFHAND -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
    }

    private ClientboundSetEquipmentPacket resetEquipment(int id, EntityEquipment equipment){
        List<Pair<EquipmentSlot, ItemStack>> slots=
                Arrays.stream(EquipmentSlot.values()).map(slot->Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(equipment, slot)))).collect(Collectors.toList());
        return new ClientboundSetEquipmentPacket(id, slots);
    }

    private ClientboundSetEntityDataPacket resetInvisible(Player player){
        ServerPlayer en = (ServerPlayer) NMSUtils.asNMSCopy(player);
        en.setInvisible(player.hasPotionEffect(PotionEffectType.INVISIBILITY));
        return new ClientboundSetEntityDataPacket(en.getId(), en.getEntityData(), true);
    }
}
