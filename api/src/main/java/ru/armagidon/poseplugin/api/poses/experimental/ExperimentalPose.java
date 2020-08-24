package ru.armagidon.poseplugin.api.poses.experimental;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.HandTypeChangeEvent;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.utils.items.ItemUtil;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.property.Property;

public abstract class ExperimentalPose extends PluginPose
{

    private final ItemStack handItem;
    private final FakePlayer npc;
    private @Getter
    HandType HandType;
    private final Location to;

    public ExperimentalPose(Player target, ItemStack handItem) {
        super(target);
        this.handItem = handItem;
        this.npc = PosePluginAPI.getAPI().getNMSFactory().createFakePlayer(target, Pose.STANDING);

        getProperties().registerProperty("mode",new Property<>(this::getHandType, this::changeMode, this::setMode));
        getProperties().register();

        this.HandType = HandType.RIGHT;
        this.to = target.getLocation().clone();
    }

    @Override
    public final void initiate() {
        super.initiate();
        npc.setHeadRotationEnabled(true);
        npc.setUpdateEquipmentEnabled(false);
        npc.setUpdateOverlaysEnabled(true);
        npc.initiate();
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().hideArmor(getPlayer());
        npc.getCustomEquipmentInterface().setHelmet(getEquipmentBySlot(PlayerArmorChangeEvent.SlotType.HEAD, getPlayer().getEquipment()));
        npc.getCustomEquipmentInterface().setChestPlate(getEquipmentBySlot(PlayerArmorChangeEvent.SlotType.CHEST, getPlayer().getEquipment()));
        npc.getCustomEquipmentInterface().setLeggings(getEquipmentBySlot(PlayerArmorChangeEvent.SlotType.LEGS, getPlayer().getEquipment()));
        npc.getCustomEquipmentInterface().setBoots(getEquipmentBySlot(PlayerArmorChangeEvent.SlotType.FEET, getPlayer().getEquipment()));
    }

    @Override
    public final void play(Player receiver) {
        if(receiver==null){
            npc.broadCastSpawn();
        } else {
            npc.spawnToPlayer(receiver);
        }
        //Requires PosePluginItems resource-pack
        npc.getCustomEquipmentInterface().setItemInMainHand(handItem);
        npc.setActiveHand(getHandType());
    }

    @Override
    public final void stop() {
        super.stop();
        PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().showTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().showArmor(getPlayer());
        npc.remove();
        npc.destroy();
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

    @PersonalEventHandler
    public final void onArmorChange(PlayerArmorChangeEvent event){
        if(event.getNewItem()==null) return;
        ItemUtil util = PosePluginAPI.getAPI().getNMSFactory().createItemUtil(event.getNewItem()).removeTag("PosePluginItem");
        switch (event.getSlotType()){
            case HEAD:
                npc.getCustomEquipmentInterface().setHelmet(util.getSource());
                break;
            case CHEST:
                npc.getCustomEquipmentInterface().setChestPlate(util.getSource());
                break;
            case LEGS:
                npc.getCustomEquipmentInterface().setLeggings(util.getSource());
                break;
            case FEET:
                npc.getCustomEquipmentInterface().setBoots(util.getSource());
                break;
        }
    }

    public final void setMode(HandType mode) {
        this.HandType = mode;
        npc.setActiveHand(mode);
    }

    public final void changeMode(HandType mode){
        if(!new HandTypeChangeEvent(HandType, mode,getPose(), getPosePluginPlayer()).call()) return;
        setMode(mode);
    }

    private ItemStack getEquipmentBySlot(PlayerArmorChangeEvent.SlotType slot, EntityEquipment eq){
        ItemStack out;
        switch (slot){
            case HEAD:
                out =  eq.getHelmet();
                break;
            case CHEST:
                out = eq.getChestplate();
                break;
            case LEGS:
                out = eq.getLeggings();
                break;
            case FEET:
                out = eq.getBoots();
                break;
            default:
                out = new ItemStack(Material.AIR);
                break;
        }
        return out!=null?out:new ItemStack(Material.AIR);
    }

}
