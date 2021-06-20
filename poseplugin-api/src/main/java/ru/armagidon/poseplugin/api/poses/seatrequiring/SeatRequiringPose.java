package ru.armagidon.poseplugin.api.poses.seatrequiring;

import org.bukkit.entity.Player;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.armagidon.poseplugin.api.poses.AbstractPose;


/*
* Class that represents pose that requires armor stand seat. E.g sit pose or lay pose.
*/
public abstract class SeatRequiringPose extends AbstractPose implements SeatObserver
{

    protected final ArmorStandSeat seat;

    public SeatRequiringPose(Player target) {
        super(target);
        this.seat = new ArmorStandSeat(target);
        this.seat.addSeatObserver(this);
    }

    @Override
    public void play(Player receiver) {
        seat.takeASeat();
    }

    @Override
    public void stop() {
        super.stop();
        seat.standUp();
    }

    @Override
    public void handleDismounting(EntityDismountEvent e, ArmorStandSeat seat) {
        if(!getPosePluginPlayer().stopCurrentPose()){
            seat.pushBack();
        }
    }

    @Override
    public void handleTeleport(ArmorStandSeat seat) {}
}
