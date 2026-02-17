package me.galax.bossworld.listeners;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.galax.bossworld.BossWorld;
import me.galax.bossworld.objects.BossInstance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BossListener implements Listener {
  private final BossWorld plugin;

  public BossListener(BossWorld plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBossDamage(EntityDamageByEntityEvent event) {
    BossInstance boss = plugin.getBossManager().getBossByUUID(event.getEntity().getUniqueId());
    if (boss == null)
      return;

    Player player = null;
    if (event.getDamager() instanceof Player) {
      player = (Player) event.getDamager();
    } else if (event.getDamager() instanceof Projectile) {
      Projectile proj = (Projectile) event.getDamager();
      if (proj.getShooter() instanceof Player) {
        player = (Player) proj.getShooter();
      }
    }

    if (player != null) {
      boss.addDamage(player.getUniqueId(), event.getFinalDamage());
    }
  }

  @EventHandler
  public void onBossDeath(MythicMobDeathEvent event) {
    // Find which boss instance this is
    BossInstance boss = plugin.getBossManager().getBossByUUID(event.getEntity().getUniqueId());
    if (boss == null)
      return;

    // Verify it matches the configured mythicmob id
    if (event.getMob().getType().getInternalName().equalsIgnoreCase(boss.getMythicMobId())) {
      plugin.getRewardManager().distributeRewards(boss, boss.getDamageMap());
      boss.cleanupCurrentBoss();

      String msg = plugin.getConfigManager().getConfig().getString("messages.boss-killed", "&eBoss killed!")
          .replace("%boss%", boss.getDisplayName());
      String prefix = plugin.getConfigManager().getConfig().getString("messages.prefix", "");
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    String title = event.getView().getTitle();
    if (!title.startsWith(plugin.getRewardManager().getGuiTitlePrefix()))
      return;

    event.setCancelled(true);
    Player player = (Player) event.getWhoClicked();

    // Player clicking their own inventory to add items
    if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
      ItemStack item = event.getCurrentItem();
      if (item != null && item.getType() != Material.AIR) {
        plugin.getRewardManager().addItemToRewards(player, title, item);
      }
      return;
    }

    // Player clicking the GUI itself
    if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
      plugin.getRewardManager().handleGuiClick(player, title, event.getCurrentItem(), event.getSlot(),
          event.getClick());
    }
  }
}