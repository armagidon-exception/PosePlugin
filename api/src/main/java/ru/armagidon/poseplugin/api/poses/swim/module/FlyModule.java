package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;

public class FlyModule implements SwimModule {

    private final FakePlayer fakePlayer;
    private final Player player;

    public FlyModule(Player player) {
        this.fakePlayer = PosePluginAPI.getAPI().getNMSFactory().createFakePlayer(player, Pose.SWIMMING);
        this.player = player;
        Bukkit.getServer().getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
        fakePlayer.broadCastSpawn();
        PosePluginAPI.getAPI().getPlayerHider().hide(player);
    }

    @Override
    public void action() {}

    @EventHandler
    public void onJoin(PlayerJoinEvent join){
        fakePlayer.spawnToPlayer(join.getPlayer());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        fakePlayer.remove();
        PosePluginAPI.getAPI().getPlayerHider().show(player);
    }

    @Override
    public SwimPose.SwimMode getMode() {
        return SwimPose.SwimMode.FLYING;
    }
}
