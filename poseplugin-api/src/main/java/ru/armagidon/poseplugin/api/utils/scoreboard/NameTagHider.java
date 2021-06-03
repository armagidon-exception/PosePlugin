package ru.armagidon.poseplugin.api.utils.scoreboard;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.HashMap;
import java.util.Map;

public class NameTagHider
{
    private final Map<Player, ScoreboardUtil> hiddenPlayers = new HashMap<>();

    public NameTagHider(Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new ScoreboardEventPipelineInjector(plugin, hiddenPlayers));
    }

    public void hideTag(Player player){
        ScoreboardUtil util = new ScoreboardUtil(player);
        PosePluginAPI.getAPI().registerListener(util);
        hiddenPlayers.put(player, util);
        util.hideTag();
    }

    public void showTag(Player player){

        if (hiddenPlayers.containsKey(player)){
            ScoreboardUtil util = hiddenPlayers.remove(player);
            HandlerList.unregisterAll(util);
            util.showTag();
        }
    }
}
