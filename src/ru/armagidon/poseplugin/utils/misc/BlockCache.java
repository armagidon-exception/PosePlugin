package ru.armagidon.poseplugin.utils.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class BlockCache {
    private final Material material;
    private final BlockData data;
    private final Location location;

    public BlockCache(Material material, BlockData data, Location location) {
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

    public void restore() {
        getLocation().getBlock().setType(material);
        getLocation().getBlock().setBlockData(data);
    }

    public void restore(Player receiver) {
        receiver.sendBlockChange(getLocation(), getData());
    }
}
