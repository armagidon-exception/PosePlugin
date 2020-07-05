package ru.armagidon.poseplugin.api.utils.packetManagement.readers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.packetManagement.PacketReader;

import java.lang.reflect.Field;

public class EqReader extends PacketReader {

    public EqReader() {
        super("EqReader");
    }

    @Override
    protected boolean readServerPackets(Player sender, Object packet) throws Exception {
        if(packet.getClass().getSimpleName().equalsIgnoreCase("packetplayoutentityequipment")){
            Field f = packet.getClass().getDeclaredField("a");
            f.setAccessible(true);
            int id = f.getInt(packet);
            Bukkit.getOnlinePlayers().forEach(p->{
                if(p.getEntityId()==id){
                    System.out.println(id);
                }
            });
        }
        return true;
    }

    @Override
    protected boolean readClientPackets(Player sender, Object packet) {
        return true;
    }
}
