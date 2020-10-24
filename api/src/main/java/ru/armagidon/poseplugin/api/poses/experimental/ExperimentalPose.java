package ru.armagidon.poseplugin.api.poses.experimental;

import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.armagidonapi.itemutils.ItemBuilder;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.npc.HandType;
import ru.armagidon.poseplugin.api.utils.property.Property;

public abstract class ExperimentalPose extends AbstractPose
{

    private final ItemStack handItem;
    private final FakePlayer npc;
    private @Getter HandType handType;
    private final Location to;

    public ExperimentalPose(Player target, Material type) {
        super(target);
        this.handItem = addHideTag(ItemBuilder.create(type).asItemStack());
        this.npc = FakePlayer.createNew(target, Pose.STANDING);

        getProperties().registerProperty("handtype",new Property<>(this::getHandType, this::setMode));
        getProperties().register();

        this.handType = HandType.RIGHT;
        this.to = target.getLocation().clone();

    }

    @Override
    public final void initiate() {
        super.initiate();
        npc.setHeadRotationEnabled(true);
        npc.setSynchronizationEquipmentEnabled(false);
        npc.setSynchronizationOverlaysEnabled(true);
        npc.initiate();
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().hideArmor(getPlayer());
        for (PlayerArmorChangeEvent.SlotType value : PlayerArmorChangeEvent.SlotType.values()) {
            updateNPCsArmor(value, getEquipmentBySlot(value, getPlayer().getEquipment()));
        }
    }

    @Override
    public final void play(Player receiver) {
        if(receiver==null){
            npc.broadCastSpawn();
        } else {
            npc.spawnToPlayer(receiver);
        }
        //Requires PosePluginItems resource-pack
        npc.getCustomEquipmentManager().setPieceOfEquipment(EquipmentSlot.HAND, handItem);
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

        PosePluginAPI.pluginTagClear.pushThrough(event.getNewItem());

        npc.getCustomEquipmentManager().setPieceOfEquipment(EquipmentSlot.valueOf(event.getSlotType().name()), event.getNewItem());
    }

    public final void setMode(HandType mode) {
        this.handType = mode;
        npc.setActiveHand(mode);
    }

    private ItemStack getEquipmentBySlot(PlayerArmorChangeEvent.SlotType slot, EntityEquipment eq){
        ItemStack out;
        switch (slot){
            case HEAD:
                out = eq.getHelmet();
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
        return out !=null ? out : new ItemStack(Material.AIR);
    }

    private void updateNPCsArmor(PlayerArmorChangeEvent.SlotType slotType, ItemStack stack){
        npc.getCustomEquipmentManager().setPieceOfEquipment(EquipmentSlot.valueOf(slotType.name()), stack);
    }

    protected static ItemStack addHideTag(ItemStack stack){
        NBTItem item = new NBTItem(stack, true);
        item.setString("PosePluginItem", stack.getType().name());
        return item.getItem();
    }

}
