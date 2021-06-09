package ru.armagidon.poseplugin.api.poses.seatrequiring;

import org.spigotmc.event.entity.EntityDismountEvent;

public interface SeatObserver
{
    void handleTeleport(ArmorStandSeat seat);
    void handleDismounting(EntityDismountEvent event, ArmorStandSeat seat);
}
