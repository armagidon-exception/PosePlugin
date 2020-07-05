package ru.armagidon.poseplugin.api.ticking;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.Map;

public class TickModuleManager {

    private final Map<Tickable, BukkitTask> tickers;

    public TickModuleManager() {

        tickers = Maps.newHashMap();
    }

    public final void registerTickModule(Tickable module, boolean async){
        Runnable task = () -> {
            try {
                module.tick();
            } catch (Exception e) {
                removeTickModule(module);
                PosePluginAPI.getAPI().getLogger().severe("Error occurred while ticking: " + e.getMessage());
                e.printStackTrace();
            }
        };

        BukkitTask t;

        if(!async) {
            t = Bukkit.getScheduler().runTaskTimer(PosePluginAPI.getAPI().getPlugin(), task, 0, 1);
        } else {
            t = Bukkit.getScheduler().runTaskTimerAsynchronously(PosePluginAPI.getAPI().getPlugin(), task, 0,1);
        }
        tickers.put(module,t);
    }

    public final void removeTickModule(Tickable tickable){
        if(tickers.containsKey(tickable)) {
            BukkitTask t = tickers.get(tickable);
            if(!t.isCancelled()) t.cancel();
            tickers.remove(tickable);
        }
    }

}
