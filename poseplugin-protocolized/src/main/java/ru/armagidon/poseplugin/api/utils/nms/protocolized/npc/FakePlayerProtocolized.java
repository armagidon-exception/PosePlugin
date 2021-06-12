package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@ToolPackage(mcVersion = "protocolized")
public class FakePlayerProtocolized extends FakePlayer<WrappedDataWatcher>
{

    private static final Random RANDOM = new Random();

    @Getter private final int id;

    @Getter private final WrappedDataWatcher dataWatcher;

    private final WrapperPlayServerEntityDestroy destroy;
    private WrapperPlayServerNamedEntitySpawn spawner;
    private final WrapperPlayServerPlayerInfo addInfo;
    private final WrapperPlayServerRelEntityMoveLook movePacket;
    private WrapperPlayServerBlockChange fakeBedPacket;



    public FakePlayerProtocolized(Player parent, Pose pose) {
        super(parent, pose);

        this.id = RANDOM.nextInt(9999);

        //Get Location of fake bed
        this.bedLoc = FakePlayerUtils.toBedLocation(parent.getLocation());
        //Cache original type of block
        this.cache = new BlockCache(bedLoc.getBlock().getBlockData(), bedLoc);

        //Create single instances of packet for optimisation purposes. So server won't need to create tons of copies of the same packet.

        //Create instance of move packet to pop up npc a little
        this.movePacket = new WrapperPlayServerRelEntityMoveLook();
        movePacket.setEntityID(id);
        movePacket.setDy(0.1D);
        movePacket.setOnGround(true);

        //Create packet instance of fake bed(could've used sendBlockChange but im crazy and it will recreate copies of the same packet)
        this.fakeBedPacket = bedPacket(parent.getLocation().getYaw());
        //Create packet instance of NPC 's data
        this.addInfo = infoPacket();

        //Create instance of npc
        this.spawner = spawnerPacket(parent, id);

        //Create data watcher to modify entity metadata


        this.dataWatcher = WrappedDataWatcher.getEntityWatcher(parent).deepClone();
        //Create instance of the packet with this data

        metadataAccessor = new MetadataEditorProtocolized(this);
        npcUpdater = new NPCSynchronizedProtocolized(this);
        customEquipmentManager = new NPCInventoryProtocolized(this);

        //Set metadata
        setMetadata();

        this.destroy = new WrapperPlayServerEntityDestroy();
        destroy.setEntityIds(id);

    }

    private void setMetadata(){
        //Save current overlay bit mask
        byte overlays = getDataWatcher().getByte(16);
        //Set pose to the NPC
        metadataAccessor.setPose(pose);
        //Set current overlays to the NPC
        metadataAccessor.setOverlays(overlays);
        //Set BedLocation to NPC if its pose is SLEEPING
        if(metadataAccessor.getPose().equals(Pose.SLEEPING))
            metadataAccessor.setBedPosition(bedLoc);
        metadataAccessor.merge(true);

    }

    private WrapperPlayServerNamedEntitySpawn spawnerPacket(Player parent, int id) {
        WrapperPlayServerNamedEntitySpawn spawn = new WrapperPlayServerNamedEntitySpawn();
        spawn.setEntityID(id);
        spawn.setX(parent.getLocation().getX());
        spawn.setY(parent.getLocation().getY());
        spawn.setZ(parent.getLocation().getZ());
        spawn.setPlayerUUID(parent.getUniqueId());
        return spawn;
    }

    private WrapperPlayServerBlockChange bedPacket(float angle) {
        WrapperPlayServerBlockChange fakeBedPacket = new WrapperPlayServerBlockChange();
        fakeBedPacket.setLocation(new BlockPosition(parent.getLocation().clone().toVector().setY(0)));
        Bed bed = (Bed) Bukkit.createBlockData(Material.WHITE_BED);
        bed.setPart(Bed.Part.HEAD);
        bed.setFacing(BlockPositionUtils.yawToFace(angle).getOppositeFace());
        fakeBedPacket.setBlockData(WrappedBlockData.createData(bed));
        return fakeBedPacket;
    }

