package ru.armagidon.poseplugin.api.utils.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.HashMap;
import java.util.Map;

public class NameTagHider
{
    private final Map<Player, ScoreboardUtil> utils = new HashMap<>();

    public void hideTag(Player player){
        ScoreboardUtil util = new ScoreboardUtil(player);
        PosePluginAPI.getAPI().registerListener(util);
        utils.put(player, util);
        util.hideTag();
    }

    public void showTag(Player player){

        if (utils.containsKey(player)){
            ScoreboardUtil util = utils.remove(player);
            HandlerList.unregisterAll(util);
            util.showTag();
        }
    }
}
