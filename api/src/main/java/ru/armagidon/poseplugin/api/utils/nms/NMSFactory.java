package ru.armagidon.poseplugin.api.utils.nms;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.items.ItemUtil;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;

import java.lang.reflect.Constructor;

public class NMSFactory
{
    private final Constructor<?> fakeplayer;
    private final Constructor<?> playerhider;
    private final Constructor<?> itemUtil;

    public NMSFactory() throws ClassNotFoundException, NoSuchMethodException {
        this.fakeplayer = Class.forName("ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer_"+ReflectionTools.nmsVersion()).getDeclaredConstructor(Player.class, Pose.class);
        fakeplayer.setAccessible(true);
        this.playerhider = Class.forName("ru.armagidon.poseplugin.api.utils.nms.PlayerHider_"+ReflectionTools.nmsVersion()).getDeclaredConstructor();
        playerhider.setAccessible(true);
        itemUtil = Class.forName("ru.armagidon.poseplugin.api.utils.nms.item.ItemUtil_"+ReflectionTools.nmsVersion()).getDeclaredConstructor(ItemStack.class);
        itemUtil.setAccessible(true);
    }

    @SneakyThrows
    public FakePlayer createFakePlayer(Player player, Pose pose){
        return (FakePlayer) fakeplayer.newInstance(player,pose);
    }

    @SneakyThrows
    public PlayerHider createPlayerHider(){
        return (PlayerHider) playerhider.newInstance();
    }

    @SneakyThrows
    public ItemUtil createItemUtil(ItemStack item){
        return (ItemUtil) itemUtil.newInstance(item);
    }
}
