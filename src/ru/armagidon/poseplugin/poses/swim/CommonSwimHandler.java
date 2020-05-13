package ru.armagidon.poseplugin.poses.swim;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;

import static ru.armagidon.poseplugin.utils.VectorUtils.getBlock;

public class CommonSwimHandler implements ISwimAnimationHandler {

    private final int modifier;
    private CacheBlock cache = null;
    private BukkitTask task;


    public CommonSwimHandler(int modifier, Player target) {
        this.modifier = modifier;
        task = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(),()->{

            Block above = getBlock(target.getLocation().add(0,modifier,0));
            Bukkit.getOnlinePlayers().stream().filter(receiver->!receiver.getName().equals(target.getName())).
                    forEach(rec-> rec.sendBlockChange(above.getLocation(),cache.getData()));

        },0,1);
    }

    @Override
    public void play(Player target) {
        Block above = getBlock(target.getLocation().add(0,modifier,0));
        if(cache!=null){
            cache.restore();
            cache = null;
        }
        if(!above.getType().isSolid()|| Tag.BANNERS.isTagged(above.getType())){
            //If block above is air, place a fake barrier
            if(cache==null) cache = new CacheBlock(above.getType(),above.getBlockData(),above.getLocation());
            above.setType(Material.BARRIER);
        }
    }

    @Override
    public void stop() {

        if(cache!=null) cache.restore();
        task.cancel();
    }

    class CacheBlock
    {
        private final Material material;
        private final BlockData data;
        private final Location location;

        public CacheBlock(Material material, BlockData data, Location location) {
            this.material = material;
            this.data = data;
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public BlockData getData() {
            return data;
        }

        public Material getMaterial() {
            return material;
        }

        public void restore(){
            getLocation().getBlock().setType(material);
            getLocation().getBlock().setBlockData(data);
        }
    }
}
