package ru.armagidon.poseplugin.api.utils.nms.protocolized.playerhider;

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
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils;
import ru.armagidon.poseplugin.api.utils.nms.playerhider.PlayerHider;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.AbstractPacket;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityMetadata;

import java.util.HashMap;


public class NewPlayerHiderProtocolized extends PlayerHider
{

    private final HashMap<Player, AbstractPacket[]> hiddenPlayers;

    public NewPlayerHiderProtocolized() {
        this.hiddenPlayers = Maps.newHashMap();
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
    }

    @Override
    public void tick() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            hiddenPlayers.forEach((hidden, packets) -> {
                if (!p.equals(hidden)) {
                    packets[1].sendPacket(p);
                }
                packets[0].sendPacket(p);
            });
        });
    }

    @Override
    public void hide(Player player) {
        if(hiddenPlayers.containsKey(player))
            return;

        AbstractPacket[] packets = new AbstractPacket[2];

        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(player).deepClone();
        watcher.setObject(0, WrappedDataWatcher.Registry.get(Byte.class), FakePlayerUtils.setBit(watcher.getByte(0), 5, true), true);
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata();
        metadata.setEntityID(player.getEntityId());
        metadata.setMetadata(watcher.getWatchableObjects());

        Bukkit.getOnlinePlayers().forEach(online->{
            if(!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                metadata.sendPacket(online);
        });

        packets[0] = metadata;

        WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment();
        equipment.setEntityID(player.getEntityId());
        for (EnumWrappers.ItemSlot slot : EnumWrappers.ItemSlot.values()) {
            equipment.setSlotStackPair(slot, new ItemStack(Material.AIR));
        }

        packets[1] = equipment;


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

    private org.bukkit.inventory.ItemStack getEquipmentBySlot(EntityEquipment e, EnumWrappers.ItemSlot slot){
        return switch (slot) {
            case HEAD -> e.getHelmet();
            case CHEST -> e.getChestplate();
            case LEGS -> e.getLeggings();
            case FEET -> e.getBoots();
            case OFFHAND -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
    }

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }
}
