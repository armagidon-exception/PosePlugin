package ru.armagidon.sit.utils;

import de.Kurfat.Java.Minecraft.BetterChair.PlayerSitEvent;
import de.Kurfat.Java.Minecraft.BetterChair.Types.Chair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.SitPose;
import ru.armagidon.sit.poses.StandingPose;

import java.util.Map;

import static ru.armagidon.sit.utils.Listener.containsPlayer;
import static ru.armagidon.sit.utils.Utils.SIT;
import static ru.armagidon.sit.utils.Utils.STAND;

public class BetterChairHandler implements Listener
{

    private boolean enabled;
    private final String PLUGINAME = "BetterChair";

    private final Map<String, SitPluginPlayer> players = ru.armagidon.sit.utils.Listener.players;

    public BetterChairHandler() {
        this.enabled = Bukkit.getPluginManager().getPlugin(PLUGINAME)!=null;
        if(enabled){
            Bukkit.getPluginManager().registerEvents(this, SitPlugin.getInstance());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void sit(EntityMountEvent event){

        if(event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity();

            if (!containsPlayer(player)) return;
            SitPluginPlayer p = players.get(player.getName());

            if( Chair.CACHE_BY_PLAYER.containsKey(player) ){

                p.setPose(new SitPose(player));
                player.sendMessage(SIT);

            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void stand(PlayerSitEvent event){
        Player player = event.getPlayer();
        if(!event.isEnable()) {
            if (!containsPlayer(player)) return;
            SitPluginPlayer p = players.get(player.getName());

            if (Chair.CACHE_BY_PLAYER.containsKey(player)) {

                p.setPose(new StandingPose(player));
                player.sendMessage(STAND);
                Chair chair = Chair.CACHE_BY_PLAYER.get(player);
                chair.eject();
                chair.remove();

            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
