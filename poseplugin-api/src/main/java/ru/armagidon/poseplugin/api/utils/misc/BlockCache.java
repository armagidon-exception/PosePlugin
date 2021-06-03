package ru.armagidon.poseplugin.api.utils.misc;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class BlockCache {

    private @Getter @Setter BlockData data;
    private @Getter @Setter Location location;

    public BlockCache(BlockData data, Location location) {
        this.data = data;
        this.location = location;
    }

    public void restore(){
        getLocation().getBlock().setBlockData(data);
    }

    public void restore(Player receiver) {
        receiver.sendBlockChange(getLocation(), getData());
    }
}
