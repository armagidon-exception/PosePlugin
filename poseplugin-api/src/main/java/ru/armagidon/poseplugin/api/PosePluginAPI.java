package ru.armagidon.poseplugin.api;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventDispatcher;
import ru.armagidon.poseplugin.api.personalListener.PersonalHandlerList;
import ru.armagidon.poseplugin.api.player.P3Map;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.ticking.TickModuleManager;
import ru.armagidon.poseplugin.api.ticking.TickingBundle;
import ru.armagidon.poseplugin.api.utils.ArmorHider;
import ru.armagidon.poseplugin.api.utils.misc.event.EventListener;
import ru.armagidon.poseplugin.api.utils.nms.ToolFactory;
import ru.armagidon.poseplugin.api.utils.nms.playerhider.PlayerHider;
import ru.armagidon.poseplugin.api.utils.nms.scoreboard.NameTagHider;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class PosePluginAPI
{
    private static PosePluginAPI API;

    public static final String NBT_TAG = "PosePluginItem";


    private @Getter PlayerHider playerHider;
    private @Getter P3Map playerMap;
    private @Getter TickModuleManager tickManager;
    private @Getter NameTagHider nameTagHider;
    private @Getter PersonalHandlerList personalHandlerList;
    private @Getter TickingBundle tickingBundle;
    private @Getter ArmorHider armorHider;

    @SneakyThrows
    private PosePluginAPI(Plugin plugin) {
        this.plugin = plugin;

    }

    private final @Getter Plugin plugin;

    private void init(){
        ToolFactory.scanTools();

        initTools();

        Bukkit.getServer().getPluginManager().registerEvents(armorHider, plugin);
        this.tickingBundle = new TickingBundle();
        //Init nms-factory and player-hider

        //playerHider = PlayerHider.createNew();

        //Foreach online players
        Bukkit.getOnlinePlayers().forEach((Consumer<Player>) player -> playerMap.addPlayer(player));
        //Register main events
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(),plugin);
        //Register PersonalEventDispatcher
        Bukkit.getServer().getPluginManager().registerEvents(new PersonalEventDispatcher(),plugin);
    }

    public Logger getLogger(){
        return plugin.getLogger();
    }

    public void shutdown() {
        getPlayerMap().forEach(p -> p.getPose().stop());
    }

    public static PosePluginAPI getAPI() {
        return API;
    }

    public static void initialize(Plugin plugin){
        PosePluginAPI ppapi = new PosePluginAPI(plugin);
        API = ppapi;
        ppapi.init();
    }

    public void registerListener(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public PosePluginPlayer getPlayer(Player bukkitInstance) {
        return playerMap.getPosePluginPlayer(bukkitInstance);
    }

    private void initTools() {
        this.playerMap = new P3Map();
        this.tickManager = new TickModuleManager();
        this.nameTagHider = ToolFactory.create(NameTagHider.class, plugin);
        this.personalHandlerList = new PersonalHandlerList();
        this.armorHider = new ArmorHider();
    }
}
