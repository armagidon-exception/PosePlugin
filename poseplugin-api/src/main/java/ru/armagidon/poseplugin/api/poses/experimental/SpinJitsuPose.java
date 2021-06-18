package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.utils.nms.ToolFactory;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;

@PoseAvailabilitySince(version = "1.15")
public class SpinJitsuPose extends AbstractPose
{

    private final FakePlayer<?> npc;
    private final Location to;

    public SpinJitsuPose(Player target) {
        super(target);
        this.npc = ToolFactory.create(FakePlayer.class,new Class[]{Player.class, Pose.class}, target, Pose.SPIN_ATTACK);
        getProperties().register();
        this.to = target.getLocation().clone();
    }

    @Override
    public final void initiate() {
        super.initiate();
        npc.setHeadRotationEnabled(false);
        npc.setSynchronizationEquipmentEnabled(true);
        npc.setSynchronizationOverlaysEnabled(true);
        npc.getInventory().setItemMapper(new TagRemovingMapper("PosePluginItem"));
        Location l = to.clone();
        l.setPitch(-90f);
        npc.setLocationRotation(l);
        npc.initiate();
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().hideArmor(getPlayer());
    }

    @Override
    public final void play(Player receiver) {
        if(receiver == null){
            npc.broadCastSpawn();
        } else {
            npc.spawnToPlayer(receiver);
        }
    }

    @Override
    public final void stop() {
        super.stop();
        PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().showTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().showArmor(getPlayer());
        npc.remove();
        npc.dispose();
    }

    @PersonalEventHandler
    public final void onMove(PlayerMoveEvent event){
        if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()) {
            Location t = to.clone().setDirection(getPlayer().getLocation().getDirection());
            t.setYaw(getPlayer().getLocation().getYaw());
            t.setPitch(getPlayer().getLocation().getPitch());
            event.setTo(t);
        }
    }

    private ItemStack getEquipmentBySlot(PlayerArmorChangeEvent.SlotType slot, EntityEquipment eq){
        ItemStack out = switch (slot) {
            case HEAD -> eq.getHelmet();
            case CHEST -> eq.getChestplate();
            case LEGS -> eq.getLeggings();
            case FEET -> eq.getBoots();
        };
        return out != null ? out : new ItemStack(Material.AIR);
    }

    @Override
    public EnumPose getType() {
        return EnumPose.SPINJITSU;
    }
}
