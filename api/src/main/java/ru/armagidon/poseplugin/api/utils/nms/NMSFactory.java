package ru.armagidon.poseplugin.api.utils.nms;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class NMSFactory
{
    private final Constructor<?> fakeplayer;
    private final Constructor<?> playerhider;

    public NMSFactory() throws ClassNotFoundException, NoSuchMethodException {
        this.fakeplayer = Class.forName("ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer_"+ReflectionTools.nmsVersion()).getDeclaredConstructor(Player.class, Pose.class);
        fakeplayer.setAccessible(true);
        this.playerhider = Class.forName("ru.armagidon.poseplugin.api.utils.nms.PlayerHider_"+ReflectionTools.nmsVersion()).getDeclaredConstructor();
        playerhider.setAccessible(true);
    }

    public FakePlayer createFakePlayer(Player player, Pose pose){
        try {
            return (FakePlayer) fakeplayer.newInstance(player,pose);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlayerHider createPlayerHider(){
        try {
            return (PlayerHider) playerhider.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
