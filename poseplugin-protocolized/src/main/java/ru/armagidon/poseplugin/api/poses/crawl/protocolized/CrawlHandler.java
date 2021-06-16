package ru.armagidon.poseplugin.api.poses.crawl.protocolized;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.poses.crawl.PressingBlock;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;


@ToolPackage(mcVersion = "protocolized")
public class CrawlHandler extends ru.armagidon.poseplugin.api.poses.crawl.CrawlHandler
{

    private static Object aabb;
    private final Object packet;


    public CrawlHandler(Player player) {
        super(player);
        this.packet = NMSUtils.createPosePacket(player, Pose.SWIMMING);
        if (aabb == null)
            aabb = NMSUtils.getSwimmingAABB(player);
    }

    @Override
    protected void updateBoundingBox() {
        NMSUtils.setAABB(getPlayer(), aabb);
    }

    @Override
    protected void updatePose(Player receiver) {
        NMSUtils.sendPacket(receiver, packet);
    }

    @Override
    protected PressingBlock createPressingBlock(Location above, boolean isSlab) {
        if (!isSlab) {
            return new PressingBlock.BarrierPressingBlock(above, getPlayer());
        } else {
            return new PressingBlock.ShulkerPressingBlock(above, getPlayer());
        }
    }
}
