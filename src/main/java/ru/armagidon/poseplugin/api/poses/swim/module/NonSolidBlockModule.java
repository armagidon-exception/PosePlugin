package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.misc.VectorUtils;

public class NonSolidBlockModule implements SwimModule {

    private final Player player;

    public NonSolidBlockModule(Player player) {
        this.player = player;
        player.setGliding(true);
        Bukkit.getServer().getPluginManager().registerEvents(this, PosePlugin.getInstance());
    }

    @Override
    public void action() {
        //Nothing
    }

    @EventHandler
    public void onSwim(EntityToggleGlideEvent event){
        if(event.getEntity().equals(player)){
            event.setCancelled(true);
        }
    }

    @Override
    public void stop() {
        player.setGliding(false);
        HandlerList.unregisterAll(this);
    }

    @Override
    public SwimModuleType getType() {
        return SwimModuleType.NONSOLID;
    }

    public static boolean test(Player player){
        {
            Block lookingat = VectorUtils.getDirBlock(player.getLocation());
            if(lookingat==null) return false;
            boolean unsolidlookingat = !lookingat.getType().isSolid() || Tag.PORTALS.isTagged(lookingat.getType()) || Tag.SIGNS.isTagged(lookingat.getType()) || Tag.DOORS.isTagged(lookingat.getType()) || Tag.BANNERS.isTagged(lookingat.getType());

            lookingat = lookingat.getRelative(BlockFace.UP);

            boolean solidAbovelookingat = !(!lookingat.getType().isSolid() || Tag.PORTALS.isTagged(lookingat.getType()) || Tag.SIGNS.isTagged(lookingat.getType()) || Tag.DOORS.isTagged(lookingat.getType()) || Tag.BANNERS.isTagged(lookingat.getType()));

            return unsolidlookingat && solidAbovelookingat;
        }
    }
}
