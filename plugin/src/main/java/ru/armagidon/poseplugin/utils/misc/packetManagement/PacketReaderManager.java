package ru.armagidon.poseplugin.utils.misc.packetManagement;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PacketReaderManager
{

    private final Set<PacketReader> readers = new HashSet<>();


    public final void registerPacketReader(PacketReader reader){
        if(reader!=null){
            readers.add(reader);
        }
    }

    public final void inject(Player player){
        readers.forEach(reader -> {
            if(!reader.containsInPipeline(player)){
                reader.inject(player);
            }
        });
    }

    public final void eject(Player player){

        readers.forEach(reader -> {
            if(reader.containsInPipeline(player)){
                reader.eject(player);
            }
        });
    }



}
