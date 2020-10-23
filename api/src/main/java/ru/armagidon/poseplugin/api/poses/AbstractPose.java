package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;

public abstract class AbstractPose implements IPluginPose, Listener, PersonalListener
{
    private final Player player;
    private final PropertyMap propertyMap;

    public static IPluginPose STANDING = new IPluginPose() {
        @Override
        public PropertyMap getProperties() {
            return new PropertyMap();
        }

        @Override
        public void initiate() {}

        @Override
        public void play(Player receiver) {}

        @Override
        public void stop() {}

        @Override
        public EnumPose getType() {
            return EnumPose.STANDING;
        }
    };

    public AbstractPose(Player target) {
        this.player = target;
        this.propertyMap = new PropertyMap();
    }

    public Player getPlayer() {
        return player;
    }

    protected final PosePluginPlayer getPosePluginPlayer(){
        return PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player.getName());
    }

    @Override
    public void initiate() {
        PosePluginAPI.getAPI().getPersonalHandlerList().subscribe(getPosePluginPlayer(), this);
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
    }

    public void stop(){
        PosePluginAPI.getAPI().getPersonalHandlerList().unsubscribe(getPosePluginPlayer(), this);
        HandlerList.unregisterAll(this);
        getPosePluginPlayer().setPose(AbstractPose.STANDING);
    }

    @Override
    public PropertyMap getProperties() {
        return propertyMap;
    }

    @PersonalEventHandler
    public void onDeath(PlayerDeathEvent e){
        getPosePluginPlayer().resetCurrentPose(false);
    }
}
