package ru.armagidon.poseplugin.api;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventDispatcher;
import ru.armagidon.poseplugin.api.player.P3Map;
import ru.armagidon.poseplugin.api.ticking.TickModuleManager;
import ru.armagidon.poseplugin.api.utils.misc.PluginLogger;
import ru.armagidon.poseplugin.api.utils.misc.event.EventListener;
import ru.armagidon.poseplugin.api.utils.nms.NMSFactory;
import ru.armagidon.poseplugin.api.utils.nms.PlayerHider;
import ru.armagidon.poseplugin.api.utils.packetManagement.PacketReaderManager;
import ru.armagidon.poseplugin.api.utils.packetManagement.readers.EqReader;
import ru.armagidon.poseplugin.api.utils.packetManagement.readers.SwingPacketReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("DanglingJavadoc")
public class PosePluginAPI
{
    private static final PosePluginAPI api = new PosePluginAPI();

    private final PacketReaderManager manager;
    private NMSFactory nmsFactory;
    private PlayerHider playerHider;
    private final P3Map playerMap;
    private final TickModuleManager tickManager;

    private PluginLogger logger;

    private ServerStatus status;

    private PosePluginAPI() {
        this.manager = new PacketReaderManager();
        this.playerMap = new P3Map();
        this.tickManager = new TickModuleManager();
    }

    /**PoopCode starts*/
    private Plugin plugin;

    public void init(Plugin plugin){
        status = ServerStatus.STARTING;
        this.plugin = plugin;
        /**PoopCode ends(i hope)*/
        //Init logger
        this.logger = new PluginLogger(plugin);
        //Init nms-factory and player-hider
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
            manager.inject(player);
            playerMap.addPlayer(player);
        });
        //Register main events
        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(),plugin);
        //Register PersonalEventDispatcher
        Bukkit.getServer().getPluginManager().registerEvents(new PersonalEventDispatcher(),plugin);
    }

    public void shutdown(){
        status = ServerStatus.SHUTTING_DOWN;
        getPlayerMap().forEach(p -> p.getPose().stop());
        Bukkit.getOnlinePlayers().forEach(manager::eject);
    }

    public static PosePluginAPI getAPI() {
        return api;
    }

    public NMSFactory getNMSFactory() {
        return nmsFactory;
    }

    public PlayerHider getPlayerHider() {
        return playerHider;
    }

    public P3Map getPlayerMap() {
        return playerMap;
    }

    public PacketReaderManager getPacketReaderManager() {
        return manager;
    }

    public PluginLogger getLogger(){
        return logger;
    }

    public Plugin getPlugin(){
        return plugin;
    }

    public TickModuleManager getTickManager() {
        return tickManager;
    }

    private void registerPacketListeners(){
        manager.registerPacketReader(new SwingPacketReader());
        manager.registerPacketReader(new EqReader());
    }

    public static void setEnabled(boolean enabled){
        try {
            Method m = getAPI().getPlugin().getClass().getDeclaredMethod("setEnabled", boolean.class);
            m.setAccessible(true);
            m.invoke(getAPI().getPlugin(), enabled);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
    }

    public ServerStatus getStatus() {
        return status;
    }

    public void registerListener(Listener listener){
        Bukkit.getPluginManager().registerEvents(listener, getPlugin());
    }

    public enum ServerStatus{
        STARTING,
        SHUTTING_DOWN
    }



}
