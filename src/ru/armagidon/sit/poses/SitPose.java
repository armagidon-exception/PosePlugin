package ru.armagidon.sit.poses;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import static ru.armagidon.sit.utils.Utils.*;

public class SitPose extends PluginPose {

    public SitPose(Player player) {
        super(player);
    }

    private ArmorStand seat;

    @Override
    public void play(Player receiver,boolean log) {
        takeASeat(getPlayer(),getPlayer().getLocation());
        if(log){
            getPlayer().sendMessage(SIT);
        }
    }

    @Override
    public void stop(boolean log) {
        seat.eject();
        seat.remove();
        players.get(getPlayer().getName()).setPose(new StandingPose(getPlayer()));
        if (log) getPlayer().sendMessage(STAND);
    }

    public void takeASeat(Player player, Location l){
        Location location = l.clone();
        seat = player.getWorld().spawn(location.clone().subtract(0, 1.7, 0), ArmorStand.class);
        seat.setVisible(false);
        seat.setGravity(false);
        sitPlayer();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SITTING;
    }

    @Override
    public void move(PlayerMoveEvent event) {
        Location l = seat.getLocation().clone();
        l.setYaw(event.getTo().getYaw());
        seat.teleport(l);
    }

    @Override
    public void armor(PlayerArmorStandManipulateEvent event) {
        if(event.getRightClicked().equals(seat)) {
            event.setCancelled(true);
        }
    }

    private void sitPlayer(){
        seat.addPassenger(getPlayer());
    }
}
