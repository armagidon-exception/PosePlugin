package ru.armagidon.poseplugin.api.utils.npc.protocolized.Old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.PacketContainer;
import ru.armagidon.poseplugin.api.utils.playerhider.PlayerHider;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityEquipment;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityMetadata;

import java.util.Arrays;
import java.util.HashMap;

public class OldPlayerHiderProtocolized extends PlayerHider
{

    private final HashMap<Player, PacketContainer<?>[]> hiddenPlayers;

    public OldPlayerHiderProtocolized() {
        this.hiddenPlayers = Maps.newHashMap();
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
    }

    @Override
    public void tick() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            hiddenPlayers.forEach((hidden, packets) -> {
                if (!p.equals(hidden)) {
                    packets[1].send(p);
                }
                packets[0].send(p);
            });
        });
    }

    @Override
    public void hide(Player player) {
        if(hiddenPlayers.containsKey(player))
            return;

        PacketContainer<?>[] packets = new PacketContainer[2];

        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(player).deepClone();
        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), FakePlayerUtils.setBit(watcher.getByte(0), 5, true), true);
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata();
        metadata.setEntityID(player.getEntityId());
        metadata.setMetadata(watcher.getWatchableObjects());

        Bukkit.getOnlinePlayers().forEach(online->{
            if(!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                metadata.sendPacket(online);
        });

        packets[0] = new PacketContainer<>(metadata);

        packets[1] = new PacketContainer<>(Arrays.stream(EnumWrappers.ItemSlot.values()).map(slot -> {
            WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment();
            equipment.setEntityID(player.getEntityId());
            equipment.setSlot(slot);
            equipment.setItem(new ItemStack(Material.AIR));
            return equipment;
        }).toArray(WrapperPlayServerEntityEquipment[]::new));


        hiddenPlayers.put(player, packets);
    }

    @Override
    public void show(Player player) {
        if(!hiddenPlayers.containsKey(player)){
            return;
        }
        hiddenPlayers.remove(player);
        WrapperPlayServerEntityMetadata metadata = resetInvisible(player);
        WrapperPlayServerEntityEquipment eq = resetEquipment(player);

        Bukkit.getOnlinePlayers().forEach(online -> {
            if(!online.getUniqueId().equals(player.getUniqueId())){
                eq.sendPacket(online);
            }
            metadata.sendPacket(online);
        });
    }

    private WrapperPlayServerEntityMetadata resetInvisible(Player player){
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(player).deepClone();
        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), FakePlayerUtils.setBit(watcher.getByte(0), 5, false), true);
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata();
        metadata.setEntityID(player.getEntityId());
        metadata.setMetadata(watcher.getWatchableObjects());
        return metadata;
    }

    private WrapperPlayServerEntityEquipment resetEquipment(Player player) {
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment();
        packet.setEntityID(player.getEntityId());
        for (EnumWrappers.ItemSlot slot : EnumWrappers.ItemSlot.values()) {
            packet.setSlotStackPair(slot, getEquipmentBySlot(player.getEquipment(), slot));
        }
        return packet;
    }

    private ItemStack getEquipmentBySlot(EntityEquipment e, EnumWrappers.ItemSlot slot){
        ItemStack eq;
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

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }
}
