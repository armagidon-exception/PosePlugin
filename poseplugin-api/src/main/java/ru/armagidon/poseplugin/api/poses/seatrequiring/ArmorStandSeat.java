package ru.armagidon.poseplugin.api.poses.seatrequiring;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.ticking.Tickable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ArmorStandSeat implements Listener, Tickable
{

    private final Player sitter;
    private ArmorStand seat;
    private final Set<SeatObserver> seatObservers = new HashSet<>();
    private static final String METADATA_KEY = "PosePluginSeatStandUpCause";
    private double yOffset = 0;

    public ArmorStandSeat(Player sitter) {
        this.sitter = sitter;
        PosePluginAPI.getAPI().getTickingBundle().addToTickingBundle(ArmorStandSeat.class, this);
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
    }

    public void takeASeat() {
        takeASeat(0);
    }

    public void takeASeat(double yOffset) {
        Location location = sitter.getLocation().clone();
        this.yOffset = yOffset;
        seat = sitter.getWorld().spawn(location.clone().add(0, yOffset, 0).subtract(0, 0.2D, 0), ArmorStand.class, (armorStand -> {
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setVisible(false);
            armorStand.setCollidable(false);
            armorStand.addPassenger(sitter);
        }));
    }


    public void standUp() {
        if (seat == null) return;
        HandlerList.unregisterAll(this);
        sitter.eject();
        seat.addScoreboardTag("PPGC");
        seat.remove();
        sitter.teleport(seat.getLocation().clone().subtract(0, yOffset, 0).add(0, 0.2D,0).setDirection(sitter.getLocation().getDirection()));
        if( PosePluginAPI.getAPI().getPlugin().isEnabled() ) {
            Bukkit.getScheduler().runTaskLater(PosePluginAPI.getAPI().getPlugin(), () ->
                    sitter.teleport(seat.getLocation().clone().subtract(0, yOffset, 0).add(0, 0.2D,0).setDirection(sitter.getLocation().getDirection())), 1);
        }
        PosePluginAPI.getAPI().getTickingBundle().removeFromTickingBundle(ArmorStandSeat.class, this);
    }

    @EventHandler
    private void armorManipulate(PlayerArmorStandManipulateEvent event){
        if (seat == null) return;
        if(event.getRightClicked().equals(seat)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void playerStoodUpEvent(EntityDismountEvent event){
        if (seat == null) return;
        if( event.getEntity().getType().equals(EntityType.PLAYER) && event.getDismounted().getType().equals(EntityType.ARMOR_STAND) ){
            ArmorStand stand = (ArmorStand) event.getDismounted();
            Player player = (Player) event.getEntity();
            if( player.getUniqueId().equals(sitter.getUniqueId()) && stand.equals(seat) ){
                //If dismounting was caused by teleport, don't fire event
                if (player.hasMetadata(METADATA_KEY)) {
                    StandUpCause cause = (StandUpCause) player.getMetadata(METADATA_KEY).get(0).value();
                    if (cause == null) return;
                    if (cause.equals(StandUpCause.TELEPORT)) {
                        Bukkit.getScheduler().runTaskLater(PosePluginAPI.getAPI().getPlugin(), () -> {
                            seatObservers.forEach(observer -> observer.handleTeleport(this));
                        }, 5);
                        player.removeMetadata(METADATA_KEY, PosePluginAPI.getAPI().getPlugin());
                        return;
                    }
                }

                //If player dismounted from seat, do stuff
                seatObservers.forEach(observer -> observer.handleDismounting(event, this));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (seat == null) return;
        if (event.getPlayer().hasMetadata(METADATA_KEY)) return;
        if (event.getPlayer().equals(sitter) && event.getPlayer().getVehicle() != null && event.getPlayer().getVehicle().equals(seat)) {
            event.getPlayer().setMetadata(METADATA_KEY, new FixedMetadataValue(PosePluginAPI.getAPI().getPlugin(), StandUpCause.TELEPORT));
            seat.eject();
            event.getPlayer().teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            takeASeat();
        }
    }

    private void rotate(){
        if (seat == null) return;
        try {
            Object vanillaStand = seat.getClass().getMethod("getHandle").invoke(seat);
            Field yawF = vanillaStand.getClass().getField("yaw");
            yawF.set(vanillaStand, sitter.getLocation().getYaw());
        } catch (NoSuchFieldException e) {
            try {
                Object vanillaStand = seat.getClass().getMethod("getHandle").invoke(seat);
                Method setYRot = vanillaStand.getClass().getMethod("setYRot", float.class);
                setYRot.setAccessible(true);
                setYRot.invoke(vanillaStand, sitter.getLocation().getYaw());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick(){
        if (seat != null) {
            rotate();
            if (!seat.isDead() && !seat.getPassengers().contains(sitter) && seat.getScoreboardTags().contains("PPGC")) {
                seat.remove();
            }
        }
    }

    public void addSeatObserver(SeatObserver observer) {
        Validate.notNull(observer);
        seatObservers.add(observer);
    }

    public void pushBack() {
        if (seat == null) return;
        PosePluginAPI.getAPI().getTickManager().later(() -> seat.addPassenger(sitter), 3);
    }

    enum StandUpCause {
        TELEPORT, WANTED
    }
}
