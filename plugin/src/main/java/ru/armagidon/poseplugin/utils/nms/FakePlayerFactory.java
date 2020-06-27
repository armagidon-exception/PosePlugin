package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.utils.nms.npc.FakePlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FakePlayerFactory
{

    private Constructor<?> maker;

    public FakePlayerFactory() throws ClassNotFoundException, NoSuchMethodException {
        this.maker = Class.forName("ru.armagidon.poseplugin.utils.nms.npc.FakePlayer_"+ReflectionTools.nmsVersion()).getDeclaredConstructor(Player.class, Pose.class);
        maker.setAccessible(true);
    }

    public FakePlayer createInstance(Player player, Pose pose){
        try {
            return (FakePlayer) maker.newInstance(player,pose);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
