package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New.NewNPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old.OldNPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityHeadRotation;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerRelEntityMoveLook;
import ru.armagidon.poseplugin.api.utils.versions.VersionControl;

import static ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.FakePlayerUtils.getFixedRotation;


public class NPCSynchronizerProtocolized extends NPCSynchronizer<WrappedDataWatcher>
{

    private final NPCSynchronizer<WrappedDataWatcher> synchronizer;
    private byte pOverlays;

    public NPCSynchronizerProtocolized(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
        this.pOverlays = fakePlayer.getDataWatcher().getByte(16);
        if (VersionControl.getMCVersion() == 1) {
            this.synchronizer = new OldNPCSynchronizer(fakePlayer);
        } else {
            this.synchronizer = new NewNPCSynchronizer(fakePlayer);
        }
    }

    @Override
    public void syncEquipment() {
        synchronizer.syncEquipment();
    }

    public void syncOverlays(){
        byte overlays = WrappedDataWatcher.getEntityWatcher(fakePlayer.getParent()).getByte(16);
        if(overlays != pOverlays){
            pOverlays = overlays;
            fakePlayer.getMetadataAccessor().setOverlays(pOverlays);
            fakePlayer.getMetadataAccessor().merge(false);
            fakePlayer.getTrackers().forEach(p -> fakePlayer.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        ((FakePlayerProtocolized)fakePlayer).getPosition().setYaw(fakePlayer.getParent().getLocation().getYaw());
        WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation();
        rotation.setEntityID(fakePlayer.getId());
        rotation.setHeadYaw(getFixedRotation(((FakePlayerProtocolized)fakePlayer).getPosition().getYaw()));
        WrapperPlayServerRelEntityMoveLook lookPacket = new WrapperPlayServerRelEntityMoveLook();
        lookPacket.setEntityID(fakePlayer.getId());
        lookPacket.setYaw(((FakePlayerProtocolized)fakePlayer).getPosition().getYaw());
        lookPacket.setPitch(((FakePlayerProtocolized)fakePlayer).getPosition().getPitch());
        lookPacket.setOnGround(true);
        fakePlayer.getTrackers().forEach(p -> {
            lookPacket.sendPacket(p);
            rotation.sendPacket(p);
        });
    }

    protected ItemStack getEquipmentBySlot(EntityEquipment e, EnumWrappers.ItemSlot slot){
        return switch (slot) {
            case HEAD -> e.getHelmet();
            case CHEST -> e.getChestplate();
            case LEGS -> e.getLeggings();
            case FEET -> e.getBoots();
            case OFFHAND -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
    }
}
