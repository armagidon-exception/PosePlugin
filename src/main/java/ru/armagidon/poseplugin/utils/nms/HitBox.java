package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.ticking.Tickable;

public class HitBox implements Tickable
{
    private final Player parent;
    private final FakePlayer npc;
    private Slime hitbox;

    public HitBox(Player player, FakePlayer npc) {
        this.parent = player;
        this.npc = npc;
    }

    public void spawn(Location location){
        Location spawnLoc = location.clone().add(0,0.5,0);
        if(location.getWorld()==null) return;
        if(hitbox==null) {
            this.hitbox = location.getWorld().spawn(spawnLoc, Slime.class, (slime) -> {
                slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                slime.setGravity(false);
                slime.setInvulnerable(true);
                slime.setAI(false);
                slime.setSize(1);
                AttributeInstance slimeAttribute = slime.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                AttributeInstance playerAttribute = parent.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (slimeAttribute != null && playerAttribute != null)
                    slimeAttribute.setBaseValue(playerAttribute.getValue());
                slime.setHealth(parent.getHealth());
            });
        }
    }

    public void remove(){
        if(hitbox!=null) {
            hitbox.remove();
            hitbox = null;
        }
    }

    public void onBurn(EntityCombustEvent event){
        if(hitbox!=null&&event.getEntity().equals(hitbox)){
            parent.setFireTicks(event.getDuration()*20);
            event.setDuration(0);
        }
    }

    public void hitWithSpectralArrow(ProjectileHitEvent event){
        Entity hit = event.getHitEntity();
        if(hit==null) return;
        Projectile arrow = event.getEntity();
        if(arrow.getType().equals(EntityType.SPECTRAL_ARROW)){
            SpectralArrow a = (SpectralArrow) arrow;
            if(hit.equals(hitbox)||hit.equals(parent)){
                a.setGlowingTicks(0);
            }
        }
    }

    public void damageByPlayer(EntityDamageByEntityEvent event){
        if(!npc.isInvulnerable()&&hitbox!=null){
            LivingEntity slime = (LivingEntity) event.getEntity();
            if(slime.equals(hitbox)) {
                if(event.getDamager().equals(parent)) {
                    event.setCancelled(true);
                    return;
                }
                parent.damage(event.getDamage(), event.getDamager());
                Bukkit.getOnlinePlayers().forEach(p->
                        p.playSound(parent.getLocation(), Sound.ENTITY_PLAYER_HURT, 1,1));
                npc.animation((byte)2);
            }
        }
    }

    @Override
    public void tick() {
        if(hitbox!=null){
            hitbox.setHealth(hitbox.getMaxHealth());
        }
    }
}
