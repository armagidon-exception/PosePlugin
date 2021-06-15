package ru.armagidon.poseplugin.api.poses.crawl;


import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;

@ToolPackage(mcVersion = "1.17")
public class CrawlHander117 extends CrawlHandler
{

    public CrawlHander117(Player player) {
        super(player);
    }

    @Override
    protected void updateBoundingBox() {

    }

    @Override
    protected void updatePose(Player receiver) {

    }

    @Override
    protected void handleBlockBreak(BlockBreakEvent event) {

    }
}
