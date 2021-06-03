package ru.armagidon.poseplugin.api.utils.playerhider;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.New.NewPlayerHiderProtocolized;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.Old.OldPlayerHiderProtocolized;

public abstract class PlayerHider implements Tickable
{

    public abstract void hide(Player player);
    public abstract void show(Player player);
    public abstract boolean isHidden(Player player);


    public static PlayerHider createNew() {
        if (ReflectionTools.nmsVersion().equalsIgnoreCase("v1_15_R1")) {
            return new OldPlayerHiderProtocolized();
        } else {
            return new NewPlayerHiderProtocolized();
        }
    }
}
