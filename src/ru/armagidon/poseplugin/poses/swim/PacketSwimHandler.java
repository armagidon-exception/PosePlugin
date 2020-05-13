package ru.armagidon.poseplugin.poses.swim;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.nms.AnimationPlayer;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;

public class PacketSwimHandler implements ISwimAnimationHandler {

    private BukkitTask task;
    private Runnable runnable;
    public PacketSwimHandler() {
        task = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(),()-> {
            if(runnable!=null)runnable.run();
        },0,1);

    }

    @Override
    public void play(Player target) {
        AnimationPlayer player = NMSUtils.getAnimationPlayer();
        runnable = ()-> Bukkit.getOnlinePlayers().forEach(p-> player.play(target,p, Pose.SWIMMING));
    }

    @Override
    public void stop() {
        task.cancel();
    }
}
