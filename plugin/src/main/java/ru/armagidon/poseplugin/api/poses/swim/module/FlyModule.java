package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.utils.nms.npc.FakePlayer;

public class FlyModule implements SwimModule {

    private final FakePlayer fakePlayer;
    private final Player player;
    private boolean started = false;

    public FlyModule(FakePlayer fakePlayer, Player player) {
        this.fakePlayer = fakePlayer;
        this.player = player;
        Bukkit.getServer().getPluginManager().registerEvents(this, PosePlugin.getInstance());
        this.fakePlayer.setInvulnerable(true);
    }

    @Override
    public void action() {
        if(!started){
            fakePlayer.broadCastSpawn();
            started = true;
        }
        NMSUtils.setInvisible(player, true);
        fakePlayer.move();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent join){
        fakePlayer.spawnToPlayer(join.getPlayer());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        fakePlayer.remove();
        NMSUtils.setInvisible(player,false);

    }

    @Override
    public SwimModuleType getType() {
        return SwimModuleType.FLY;
    }
}
