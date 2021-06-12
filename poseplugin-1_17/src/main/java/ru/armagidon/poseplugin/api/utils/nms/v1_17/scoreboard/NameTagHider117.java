package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.scoreboard.NameTagHider;

import java.util.HashMap;
import java.util.Map;

@ToolPackage(mcVersion = "1.17")
public class NameTagHider117 extends NameTagHider
{
    private final Map<Player, ScoreboardUtil> hiddenPlayers = new HashMap<>();

    public NameTagHider117(Plugin plugin) {
        super(plugin);
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
