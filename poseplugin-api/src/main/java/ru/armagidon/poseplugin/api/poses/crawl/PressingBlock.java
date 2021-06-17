package ru.armagidon.poseplugin.api.poses.crawl;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getNmsClass;


public abstract class PressingBlock
{

    protected final Player player;
    protected Location location;
    private boolean shown = false;

    public PressingBlock(Location location, Player player) {
        this.location = location;
        this.player = player;
    }

    public void show() {
        if (!shown) {
            show0();
            shown = true;
        }
    }

    public void hide() {
        if (shown) hide0();
    }

    public void move(Location location) {
        if (shown) move0(location);
        this.location = location;
    }

    protected abstract void show0();
    protected abstract void hide0();
    protected abstract void move0(Location location);



    public static class BarrierPressingBlock extends PressingBlock {

        private static final BlockData BARRIER_DATA = Bukkit.createBlockData(Material.BARRIER);
        private final BlockCache cache;

        public BarrierPressingBlock(Location location, Player player) {
            super(location, player);
            cache = new BlockCache(location.getBlock().getBlockData(), location);
        }

        @Override
        public void show0() {
            player.sendBlockChange(location, BARRIER_DATA);
        }

        @Override
        public void hide0() {
            cache.restore(player);
        }

        @Override
        public void move0(Location to) {
            cache.restore(player);
            if (!to.getBlock().getType().isSolid()) {
                cache.setData(to.getBlock().getBlockData());
                cache.setLocation(to);
                player.sendBlockChange(to, BARRIER_DATA);
            }
        }
    }
}
