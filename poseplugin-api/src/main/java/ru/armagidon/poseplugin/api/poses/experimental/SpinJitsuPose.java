package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.poses.seatrequiring.ArmorStandSeat;
import ru.armagidon.poseplugin.api.poses.seatrequiring.SeatRequiringPose;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.utils.nms.ToolFactory;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.property.Property;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;

@PoseAvailabilitySince(version = "1.15")
public class SpinJitsuPose extends SeatRequiringPose
{

    private final FakePlayer<?> npc;
    private final Location to;
    private final ArmorStandSeat seat;

    public SpinJitsuPose(Player target) {
        super(target);
        this.npc = ToolFactory.create(FakePlayer.class,new Class[]{Player.class, Pose.class}, target, Pose.SPIN_ATTACK);
        getProperties().registerProperty(EnumPoseOption.DEEP_DIVE.mapper(), new Property<>(npc::isDeepDiveEnabled, npc::setDeepDiveEnabled)).register();
        this.to = target.getLocation().clone();
        this.seat = new ArmorStandSeat(target);
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
        if (npc.isDeepDiveEnabled()) {
            seat.takeASeat(-1);
        }
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
        if (npc.isDeepDiveEnabled()) seat.standUp();
    }

    @PersonalEventHandler
    public final void onMove(PlayerMoveEvent event){
        if (npc.isDeepDiveEnabled()) return;
        if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()) {
            Location t = to.clone().setDirection(getPlayer().getLocation().getDirection());
            t.setYaw(getPlayer().getLocation().getYaw());
            t.setPitch(getPlayer().getLocation().getPitch());
            event.setTo(t);
        }
    }

    @Override
    public void handleTeleport(ArmorStandSeat seat) {
        if (npc.isDeepDiveEnabled())
            super.handleTeleport(seat);
    }

    @Override
    public void handleDismounting(EntityDismountEvent e, ArmorStandSeat seat) {
        if (npc.isDeepDiveEnabled())
            super.handleDismounting(e, seat);
    }

    @Override
    public EnumPose getType() {
        return EnumPose.SPINJITSU;
    }
}
