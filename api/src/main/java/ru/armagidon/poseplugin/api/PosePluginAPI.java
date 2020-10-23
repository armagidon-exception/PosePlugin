package ru.armagidon.poseplugin.api;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.papermc.lib.PaperLib;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.armagidonapi.itemutils.ItemModifingPipeline;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventDispatcher;
import ru.armagidon.poseplugin.api.personalListener.PersonalHandlerList;
import ru.armagidon.poseplugin.api.player.P3Map;
import ru.armagidon.poseplugin.api.ticking.TickModuleManager;
import ru.armagidon.poseplugin.api.ticking.TickingBundle;
import ru.armagidon.poseplugin.api.utils.ArmorHider;
import ru.armagidon.poseplugin.api.utils.NameTagHider;
import ru.armagidon.poseplugin.api.utils.corewrapper.CoreWrapper;
import ru.armagidon.poseplugin.api.utils.corewrapper.PaperCoreWrapper;
import ru.armagidon.poseplugin.api.utils.corewrapper.SpigotCoreWrapper;
import ru.armagidon.poseplugin.api.utils.misc.Debugger;
import ru.armagidon.poseplugin.api.utils.misc.event.EventListener;
import ru.armagidon.poseplugin.api.utils.playerhider.PlayerHider;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PosePluginAPI
{
    private static final PosePluginAPI api = new PosePluginAPI();
    public static ItemModifingPipeline pluginTagClear = new ItemModifingPipeline() {{
        addLast(stack -> {
            if(  stack == null || stack.getType() == Material.AIR ) return;
            NBTItem i = new NBTItem(stack, true);
            if(i.hasKey("PosePluginItem"))
                i.removeKey("PosePluginItem");
        });
    }};


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
    private PosePluginAPI() {
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

    public void shutdown(){
        getPlayerMap().forEach(p -> p.getPose().stop());
    }

    public static PosePluginAPI getAPI() {
        return api;
    }

    public Logger getLogger(){
        return getPlugin().getLogger();
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
}
