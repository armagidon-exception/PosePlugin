package ru.armagidon.poseplugin.api.utils.playerhider;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.New.NewPlayerHiderProtocolized;

public abstract class PlayerHider implements Tickable
{

    public abstract void hide(Player player);
    public abstract void show(Player player);
    public abstract boolean isHidden(Player player);

    @SneakyThrows
    public static PlayerHider createNew(){
        /*String path = String.format("ru.armagidon.poseplugin.api.utils.playerhider.%s.PlayerHiderImpl", ReflectionTools.nmsVersion());
        return (PlayerHider) Class.forName(path).getDeclaredConstructor().newInstance();*/
        return new NewPlayerHiderProtocolized();
    }
}