    private WrapperPlayServerPlayerInfo infoPacket() {
        WrapperPlayServerPlayerInfo addInfo = new WrapperPlayServerPlayerInfo();
        addInfo.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        List<PlayerInfoData> dataList = new ArrayList<>();
        dataList.add(new PlayerInfoData(new WrappedGameProfile(parent.getUniqueId(), parent.getName()), 1, EnumWrappers.NativeGameMode.NOT_SET, WrappedChatComponent.fromText("")));
        addInfo.setData(dataList);
        return addInfo;
    }

    @Override
    public void tick() {
        //Get players nearby
        Set<Player> detectedPlayers = BlockPositionUtils.getNear(getViewDistance(), parent);

        //Check if some of them aren't trackers
        for (Player detectedPlayer : detectedPlayers) {
            if(!this.trackers.contains(detectedPlayer)){
                spawnToPlayer(detectedPlayer);
                trackers.add(detectedPlayer);
            }
        }
        //Check if some of trackers aren't in view distance
        for (Player tracker : this.trackers) {
            if(!detectedPlayers.contains(tracker)){
                removeToPlayer(tracker);
                trackers.remove(tracker);
            }
        }

        if(isSynchronizationEquipmentEnabled()) updateEquipment();
        if(isSynchronizationOverlaysEnabled()) updateOverlays();
        if(isHeadRotationEnabled()) updateHeadRotation();

        trackers.forEach(fakeBedPacket::sendPacket);
    }

    @Override
    public void broadCastSpawn() {
        Set<Player> detectedPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> p.getWorld().equals(parent.getWorld()))
                .filter(p -> p.getLocation().distanceSquared(parent.getLocation()) <= Math.pow(viewDistance, 2)).collect(Collectors.toSet());
        trackers.addAll(detectedPlayers);
        //Bukkit.getOnlinePlayers().forEach(addInfo::sendPacket);
        trackers.forEach(this::spawnToPlayer);
    }

    @Override
    public void spawnToPlayer(Player player) {
        spawner.sendPacket(player);
        fakeBedPacket.sendPacket(player);
        customEquipmentManager.showEquipment(player);
        metadataAccessor.showPlayer(player);
        if(metadataAccessor.getPose().equals(Pose.SLEEPING))
            movePacket.sendPacket(player);
        if(isHeadRotationEnabled()) {
            setHeadRotationEnabled(false);
            PosePluginAPI.getAPI().getTickManager().later(() ->
                    setHeadRotationEnabled(true), 10);
        }
    }

    @Override
    public void removeToPlayer(Player player) {
        destroy.sendPacket(player);
        cache.restore(player);
    }

    public void swingHand(boolean mainHand) {
        if(isSwingAnimationEnabled()) {
            WrapperPlayServerAnimation animation = new WrapperPlayServerAnimation();
            animation.setEntityID(id);
            animation.setAnimation(mainHand ? 0 : 3);
            trackers.forEach(animation::sendPacket);
        }
    }

    public void animation(byte id){
        WrapperPlayServerEntityStatus status = new WrapperPlayServerEntityStatus();
        status.setEntityID(id);
        status.setEntityStatus(id);
        trackers.forEach(status::sendPacket);
    }

    @Override
    public void teleport(Location destination) {
        getTrackers().forEach(t -> cache.restore(t));
        Location bedLoc = FakePlayerUtils.toBedLocation(destination);

        cache.setLocation(bedLoc);
        cache.setData(bedLoc.getBlock().getBlockData());

        fakeBedPacket = bedPacket(parent.getLocation().getYaw());

        this.bedLoc.setX(destination.getX());
        this.bedLoc.setY(destination.getY());
        this.bedLoc.setZ(destination.getZ());


        getMetadataAccessor().setBedPosition(bedLoc);

        getMetadataAccessor().merge(true);

        spawner = spawnerPacket(parent, id);

        updateNPC();

        getTrackers().forEach(movePacket::sendPacket);

    }

    private void updateOverlays() {
        npcUpdater.syncOverlays();
    }

    private void updateHeadRotation() {
        npcUpdater.syncHeadRotation();
    }

    private void updateEquipment(){
        npcUpdater.syncEquipment();
    }
}
