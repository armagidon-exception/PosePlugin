package ru.armagidon.poseplugin.api;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventDispatcher;
import ru.armagidon.poseplugin.api.personalListener.PersonalHandlerList;
import ru.armagidon.poseplugin.api.player.P3Map;
import ru.armagidon.poseplugin.api.ticking.TickModuleManager;
import ru.armagidon.poseplugin.api.utils.armor.ArmorHider;
import ru.armagidon.poseplugin.api.utils.core_wrapper.CoreWrapper;
import ru.armagidon.poseplugin.api.utils.core_wrapper.PaperCoreWrapper;
import ru.armagidon.poseplugin.api.utils.core_wrapper.SpigotCoreWrapper;
import ru.armagidon.poseplugin.api.utils.misc.Debugger;
import ru.armagidon.poseplugin.api.utils.misc.event.EventListener;
import ru.armagidon.poseplugin.api.utils.nms.NMSFactory;
import ru.armagidon.poseplugin.api.utils.nms.PlayerHider;
import ru.armagidon.poseplugin.api.utils.packetManagement.PacketReaderManager;
import ru.armagidon.poseplugin.api.utils.packetManagement.readers.SwingPacketReader;
import ru.armagidon.poseplugin.api.utils.scoreboard.NameTagHider;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PosePluginAPI
{
    private static final PosePluginAPI api = new PosePluginAPI();

    private @Getter final PacketReaderManager packetReaderManager;
    private NMSFactory nmsFactory;
    private @Getter PlayerHider playerHider;
    private @Getter final P3Map playerMap;
    private @Getter final TickModuleManager tickManager;
    private @Getter final NameTagHider nameTagHider;
    private @Getter final PersonalHandlerList personalHandlerList;
    private @Getter final Debugger debugger;
    private @Getter ArmorHider armorHider;
    private @Getter CoreWrapper coreWrapper;

    private PosePluginAPI() {
        this.packetReaderManager = new PacketReaderManager();
        this.playerMap = new P3Map();
        this.tickManager = new TickModuleManager();
        this.nameTagHider = new NameTagHider();
        this.personalHandlerList = new PersonalHandlerList();
        this.debugger = new Debugger();
    }

    /**PoopCode starts*/
    private @Getter Plugin plugin;

    public void init(Plugin plugin){
        this.plugin = plugin;
        /*PoopCode ends(i hope)*/
        this.armorHider = new ArmorHider();
        //Init nms-factory and player-hider
        if(checkPaper()) coreWrapper = new PaperCoreWrapper(plugin);
        else coreWrapper = new SpigotCoreWrapper(plugin);
        try {
            this.nmsFactory = new NMSFactory();
            this.playerHider = nmsFactory.createPlayerHider();
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            setEnabled(false);
            plugin.getLogger().severe("Failed to enabled plugin! This version is not supported!");
            e.printStackTrace();
        }
        //Foreach online players

        registerPacketListeners();

        Bukkit.getOnlinePlayers().forEach(player->{
            packetReaderManager.inject(player);
            playerMap.addPlayer(player);
        });
        //Register main events
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(),plugin);
        //Register PersonalEventDispatcher
        Bukkit.getServer().getPluginManager().registerEvents(new PersonalEventDispatcher(),plugin);
    }

    public void shutdown(){
        getPlayerMap().forEach(p -> p.getPose().stop());
        Bukkit.getOnlinePlayers().forEach(packetReaderManager::eject);
    }

    public static PosePluginAPI getAPI() {
        return api;
    }

    public NMSFactory getNMSFactory() {
        return nmsFactory;
    }

    public Logger getLogger(){
        return getPlugin().getLogger();
    }

    private void registerPacketListeners(){
        getPacketReaderManager().registerPacketReader(new SwingPacketReader());
    }

    @SneakyThrows
    public static void setEnabled(boolean enabled){
        Method m = JavaPlugin.class.getDeclaredMethod("setEnabled", boolean.class);
        m.setAccessible(true);
        m.invoke(getAPI().getPlugin(), enabled);
    }

    public void registerListener(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, getPlugin());
    }

    private boolean checkPaper(){
        return Package.getPackage("com.destroystokyo.paper")!=null;
    }
}
