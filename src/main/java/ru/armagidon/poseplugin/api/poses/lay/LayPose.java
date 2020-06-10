package ru.armagidon.poseplugin.api.poses.lay;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public class LayPose extends PluginPose
{
    private final FakePlayer fake;
    private final SitDriver driver;
    private boolean started = false;
    private boolean spawned = false;

    private boolean prevent_invisibility = getBoolean("prevent-use-when-invisible");
    private BukkitTask fakePlayerTickerTask;

    public LayPose(Player player) {
        super(player);
        this.driver = new SitDriver(player, ()->stop(true));
        this.fake = new FakePlayer(player);
        initTickModules();
    }

    @Override
    protected void initTickModules() {
        //Tick invisibility
        addTickModule(() -> {
            hideParent();
            if(!prevent_invisibility) {
                if (spawned) {
                    if (getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        fake.broadCastRemove();
                        spawned = false;
                    }
                } else {
                    if (!getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        fake.broadCastSpawn();
                        spawned = true;
                    }
                }
            }
        });
        addTickModule(driver::tick);
        addTickModule(fake::tick);
        //Tick fake player
        fakePlayerTickerTask = Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), ()-> addTickModule(fake.tickLook()), 10);
    }

    @Override
    public void play(Player receiver,boolean log) {
        if(!prevent_invisibility) {
            if (!getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                playAnimation0(receiver);
            }
        } else {
            playAnimation0(receiver);
        }
        if(!started){
            hideParent();
            driver.takeASeat();
            super.play(receiver,log);
            getPlayer().setCollidable(false);
            started = true;
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        getPlayer().setCollidable(true);
        Bukkit.getOnlinePlayers().forEach(online->{
            online.showPlayer(PosePlugin.getInstance(),getPlayer());
            fake.broadCastRemove();
        });
        driver.standUp();
        if(fakePlayerTickerTask.isCancelled()) fakePlayerTickerTask.cancel();
        showParent();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    @Override
    public String getSectionName() {
        return "lay";
    }

    private void playAnimation0(Player receiver){
        if (receiver == null) {
            Bukkit.getOnlinePlayers().forEach(online-> online.hidePlayer(PosePlugin.getInstance(), getPlayer()));
            fake.broadCastSpawn();
        } else {
            receiver.hidePlayer(PosePlugin.getInstance(),getPlayer());
            fake.spawnToPlayer(receiver);
        }
    }

    private void hideParent(){
        EntityPlayer player = ((CraftPlayer) getPlayer()).getHandle();
        PacketPlayOutEntityEffect effect = new PacketPlayOutEntityEffect(player.getId(), new MobEffect(INVISIBILITY, Short.MAX_VALUE, 1, false, false));
        sendPacket(getPlayer(), effect);
        player.setInvisible(true);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            sendPacket(onlinePlayer, metadata);
        }
    }

    private void showParent(){
        if(!getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            EntityPlayer player = ((CraftPlayer) getPlayer()).getHandle();
            PacketPlayOutRemoveEntityEffect effect = new PacketPlayOutRemoveEntityEffect(player.getId(), INVISIBILITY);
            sendPacket(getPlayer(), effect);
            player.setInvisible(false);
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                sendPacket(onlinePlayer, metadata);
            }
        }
    }
    @PersonalEventHandler
    public void onConsume(PlayerItemConsumeEvent e){
        if(e.getItem().getType().equals(POTION)||e.getItem().getType().equals(SPLASH_POTION)||e.getItem().getType().equals(LINGERING_POTION)){
            PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
            if (meta != null) {
                if(meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY)){
                    if(prevent_invisibility){
                        e.setCancelled(true);
                        PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
                    }
                }
            }
        }
    }

    @PersonalEventHandler
    public void onInteract(PlayerInteractEvent e){
        if(e.getItem()==null) return;
        if(e.getItem().getType().equals(POTION)||e.getItem().getType().equals(SPLASH_POTION)||e.getItem().getType().equals(LINGERING_POTION)){
            PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
            if(meta!=null) {
                if (meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY)) {
                    if (prevent_invisibility) {
                        e.setCancelled(true);
                        PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        fake.handleHitBox(event);
    }
}
