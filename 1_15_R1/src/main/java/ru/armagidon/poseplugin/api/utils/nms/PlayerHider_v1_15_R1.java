package ru.armagidon.poseplugin.api.utils.nms;

import com.google.common.collect.Maps;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerHider_v1_15_R1 implements PlayerHider {

    private final Map<Player, PacketPlayOutEntityMetadata> hiddenPlayers;

    PlayerHider_v1_15_R1() {
        this.hiddenPlayers = Maps.newHashMap();
    }

    @Override
    public void hide(Player player) {
        EntityPlayer vanilla = (EntityPlayer) NMSUtils.asNMSCopy(player);
        PacketPlayOutEntityMetadata ppoem = new PacketPlayOutEntityMetadata(vanilla.getId(),vanilla.getDataWatcher(), false);
        hiddenPlayers.put(player, ppoem);
    }

    @Override
    public void show(Player player) {
        NMSUtils.setInvisible(player, false);
        PacketPlayOutEntityMetadata ppoem = hiddenPlayers.get(player);
        Bukkit.getOnlinePlayers().forEach(p-> NMSUtils.sendPacket(p, ppoem));
        hiddenPlayers.remove(player);
    }

    @Override
    public boolean isHidden(Player player) {
        return hiddenPlayers.containsKey(player);
    }

    @Override
    public void tick() {
        hiddenPlayers.forEach((player, ppoem)-> {
            NMSUtils.setInvisible(player, true);
            Bukkit.getOnlinePlayers().forEach(p-> NMSUtils.sendPacket(p, ppoem));
            Bukkit.getOnlinePlayers().stream().filter(p->!p.getUniqueId().equals(player.getUniqueId())).forEach(p->{
                for (EnumItemSlot slot:EnumItemSlot.values()) {
                    NMSUtils.sendPacket(p, new PacketPlayOutEntityEquipment(player.getEntityId(), slot, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR))));
                }
            });
        });
    }
}
