package ru.armagidon.poseplugin.api.poses.lay;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.sit.SitDriver;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;
import ru.armagidon.poseplugin.utils.nms.FakePlayer;

import static net.minecraft.server.v1_15_R1.MobEffects.INVISIBILITY;
import static org.bukkit.Material.*;
import static ru.armagidon.poseplugin.utils.nms.NMSUtils.sendPacket;

public class LayPose extends PluginPose {
    private static final long HIDE_DELAY = 3;
    private final FakePlayer fake;
    private final SitDriver driver;
    private boolean started = false;
    private boolean spawned = false;

    private final boolean preventInvisibility = getBoolean("prevent-use-when-invisible");

    private final BukkitTask invisibleTick;

    public LayPose(Player player) {
        super(player);
        this.driver = new SitDriver(player, () -> stop(true));
        this.fake = new FakePlayer(player, getBoolean("headrotation"),
                getBoolean("player-invulnerable"), getBoolean("swing-animation"));
        invisibleTick = Bukkit.getScheduler().runTaskTimer(PosePlugin.getInstance(), () -> {
            hideParent();
            if (!preventInvisibility) {
                if (spawned) {
                    if (getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        Bukkit.getOnlinePlayers().forEach(this::stopAnimation);
                        spawned = false;
                    }
                } else {
                    if (!getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        Bukkit.getOnlinePlayers().forEach(this::playAnimation);
                        spawned = true;
                    }
                }
            }
        }, 0, 1);
    }

    @Override
    public void play(Player receiver, boolean log) {
        if (!preventInvisibility) {
            if (!getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                playAnimation0(receiver);
            }
        } else {
            playAnimation0(receiver);
        }
        if (!started) {
            driver.takeASeat();
            super.play(receiver, log);
            getPlayer().setCollidable(false);
            started = true;
            Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), this::hideParent, HIDE_DELAY);
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        getPlayer().setCollidable(true);
        Bukkit.getOnlinePlayers().forEach(online -> {
            online.showPlayer(PosePlugin.getInstance(), getPlayer());
            stopAnimation(online);
        });
        driver.standUp();
        showParent();
        invisibleTick.cancel();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    @Override
    public String getSectionName() {
        return "lay";
    }

    private void playAnimation0(Player receiver) {
        if (receiver == null) {
            Bukkit.getOnlinePlayers().forEach(online -> {
                online.hidePlayer(PosePlugin.getInstance(), getPlayer());
                playAnimation(online);
            });
        } else {
            receiver.hidePlayer(PosePlugin.getInstance(), getPlayer());
            playAnimation(receiver);
        }
    }

    private void playAnimation(Player receiver) {
        fake.spawn(receiver);
    }

    private void stopAnimation(Player receiver) {
        fake.remove(receiver);
    }

    private void hideParent() {
        EntityPlayer player = ((CraftPlayer) getPlayer()).getHandle();
        PacketPlayOutEntityEffect effect = new PacketPlayOutEntityEffect(player.getId(), new MobEffect(INVISIBILITY, Short.MAX_VALUE, 1, false, false));
        sendPacket(getPlayer(), effect);
        player.setInvisible(true);
    }

    private void showParent() {
        EntityPlayer player = ((CraftPlayer) getPlayer()).getHandle();
        PacketPlayOutRemoveEntityEffect effect = new PacketPlayOutRemoveEntityEffect(player.getId(), INVISIBILITY);
        sendPacket(getPlayer(), effect);
        player.setInvisible(false);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true);
        sendPacket(getPlayer(), metadata);
    }

    @PersonalEventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (!(e.getItem().getType().equals(POTION) || e.getItem().getType().equals(SPLASH_POTION) || e.getItem().getType().equals(LINGERING_POTION)))
            return;
        PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
        if (!meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY))
            return;
        if (preventInvisibility) {
            e.setCancelled(true);
            PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
        }
    }

    @PersonalEventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (!(e.getItem().getType().equals(POTION) || e.getItem().getType().equals(SPLASH_POTION) || e.getItem().getType().equals(LINGERING_POTION)))
            return;
        PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
        if (!meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY))
            return;
        if (preventInvisibility) {
            e.setCancelled(true);
            PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
        }
    }
}
