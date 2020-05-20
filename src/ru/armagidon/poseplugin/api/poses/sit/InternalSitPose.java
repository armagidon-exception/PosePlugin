package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;

public class InternalSitPose extends SitPose
{

    private ArmorStand seat;
    private Block block;
    
    public InternalSitPose(Player player) {
        super(player);
        this.block = VectorUtils.getBlock(player.getLocation()).getRelative(BlockFace.DOWN);
    }

    @Override
    public void standUp(Player player) {
        seat.remove();
    }

    @Override
    public void takeASeat(Player player, Location l) {
        Location location = l.clone();
        seat = player.getWorld().spawn(location.clone().subtract(0, 1.7, 0), ArmorStand.class);
        seat.setVisible(false);
        seat.setGravity(false);
        seat.addPassenger(player);
    }

    @EventHandler
    public void armorManipulate(PlayerArmorStandManipulateEvent event){
        if(event.getRightClicked().equals(seat)){
            event.setCancelled(true);
        }
    }
}
