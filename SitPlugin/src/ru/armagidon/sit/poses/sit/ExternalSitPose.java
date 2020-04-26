package ru.armagidon.sit.poses.sit;

import de.Kurfat.Java.Minecraft.BetterChair.Types.Chair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.armagidon.sit.SitPlugin;

public class ExternalSitPose extends SitPose {

    private final Chair chair;
    public ExternalSitPose(Player player) {
        super(player);
        this.chair = SitPlugin.bridge.createChair(player);
    }

    @Override
    public void standUp(Player player) {
        if(Chair.CACHE_BY_PLAYER.containsKey(player)&&Chair.CACHE_BY_PLAYER.containsValue(chair)) chair.remove();
    }

    @Override
    public void takeASeat(Player player, Location l)
    {
        chair.spawn();
    }
}
