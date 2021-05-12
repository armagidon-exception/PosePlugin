package ru.armagidon.poseplugin.api.poses.swim;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

public class CrawlHandler implements Tickable, PersonalListener
{
    private final Player player;
    private PressingBlock pressingBlock;

    private static Object aabb;
    private final Object packet;


    public CrawlHandler(Player player) {
        pressingBlock = null;
        this.player = player;
        this.packet = NMSUtils.createPosePacket(player, Pose.SWIMMING);
        if (aabb == null)
            aabb = NMSUtils.getSwimmingAABB(player);
    }

    public void enable() {
        PosePluginAPI.getAPI().getPersonalHandlerList().subscribe(PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player), this);
        Block above = getAbove(player.getLocation()).getBlock();

        if (isSlabLike(player.getLocation().getY())) { //Standing on a slab
            //Shulker
            pressingBlock = new PressingBlock.ShulkerPressingBlock(above.getLocation(), player);
        } else {
            //Barrier
            pressingBlock = new PressingBlock.BarrierPressingBlock(above.getLocation(), player);
        }
        pressingBlock.show();
    }

    public void disable() {
        PosePluginAPI.getAPI().getPersonalHandlerList().unsubscribe(PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player), this);
        pressingBlock.hide();
    }

    @PersonalEventHandler
    public void onBreak(BlockBreakEvent event) {
        if (pressingBlock instanceof PressingBlock.BarrierPressingBlock) {
            if (event.getBlock().getLocation().equals(pressingBlock.location)) {
                event.setCancelled(true);
            }
        }
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getZ() != event.getFrom().getZ() || event.getTo().getX() != event.getFrom().getX()) {
            Block above = getAbove(event.getPlayer().getLocation()).getBlock();
            if (isSlabLike(event.getPlayer().getLocation().getY())) { //Standing on a slab
                //Shulker
                if (!(pressingBlock instanceof PressingBlock.ShulkerPressingBlock)) {
                    pressingBlock.hide();
                    pressingBlock = new PressingBlock.ShulkerPressingBlock(above.getLocation(), player);
                    pressingBlock.show();
                }

            } else {
                //Barrier
                if (!(pressingBlock instanceof PressingBlock.BarrierPressingBlock)) {
                    pressingBlock.hide();
                    pressingBlock = new PressingBlock.BarrierPressingBlock(above.getLocation(), player);
                    pressingBlock.show();
                }
            }
            pressingBlock.move(above.getLocation());
        }
    }

    private boolean isSlabLike(double y) {
        int i = (int) y;
        double d = Math.ceil(y);
        return d - i >= 0.5;
    }

    private boolean isFraction(double num) {
        int i = (int) num;
        double d = Math.ceil(num);
        return i < d;
    }

    @Override
    public void tick() {
        Block above = isFraction(player.getLocation().getY()) ?
                player.getLocation().clone().add(0, 2, 0).getBlock() : player.getLocation().getBlock().getRelative(BlockFace.UP);
        if (!above.isSolid()) {
            NMSUtils.setAABB(player, aabb);
        }
        Bukkit.getOnlinePlayers().forEach(p -> NMSUtils.sendPacket(p, packet));
    }

    private Location getAbove(Location location) {
        return location.clone().add(0, (isFraction(location.getY()) ? 2 : 1), 0);
    }
}
