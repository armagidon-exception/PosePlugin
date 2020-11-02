package ru.armagidon.poseplugin.api.utils.playerhider.v1_15_R1;

import com.google.common.collect.Maps;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.util.PacketContainer;
import ru.armagidon.poseplugin.api.utils.playerhider.PlayerHider;

import java.util.Arrays;
import java.util.Map;

public final class PlayerHiderImpl extends PlayerHider implements Listener {

    private final Map<Player, PacketContainer<?>[]> hiddenPlayers;

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

        PacketContainer<?>[] packets = new PacketContainer[2];

        EntityPlayer vanilla = (EntityPlayer) NMSUtils.asNMSCopy(player);
        vanilla.setInvisible(true);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(vanilla.getId(),vanilla.getDataWatcher(), true);

        packets[0] = new PacketContainer<>(metadata);

        final ItemStack air = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));

        PacketPlayOutEntityEquipment[] eq = Arrays.stream(EnumItemSlot.values()).
                map(slot->new PacketPlayOutEntityEquipment(vanilla.getId(),slot,air)).
                toArray(PacketPlayOutEntityEquipment[]::new);

        packets[1] = new PacketContainer<>(eq);

        Bukkit.getOnlinePlayers().forEach(online->{
            if(!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                packets[0].send(online);
            if(!online.getUniqueId().equals(player.getUniqueId())){
                packets[1].send(online);
            }
        });
        hiddenPlayers.put(player, packets);
    }

    @Override
    public void show(Player player) {
        if(!hiddenPlayers.containsKey(player)){
            return;
        }
        hiddenPlayers.remove(player);
        EntityPlayer en = (EntityPlayer) NMSUtils.asNMSCopy(player);
        PacketContainer<PacketPlayOutEntityEquipment> eq = resetEquipment(en.getId(), player.getEquipment());
        PacketPlayOutEntityMetadata metadata = resetInvisible(en);

        Bukkit.getOnlinePlayers().forEach(online->{
            if(!online.getUniqueId().equals(player.getUniqueId())){
                eq.send(online);
            }
        });
        Bukkit.getOnlinePlayers().forEach(online->NMSUtils.sendPacket(online,metadata));
    }

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }

    @Override
    public void tick() {
        hiddenPlayers.forEach((hidden,packets)-> Bukkit.getOnlinePlayers().forEach(online->{
            if(!hidden.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                packets[0].send(online);
            }
            if(!online.getUniqueId().equals(hidden.getUniqueId())){
                packets[1].send(online);
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

    private PacketContainer<PacketPlayOutEntityEquipment> resetEquipment(int id, EntityEquipment equipment){
        PacketPlayOutEntityEquipment[] eq = Arrays.stream(EnumItemSlot.values()).
                map(slot->new PacketPlayOutEntityEquipment(id, slot, CraftItemStack.asNMSCopy( getEquipmentBySlot(equipment, slot) ) ) ).
                toArray(PacketPlayOutEntityEquipment[]::new);
        return new PacketContainer<>(eq);
    }

    private PacketPlayOutEntityMetadata resetInvisible(EntityPlayer en){
        en.setInvisible(false);
        return new PacketPlayOutEntityMetadata(en.getId(), en.getDataWatcher(), true);
    }
}
