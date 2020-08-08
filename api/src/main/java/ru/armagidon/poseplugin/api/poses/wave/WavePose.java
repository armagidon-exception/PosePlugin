package ru.armagidon.poseplugin.api.poses.wave;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;

public class WavePose extends PluginPose
{

    private final FakePlayer npc;

    public WavePose(Player target) {
        super(target);
        this.npc = PosePluginAPI.getAPI().getNMSFactory().createFakePlayer(target, Pose.STANDING);
    }

    @Override
    public void initiate() {
        super.initiate();
        npc.setHeadRotationEnabled(true);
        npc.setUpdateEquipmentEnabled(false);
        npc.setUpdateOverlaysEnabled(true);
        npc.initiate();
    }

    @Override
    public void play(Player receiver) {
        if(receiver==null){
            npc.broadCastSpawn();
        } else {
            npc.spawnToPlayer(receiver);
        }
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
        //Requires PosePluginItems resource-pack
        ItemStack trident = PosePluginAPI.getAPI().getNMSFactory().createItemUtil(new ItemStack(Material.TRIDENT)).addTag("PosePluginItem","TRIDENT").getSource();
        npc.setItemInMainHand(trident);
        npc.setHandActive(true);
    }

    @Override
    public void stop() {
        super.stop();
        PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().showTag(getPlayer());
        npc.remove();
        npc.destroy();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.WAVING;
    }

    @PersonalEventHandler
    public void onEvent(PlayerMoveEvent event){
        if (event.getTo() != null) {
            if (event.getTo().getX() != event.getFrom().getX() ||event.getTo().getY()!=event.getFrom().getY()|| event.getTo().getZ() != event.getFrom().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }
}
