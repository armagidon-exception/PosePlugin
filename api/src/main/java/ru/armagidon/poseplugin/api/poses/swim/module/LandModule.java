package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LandModule extends SwimModule {

    //Some data
    private final AtomicReference<Boolean> _static;
    private final BlockCache cache;
    private boolean under;
    private Location previous;

    //Packets
    private final Object swimPacket;

    public LandModule(PosePluginPlayer target, AtomicReference<Boolean> _static) {
        super(target);
        Block above = getAbove(target.getHandle().getLocation()).getBlock();
        this.cache = new BlockCache(above.getBlockData(), above.getLocation());
        this._static = _static;
        NMSUtils.setPlayerPose(target.getHandle(), Pose.SWIMMING);
        this.swimPacket = NMSUtils.createPosePacket(target.getHandle(), false);
    }

    @Override
    public void play() {
        Block above = getAbove(getTarget().getHandle().getLocation()).getBlock();
        if(!above.getType().isSolid())
            getTarget().getHandle().sendBlockChange(above.getLocation(), Material.BARRIER.createBlockData());
    }

    @Override
    public void stop() {
        NMSUtils.setPlayerPose(getTarget().getHandle(), Pose.SNEAKING);
        Object resetPacket = NMSUtils.createPosePacket(getTarget().getHandle(), true);
        getReceivers().forEach(p->NMSUtils.sendPacket(p, resetPacket));
        cache.restore(getTarget().getHandle());
        HandlerList.unregisterAll(this);
        PosePluginAPI.getAPI().getPersonalHandlerList().unsubscribe(getTarget(), this);
    }

    @Override
    public void tick() {
        getReceivers().forEach(p->NMSUtils.sendPacket(p, swimPacket));
        Block above = getAbove(getTarget().getHandle().getLocation()).getBlock();
        if(previous!=null&&compareLocations(previous, getTarget().getHandle().getLocation())) {
            if (!above.getType().isSolid() || IsUnSolidBlock(above.getBlockData()))
                getTarget().getHandle().sendBlockChange(above.getLocation(), Material.BARRIER.createBlockData());
            else cache.restore(getTarget().getHandle());
        }

        if(canGoUnderBlock(getTarget().getHandle())){
            under = true;
            getTarget().getHandle().setGliding(true);
        } else {
            if(under){
                getTarget().getHandle().setGliding(false);
                under=false;
            }
        }
    }

    private boolean canGoUnderBlock(Player target){
        Block dir = VectorUtils.getDirBlock(getTarget().getHandle().getLocation());
        Location l = dir.getLocation().clone().add(.5,0,.5);
        Block aboveDir = dir.getRelative(BlockFace.UP);
        boolean dirIsUnSolid =!dir.getType().isSolid();
        boolean aboveDirIsSolid = aboveDir.getType().isSolid();
        boolean onTheEdge = getTarget().getHandle().getLocation().distance(l)<=0.9;
        boolean aboveIsUnSolid = !getAbove(target.getLocation()).getBlock().getType().isSolid();
        return dirIsUnSolid && aboveDirIsSolid && onTheEdge && aboveIsUnSolid;
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent event){
        if(!_static.get()) {
            boolean compare = compareLocations(event.getFrom(), getTarget().getHandle().getLocation());
            if (!compare) {
                cache.restore(getTarget().getHandle());
                Block above = getAbove(getTarget().getHandle().getLocation()).getBlock();
                cache.setData(above.getBlockData());
                cache.setLocation(above.getLocation());
                if (!above.getType().isSolid() || IsUnSolidBlock(above.getBlockData()))
                    getTarget().getHandle().sendBlockChange(above.getLocation(), Material.BARRIER.createBlockData());
            }
            previous=event.getTo();
        } else {
            if(event.getFrom().getX()!=event.getTo().getX()||event.getFrom().getY()!=event.getTo().getY()||event.getFrom().getZ()!=event.getTo().getZ())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event){
        if(event.getEntity().equals(getTarget().getHandle())){
            if(!event.isGliding()&&under){
                event.setCancelled(true);
            } else if(!under&&event.isGliding()){
                PluginPose.callStopEvent(EnumPose.SWIMMING, getTarget(), StopAnimationEvent.StopCause.STOPPED);
            }
        }
    }

    public Location getAbove(Location source){
        return source.clone().add(0,1,0);
    }

    public boolean compareLocations(Location first, Location second){
        Location f = VectorUtils.getRoundedBlock(first).getLocation();
        Location s = VectorUtils.getRoundedBlock(second).getLocation();
        return (f.getX()==s.getX()&&f.getY()==s.getY()&&f.getX()==s.getZ());
    }

    private boolean isWaterLogged(BlockData data){
        if(data instanceof Waterlogged){
            if(data instanceof Slab){
                Slab slab = (Slab) data;
                return slab.getType().equals(Slab.Type.TOP);
            } else if(data instanceof TrapDoor){
                TrapDoor trapDoor = (TrapDoor) data;
                return trapDoor.getHalf().equals(Bisected.Half.TOP)||trapDoor.isOpen();
            }
            return true;
        }
        return false;
    }

    private boolean IsUnSolidBlock(BlockData data){
        if(isWaterLogged(data)) return true;
        else if(Tag.PORTALS.isTagged(data.getMaterial())) return true;
        else if(Tag.BANNERS.isTagged(data.getMaterial())) return true;
        else if(Tag.FENCE_GATES.isTagged(data.getMaterial())) return true;
        else if (Tag.DOORS.isTagged(data.getMaterial())) return true;
        else if(Tag.TALL_FLOWERS.isTagged(data.getMaterial())) return true;
        return false;
    }

    @Override
    public SwimPose.SwimMode getMode() {
        return SwimPose.SwimMode.CRAWLING;
    }

    public Set<Player> getReceivers(){
        return Bukkit.getOnlinePlayers().stream().filter(p->!p.getUniqueId().equals(getTarget().getHandle().getUniqueId())).collect(Collectors.toSet());
    }
}
