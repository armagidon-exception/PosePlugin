package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.misc.ItemBuilder;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.utils.nms.ToolFactory;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.property.Property;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;

public abstract class ExperimentalHandPose extends AbstractPose
{

    private final ItemStack handItem;
    private final FakePlayer<?> npc;
    private final Location to;

    public ExperimentalHandPose(Player target, Material type) {
        super(target);
        this.handItem = addHideTag(ItemBuilder.create(type).asItemStack());
        this.npc = ToolFactory.create(FakePlayer.class, new Class[]{Player.class, Pose.class}, target, Pose.STANDING);

        getProperties().registerProperty(EnumPoseOption.HANDTYPE.mapper(), new Property<>(npc::getActiveHand, npc::setActiveHand));
        getProperties().register();

        this.to = target.getLocation().clone();
    }

    @Override
    public final void initiate() {
        super.initiate();
        npc.getInventory().setPieceOfEquipment(EquipmentSlot.HAND, handItem);
        npc.getNpcSynchronizer().ignoreSlot(EquipmentSlot.HAND);
        npc.getInventory().setItemMapper(new TagRemovingMapper("PosePluginItem"));
        npc.setHeadRotationEnabled(true);
        npc.setSynchronizationEquipmentEnabled(true);
        npc.setSynchronizationOverlaysEnabled(true);
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

    protected static ItemStack addHideTag(ItemStack stack){
        NBTModifier.setString(stack, "PosePluginItem", stack.getType().name());
        return stack;
    }

    @PoseAvailabilitySince(version = "1.15")
    public static class HandShakePose extends ExperimentalHandPose
    {
        public HandShakePose(Player target) {
            super(target, Material.SHIELD);
        }

        @Override
        public EnumPose getType() {
            return EnumPose.HANDSHAKING;
        }
    }

    @PoseAvailabilitySince(version = "1.15")
    public static class ClapPose extends ExperimentalHandPose {

        public ClapPose(Player target) {
            super(target, Material.BOW);
        }

        @Override
        public EnumPose getType() {
            return EnumPose.CLAPPING;
        }
    }

    @PoseAvailabilitySince(version = "1.15")
    public static class WavePose extends ExperimentalHandPose
    {

        public WavePose(Player target) {
            super(target, Material.TRIDENT);
        }

        @Override
        public EnumPose getType() {
            return EnumPose.WAVING;
        }
    }

    @PoseAvailabilitySince(version = "1.17")
    public static class PointPose extends ExperimentalHandPose
    {

        public PointPose(Player target) {
            super(target, Material.SPYGLASS);
        }

        @Override
        public EnumPose getType() {
            return EnumPose.POINTING;
        }
    }
}
