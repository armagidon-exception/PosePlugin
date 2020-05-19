package ru.armagidon.poseplugin.poses.swim;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.nms.interfaces.AnimationPlayer;

public class PacketSwimHandler implements ISwimAnimationHandler {

    private final Player player;
    private BukkitTask task;
    private Runnable runnable;
    public PacketSwimHandler(Player player) {
        this.player = player;
        task = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(),()-> {
            if(runnable!=null)runnable.run();
        },0,1);

    }

    @Override
    public void play(Player target) {
        runnable = ()->Bukkit.getOnlinePlayers().forEach(p-> AnimationPlayer.play(target,p, Pose.SWIMMING));
    }

    @Override
    public void stop() {
        task.cancel();
    }
}
