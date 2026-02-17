package me.galax.bossworld.listeners;

import java.util.List;
import me.galax.bossworld.BossWorld;
import me.galax.bossworld.objects.BossInstance;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class BossAntiCheatListener implements Listener {
  private final BossWorld plugin;

  public BossAntiCheatListener(BossWorld plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onCommand(PlayerCommandPreprocessEvent event) {
    if (!plugin.getConfigManager().getConfig().getBoolean("anti-cheat.enabled", true))
      return;

    Player player = event.getPlayer();
    if (player.hasPermission("bossworld.anticheat.bypass"))
      return;

    BossInstance nearbyBoss = getNearbyActiveBoss(player.getLocation());
    if (nearbyBoss == null)
      return;

    String message = event.getMessage().toLowerCase();
    List<String> blocked = plugin.getConfigManager().getConfig().getStringList("anti-cheat.blocked-commands");

    for (String cmd : blocked) {
      if (message.startsWith(cmd.toLowerCase() + " ") || message.equalsIgnoreCase(cmd)) {
        event.setCancelled(true);
        String warning = plugin.getConfigManager().getConfig().getString("anti-cheat.messages.command-blocked",
            "&cBlocked!");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', warning));
        return;
      }
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    if (!plugin.getConfigManager().getConfig().getBoolean("anti-cheat.enabled", true))
      return;

    Player player = event.getPlayer();
    if (player.hasPermission("bossworld.anticheat.bypass"))
      return;

    if (event.getTo() == null || (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
        event.getFrom().getBlockZ() == event.getTo().getBlockZ())) {
      return;
    }

    BossInstance nearbyBoss = getNearbyActiveBoss(event.getTo());
    if (nearbyBoss != null) {
      checkAndDisableCheats(player);
    }
  }

  private BossInstance getNearbyActiveBoss(Location location) {
    if (location == null)
      return null;

    double radius = plugin.getConfigManager().getConfig().getDouble("anti-cheat.fight-radius", 50.0);

    for (BossInstance boss : plugin.getBossManager().getBosses()) {
      if (boss.getCurrentBossUUID() == null)
        continue; // Boss not active

      Location spawnLoc = boss.getSpawnLocation();
      if (spawnLoc == null || !location.getWorld().equals(spawnLoc.getWorld()))
        continue;

      if (location.distance(spawnLoc) <= radius) {
        return boss;
      }
    }
    return null;
  }

  private void checkAndDisableCheats(Player player) {
    if (plugin.getServer().getPluginManager().isPluginEnabled("CMI")) {
      try {
        Class<?> cmiClass = Class.forName("com.Zrips.CMI.CMI");
        Object cmiInstance = cmiClass.getMethod("getInstance").invoke(null);
        Object playerManager = cmiClass.getMethod("getPlayerManager").invoke(cmiInstance);
        Object cmiUser = playerManager.getClass().getMethod("getUser", Player.class).invoke(playerManager, player);

        if (cmiUser != null) {
          boolean isFly = (Boolean) cmiUser.getClass().getMethod("isFly").invoke(cmiUser);
          if (isFly) {
            cmiUser.getClass().getMethod("setFly", boolean.class).invoke(cmiUser, false);
            sendWarning(player, "anti-cheat.messages.fly-disabled");
          }

          boolean isGod = (Boolean) cmiUser.getClass().getMethod("isGod").invoke(cmiUser);
          if (isGod) {
            cmiUser.getClass().getMethod("setGod", boolean.class).invoke(cmiUser, false);
            sendWarning(player, "anti-cheat.messages.god-disabled");
          }
        }
      } catch (Exception e) {
        fallbackDisable(player);
      }
    } else {
      fallbackDisable(player);
    }
  }

  private void sendWarning(Player player, String configPath) {
    String msg = plugin.getConfigManager().getConfig().getString(configPath);
    if (msg != null && !msg.isEmpty()) {
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
  }

  private void fallbackDisable(Player player) {
    if (player.getAllowFlight()) {
      player.setAllowFlight(false);
      player.setFlying(false);
      sendWarning(player, "anti-cheat.messages.fly-disabled");
    }
  }
}