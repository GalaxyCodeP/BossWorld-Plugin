package me.galax.bossworld.commands;

import me.galax.bossworld.BossWorld;
import me.galax.bossworld.objects.BossInstance;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BossCommand implements CommandExecutor {
  private final BossWorld plugin;

  public BossCommand(BossWorld plugin) {
    this.plugin = plugin;
  }

  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("This command is for players only!");
      return true;
    }

    Player player = (Player) sender;

    // /bossworld - teleport to default boss
    if (args.length == 0) {
      BossInstance defaultBoss = plugin.getBossManager().getBoss("default");
      if (defaultBoss == null) {
        sendPrefixedMessage(player,
            plugin.getConfigManager().getConfig().getString("messages.no-location", "&cNo boss location set!"));
        return true;
      }

      Location loc = defaultBoss.getSpawnLocation();
      if (loc == null) {
        sendPrefixedMessage(player,
            plugin.getConfigManager().getConfig().getString("messages.no-location", "&cNo boss location set!")
                .replace("%boss%", "default"));
      } else {
        player.teleport(loc);
        sendPrefixedMessage(player,
            plugin.getConfigManager().getConfig().getString("messages.teleport-success", "&aTeleported!")
                .replace("%boss%", defaultBoss.getDisplayName()));
      }
      return true;
    }

    // /bossworld <bossId> - teleport to specific boss
    if (args.length == 1 && !isAdminSubcommand(args[0])) {
      String bossId = args[0];
      BossInstance boss = plugin.getBossManager().getBoss(bossId);
      if (boss == null) {
        player.sendMessage(ChatColor.RED + "Boss not found: " + bossId);
        return true;
      }

      Location loc = boss.getSpawnLocation();
      if (loc == null) {
        sendPrefixedMessage(player,
            plugin.getConfigManager().getConfig().getString("messages.no-location", "&cNo boss location set!")
                .replace("%boss%", bossId));
      } else {
        player.teleport(loc);
        sendPrefixedMessage(player,
            plugin.getConfigManager().getConfig().getString("messages.teleport-success", "&aTeleported!")
                .replace("%boss%", boss.getDisplayName()));
      }
      return true;
    }

    // Admin commands
    if (args[0].equalsIgnoreCase("reward")) {
      if (!player.hasPermission("bossworld.admin")) {
        player.sendMessage(ChatColor.RED + "No permission.");
        return true;
      }

      if (args.length >= 3 && args[1].equalsIgnoreCase("edit")) {
        String bossId = args[2];
        plugin.getRewardManager().openRewardEditor(player, bossId);
      } else {
        player.sendMessage(ChatColor.RED + "Usage: /bossworld reward edit <bossId>");
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("setspawn")) {
      if (!player.hasPermission("bossworld.admin")) {
        player.sendMessage(ChatColor.RED + "No permission.");
        return true;
      }

      if (args.length >= 2) {
        String bossId = args[1];
        BossInstance boss = plugin.getBossManager().getBoss(bossId);
        if (boss == null) {
          player.sendMessage(ChatColor.RED + "Boss not found: " + bossId);
          return true;
        }
        boss.setSpawnLocation(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Set spawn location for boss: " + bossId);
      } else {
        player.sendMessage(ChatColor.RED + "Usage: /bossworld setspawn <bossId>");
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("spawn")) {
      if (!player.hasPermission("bossworld.admin")) {
        player.sendMessage(ChatColor.RED + "No permission.");
        return true;
      }

      if (args.length >= 2) {
        String bossId = args[1];
        BossInstance boss = plugin.getBossManager().getBoss(bossId);
        if (boss == null) {
          player.sendMessage(ChatColor.RED + "Boss not found: " + bossId);
          return true;
        }
        boss.spawnBoss();
        player.sendMessage(ChatColor.GREEN + "Spawned boss: " + bossId);
      } else {
        player.sendMessage(ChatColor.RED + "Usage: /bossworld spawn <bossId>");
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("clear")) {
      if (!player.hasPermission("bossworld.admin")) {
        player.sendMessage(ChatColor.RED + "No permission.");
        return true;
      }

      if (args.length >= 2) {
        String bossId = args[1];
        BossInstance boss = plugin.getBossManager().getBoss(bossId);
        if (boss == null) {
          player.sendMessage(ChatColor.RED + "Boss not found: " + bossId);
          return true;
        }
        boss.cleanupCurrentBoss();
        player.sendMessage(ChatColor.YELLOW + "Cleared boss: " + bossId);
      } else {
        // Clear all bosses
        plugin.getBossManager().cleanupAllBosses();
        player.sendMessage(ChatColor.YELLOW + "Cleared all bosses!");
      }
      return true;
    }

    if (args[0].equalsIgnoreCase("reload")) {
      if (!player.hasPermission("bossworld.admin")) {
        player.sendMessage(ChatColor.RED + "No permission.");
        return true;
      }

      plugin.getConfigManager().reload();
      plugin.getBossManager().loadBosses();
      plugin.getRewardManager().loadRewards();
      player.sendMessage(ChatColor.GREEN + "Reloaded configuration!");
      return true;
    }

    if (args[0].equalsIgnoreCase("list")) {
      if (!player.hasPermission("bossworld.admin")) {
        player.sendMessage(ChatColor.RED + "No permission.");
        return true;
      }

      player.sendMessage(ChatColor.GOLD + "=== BossWorld Bosses ===");
      for (BossInstance boss : plugin.getBossManager().getBosses()) {
        String status = boss.getCurrentBossUUID() != null ? ChatColor.GREEN + "ACTIVE" : ChatColor.GRAY + "inactive";
        player.sendMessage(ChatColor.YELLOW + "- " + boss.getBossId() + " (" + boss.getDisplayName() + ") " + status);
      }
      return true;
    }

    player.sendMessage(ChatColor.RED + "Usage:");
    player.sendMessage(ChatColor.YELLOW + "/bossworld [bossId] - Teleport to boss");
    player.sendMessage(ChatColor.YELLOW + "/bossworld list - List all bosses");
    player.sendMessage(ChatColor.YELLOW + "/bossworld setspawn <bossId> - Set spawn location");
    player.sendMessage(ChatColor.YELLOW + "/bossworld spawn <bossId> - Spawn boss");
    player.sendMessage(ChatColor.YELLOW + "/bossworld clear [bossId] - Clear boss(es)");
    player.sendMessage(ChatColor.YELLOW + "/bossworld reward edit <bossId> - Edit rewards");
    player.sendMessage(ChatColor.YELLOW + "/bossworld reload - Reload config");
    return true;
  }

  private boolean isAdminSubcommand(String arg) {
    return arg.equalsIgnoreCase("reward") ||
        arg.equalsIgnoreCase("setspawn") ||
        arg.equalsIgnoreCase("spawn") ||
        arg.equalsIgnoreCase("clear") ||
        arg.equalsIgnoreCase("reload") ||
        arg.equalsIgnoreCase("list");
  }

  private void sendPrefixedMessage(Player player, String message) {
    if (message == null)
      return;
    String prefix = this.plugin.getConfigManager().getConfig().getString("messages.prefix", "");
    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
  }
}