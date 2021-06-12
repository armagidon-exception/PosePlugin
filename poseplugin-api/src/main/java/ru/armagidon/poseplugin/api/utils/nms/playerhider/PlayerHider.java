package ru.armagidon.poseplugin.api.utils.nms.playerhider;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;

public abstract class PlayerHider implements Tickable
{

    public abstract void hide(Player player);
    public abstract void show(Player player);
    public abstract boolean isHidden(Player player);


    @SneakyThrows
    public static PlayerHider createNew() {
        if (Bukkit.getVersion().contains("1.17")) {
            Class<?> clazz = Class.forName("ru.armagidon.poseplugin.api.utils.playerhider.v1_17_R1.PlayerHiderImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (PlayerHider) constructor.newInstance();
        } else if (ReflectionTools.nmsVersion().equalsIgnoreCase("v1_15_R1")) {
            //return new OldPlayerHiderProtocolized();
        } else {
            //return new NewPlayerHiderProtocolized();
        }
        return null;
    }
}
