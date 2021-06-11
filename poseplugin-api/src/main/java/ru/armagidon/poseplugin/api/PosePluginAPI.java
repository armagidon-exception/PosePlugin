package ru.armagidon.poseplugin.api;

import io.papermc.lib.PaperLib;
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
import ru.armagidon.poseplugin.api.poses.IllegalMCVersionException;
import ru.armagidon.poseplugin.api.ticking.TickModuleManager;
import ru.armagidon.poseplugin.api.ticking.TickingBundle;
import ru.armagidon.poseplugin.api.utils.ArmorHider;
import ru.armagidon.poseplugin.api.utils.scoreboard.NameTagHider;
import ru.armagidon.poseplugin.api.utils.corewrapper.CoreWrapper;
import ru.armagidon.poseplugin.api.utils.corewrapper.PaperCoreWrapper;
import ru.armagidon.poseplugin.api.utils.corewrapper.SpigotCoreWrapper;
import ru.armagidon.poseplugin.api.utils.misc.Debugger;
import ru.armagidon.poseplugin.api.utils.misc.event.EventListener;
import ru.armagidon.poseplugin.api.utils.playerhider.PlayerHider;

import java.util.logging.Logger;

public class PosePluginAPI
{
    private static PosePluginAPI API;

    public static final String NBT_TAG = "PosePluginItem";


    private @Getter PlayerHider playerHider;
    private @Getter final P3Map playerMap;
    private @Getter final TickModuleManager tickManager;
    private @Getter final NameTagHider nameTagHider;
    private @Getter final PersonalHandlerList personalHandlerList;
    private @Getter final Debugger debugger;
    private @Getter TickingBundle tickingBundle;
    private @Getter ArmorHider armorHider;
    private @Getter CoreWrapper coreWrapper;

    @SneakyThrows
    private PosePluginAPI(Plugin plugin) {
        this.plugin = plugin;
        this.playerMap = new P3Map();
        this.tickManager = new TickModuleManager();
        this.nameTagHider = new NameTagHider(plugin);
        this.personalHandlerList = new PersonalHandlerList();
        this.debugger = new Debugger();
    }

    private final @Getter Plugin plugin;

    private void init(){
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            if (!Bukkit.getVersion().contains("1.17")) {
                throw new IllegalMCVersionException("ProtocolLib was not found. Disabling...");
            }
        }

        this.armorHider = new ArmorHider();
        Bukkit.getServer().getPluginManager().registerEvents(armorHider, plugin);
        this.tickingBundle = new TickingBundle();
        //Init nms-factory and player-hider
        if(PaperLib.isPaper()) coreWrapper = new PaperCoreWrapper(plugin);
        else {
            coreWrapper = new SpigotCoreWrapper(plugin);
            PaperLib.suggestPaper(plugin);
        }

        playerHider = PlayerHider.createNew();

        //Foreach online players
        Bukkit.getOnlinePlayers().forEach(playerMap::addPlayer);
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
}
