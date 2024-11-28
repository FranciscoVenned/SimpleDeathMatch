package com.venned.simpledeathmatch.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DurabilityProtectionListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Verifica si la entidad que recibe daño es un jugador
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Restaura la durabilidad de cada pieza de la armadura equipada
            for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
                if (armorPiece != null && armorPiece.getType().getMaxDurability() > 0) {
                    armorPiece.setDurability((short) 0); // Restaura la durabilidad al máximo
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Verifica si quien hace daño es un jugador
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            ItemStack itemInHand = damager.getInventory().getItemInMainHand();

            // Restaura la durabilidad del ítem usado para atacar
            if (itemInHand != null && itemInHand.getType().getMaxDurability() > 0) {
                itemInHand.setDurability((short) 0); // Restaura la durabilidad al máximo
            }
        }
    }
}
