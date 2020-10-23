package ru.armagidon.poseplugin.api.utils.playerhider;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;

public interface PlayerHider extends Tickable
{
    void hide(Player player);
    void show(Player player);
    boolean isHidden(Player player);
    
    @SneakyThrows
    static PlayerHider createNew(){
        Constructor<?> constructor = Class.forName("ru.armagidon.poseplugin.api.utils.playerhider.PlayerHider_" + ReflectionTools.nmsVersion()).getDeclaredConstructor();
        return  (PlayerHider) constructor.newInstance();
    }
}
