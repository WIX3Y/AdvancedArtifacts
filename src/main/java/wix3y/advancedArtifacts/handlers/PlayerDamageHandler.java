package wix3y.advancedArtifacts.handlers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import wix3y.advancedArtifacts.AdvancedArtifacts;

public class PlayerDamageHandler implements Listener {
    private final InventoryHandler inventoryHandler;

    public PlayerDamageHandler(AdvancedArtifacts plugin, InventoryHandler inventoryHandler) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.inventoryHandler = inventoryHandler;
    }

    /**
     * Modify player's damage taken based on their artifacts
     *
     * @param event the player take damage event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getEntity().hasMetadata("NPC")) {
            return;
        }

        EntityDamageEvent.DamageCause damageCause = event.getCause();
        double damage = event.getDamage();
        double newDamage;

        if (damageCause == EntityDamageEvent.DamageCause.FALL || damageCause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_fall_damage");
        }
        else if (damageCause == EntityDamageEvent.DamageCause.CONTACT) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_contact_damage");
        }
        else if (damageCause == EntityDamageEvent.DamageCause.WITHER) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_wither_damage");
        }
        else if (damageCause == EntityDamageEvent.DamageCause.POISON) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_poison_damage");
        }
        else if (damageCause == EntityDamageEvent.DamageCause.THORNS) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_thorns_damage");
        }
        else if (damageCause == EntityDamageEvent.DamageCause.CAMPFIRE || damageCause == EntityDamageEvent.DamageCause.FIRE || damageCause == EntityDamageEvent.DamageCause.FIRE_TICK || damageCause == EntityDamageEvent.DamageCause.HOT_FLOOR || damageCause == EntityDamageEvent.DamageCause.LAVA) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_fire_damage");
        }
        else {
            return;
        }

        if (newDamage == 0) {
            event.setCancelled(true);
        }
        else {
            event.setDamage(newDamage);
        }
    }

    /**
     * Modify player's damage taken based on their artifacts
     *
     * @param event the player take damage event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTakeDamageFromEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getEntity().hasMetadata("NPC")) {
            return;
        }

        Entity damagerEntity = event.getDamager();
        String damager;

        if (damagerEntity instanceof LivingEntity entity) {
            damager = entity.getType().toString();
        }
        else if (damagerEntity instanceof Projectile projectile) {
            ProjectileSource projectileSource = projectile.getShooter();
            if (projectileSource instanceof LivingEntity entity) {
                damager = entity.getType().toString();
            }
            else {
                return;
            }
        }
        else {
            return;
        }

        EntityDamageEvent.DamageCause damageCause = event.getCause();
        double damage = event.getDamage();
        double newDamage;

        if (damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || damageCause == EntityDamageEvent.DamageCause.PROJECTILE) {
            newDamage = damage * inventoryHandler.getMinItemPercentage(player.getUniqueId().toString(), "reduce_mob_damage_" + damager.toLowerCase());
            if (newDamage == 0) {
                event.setCancelled(true);
            }
            else {
                event.setDamage(newDamage);
            }
        }
    }

    /**
     * Modify player's damage dealt based on their artifacts
     *
     * @param event the player deal damage event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDealDamageToEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (event.getEntity().hasMetadata("NPC")) {
            return;
        }

        Entity damagedEntity = event.getEntity();
        String mob;

        if (damagedEntity instanceof LivingEntity entity) {
            mob = entity.getType().toString();
        }
        else {
            return;
        }

        EntityDamageEvent.DamageCause damageCause = event.getCause();
        double damage = event.getDamage();
        double newDamage;

        if (damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            newDamage = damage * inventoryHandler.getMaxItemPercentage(player.getUniqueId().toString(), "increase_mob_damage_" + mob.toLowerCase());
            if (newDamage == 0) {
                event.setCancelled(true);
            }
            else {
                event.setDamage(newDamage);
            }
        }
    }

}