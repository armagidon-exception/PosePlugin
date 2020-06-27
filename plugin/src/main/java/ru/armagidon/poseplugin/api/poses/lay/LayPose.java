package ru.armagidon.poseplugin.api.poses.lay;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.sit.SitDriver;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.utils.nms.npc.FakePlayer;

public class LayPose extends PluginPose {

    private final FakePlayer fakePlayer;
    private final SitDriver driver;

    private boolean started = false;

    private boolean preventInvisible;

    public LayPose(Player target) {
        super(target);
        this.fakePlayer = PosePlugin.getInstance().getFakePlayerFactory().createInstance(getPlayer(), Pose.SLEEPING);
        this.fakePlayer.setInvulnerable(getBoolean("player-invulnerable"));
        this.preventInvisible = getBoolean("prevent-use-when-invisible");
        fakePlayer.setInvulnerable(getBoolean("player-invulnerable"));
        fakePlayer.setHeadRotationEnabled(getBoolean("head-rotation"));
        fakePlayer.setSwingAnimationEnabled(getBoolean("swing-animation"));
        fakePlayer.setUpdateEquipmentEnabled(getBoolean("update-equipment"));
        fakePlayer.setUpdateOverlaysEnabled(getBoolean("update-overlays"));
        fakePlayer.setViewDistance(getInt("view-distance"));
        this.driver = new SitDriver(target, (e)-> {
            if(!callStopEvent(EnumPose.LYING, getPosePluginPlayer(), true, StopAnimationEvent.StopCause.STOPPED)){
                e.setCancelled(true);
            }
        });
        initTickModules();
    }

    @Override
    protected void initTickModules() {
        addTickModule(this::disappear);
        addTickModule(()->{
                if(getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)){
                    if(preventInvisible){
                        getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
                        PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, getPlayer());
                    } else {
                        if(!fakePlayer.isInvisible()){
                            fakePlayer.setInvisible(true);
                        }
                    }
                } else {
                    if(!preventInvisible){
                        if(fakePlayer.isInvisible()){
                            fakePlayer.setInvisible(false);
                        }
                    }
                }

        });
        addTickModule(driver::tick);
        addTickModule(fakePlayer::tick);
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver, log);
        if(!started) {
            driver.takeASeat();
            disappear();
            started=true;
        }
        if(receiver==null) {
            hideParent();
            fakePlayer.broadCastSpawn();
        } else {
            hideParent0(receiver);
            fakePlayer.spawnToPlayer(receiver);
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        fakePlayer.remove();
        driver.standUp();
        showParent();
        appear();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    @Override
    public String getSectionName() {
        return "lay";
    }

    @PersonalEventHandler
    public void gameModeChange(PlayerGameModeChangeEvent event){
        fakePlayer.checkGameMode(event.getNewGameMode());
    }

    @PersonalEventHandler
    public void onUsePotionTrowable(PlayerInteractEvent e){
        if(preventInvisible) {
            if (e.getItem() == null) return;
            ItemStack hand = e.getItem();
            switch (hand.getType()) {
                case POTION:
                case LINGERING_POTION:
                case SPLASH_POTION: {
                    PotionMeta meta = (PotionMeta) hand.getItemMeta();
                    if (meta != null) {
                        if (meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY)) {
                            e.setCancelled(true);
                            PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
                        }
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    @PersonalEventHandler
    public void onUsePotionDrinkable(PlayerItemConsumeEvent e){
        if(preventInvisible) {
            ItemStack hand = e.getItem();
            switch (hand.getType()) {
                case POTION:
                case LINGERING_POTION:
                case SPLASH_POTION: {
                    PotionMeta meta = (PotionMeta) hand.getItemMeta();
                    if (meta != null) {
                        if (meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY)) {
                            e.setCancelled(true);
                            PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
                        }
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(event.getDamager().getType().equals(EntityType.PLAYER)) {
            fakePlayer.getHitBox().damageByPlayer(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBurn(EntityCombustEvent event){
        fakePlayer.getHitBox().onBurn(event);
    }

    @EventHandler
    public void onProjectile(ProjectileHitEvent event){
        fakePlayer.getHitBox().hitWithSpectralArrow(event);
    }

    private void hideParent(){
        Bukkit.getOnlinePlayers().forEach(this::hideParent0);
    }

    private void hideParent0(Player receiver){
        if(receiver.equals(getPlayer())) return;
        receiver.hidePlayer(PosePlugin.getInstance(), getPlayer());
    }

    private void showParent(){
        Bukkit.getOnlinePlayers().forEach(player->{
            if(player.equals(getPlayer())) return;
            player.showPlayer(PosePlugin.getInstance(), getPlayer());
        });
    }

    private void disappear(){
        NMSUtils.setInvisible(getPlayer(), true);
        //getPlayer().hidePlayer(PosePlugin.getInstance(), getPlayer());
    }

    private void appear(){
        NMSUtils.setInvisible(getPlayer(), false);
        //getPlayer().showPlayer(PosePlugin.getInstance(), getPlayer());
    }

}
