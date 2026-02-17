package me.galax.bossworld.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import me.galax.bossworld.BossWorld;
import me.galax.bossworld.objects.BossInstance;
import me.galax.bossworld.objects.RewardItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardManager {
  private final BossWorld plugin;
  private final Map<String, List<RewardItem>> bossRewards = new HashMap<>();
  private final String GUI_TITLE_PREFIX = ChatColor.DARK_BLUE + "Boss Rewards: ";

  public RewardManager(BossWorld plugin) {
    this.plugin = plugin;
    loadRewards();
  }

  public void loadRewards() {
    this.bossRewards.clear();
    ConfigurationSection section = this.plugin.getConfigManager().getData().getConfigurationSection("rewards");
    if (section != null) {
      for (String bossId : section.getKeys(false)) {
        List<RewardItem> items = new ArrayList<>();
        ConfigurationSection bossSection = section.getConfigurationSection(bossId);
        if (bossSection != null) {
          for (String key : bossSection.getKeys(false)) {
            try {
              ItemStack item = bossSection.getItemStack(key + ".item");
              double chance = bossSection.getDouble(key + ".chance");
              if (item != null) {
                items.add(new RewardItem(item, chance));
              }
            } catch (Exception e) {
              plugin.getLogger().warning("Error loading reward for boss " + bossId + " key " + key);
            }
          }
        }
        this.bossRewards.put(bossId, items);
      }
    }
  }

  public void saveRewards() {
    this.plugin.getConfigManager().getData().set("rewards", null);
    for (Map.Entry<String, List<RewardItem>> entry : this.bossRewards.entrySet()) {
      String bossId = entry.getKey();
      List<RewardItem> items = entry.getValue();
      for (int i = 0; i < items.size(); i++) {
        RewardItem ri = items.get(i);
        this.plugin.getConfigManager().getData().set("rewards." + bossId + "." + i + ".item", ri.getItem());
        this.plugin.getConfigManager().getData().set("rewards." + bossId + "." + i + ".chance", ri.getChance());
      }
    }
    this.plugin.getConfigManager().saveData();
  }

  public void openRewardEditor(Player player, String bossId) {
    if (this.plugin.getBossManager().getBoss(bossId) == null) {
      player.sendMessage(ChatColor.RED + "Boss ID not found: " + bossId);
      return;
    }

    Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + bossId);
    List<RewardItem> rewards = this.bossRewards.computeIfAbsent(bossId, k -> new ArrayList<>());

    for (RewardItem ri : rewards) {
      ItemStack display = ri.getItem().clone();
      ItemMeta meta = display.getItemMeta();
      List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
      lore.add("");
      lore.add(ChatColor.GRAY + "Chance: " + ChatColor.YELLOW + ri.getChance() + "%");
      lore.add("");
      lore.add(ChatColor.DARK_GRAY + "Left-Click: -1%");
      lore.add(ChatColor.DARK_GRAY + "Right-Click: +1%");
      lore.add(ChatColor.DARK_GRAY + "Shift-Left: Delete");
      meta.setLore(lore);
      // Store bossId in persistent data container if needed, but title is enough for
      // now
      display.setItemMeta(meta);
      inv.addItem(display);
    }
    player.openInventory(inv);
  }

  public void handleGuiClick(Player player, String inventoryTitle, ItemStack clickedItem, int slot,
      ClickType clickType) {
    if (!inventoryTitle.startsWith(GUI_TITLE_PREFIX))
      return;
    String bossId = inventoryTitle.substring(GUI_TITLE_PREFIX.length());

    if (clickedItem == null || clickedItem.getType() == Material.AIR)
      return;

    List<RewardItem> rewards = this.bossRewards.get(bossId);
    if (rewards == null)
      return;

    // Direct mapping index assumption (risky if grid is not perfectly sequential,
    // but standard for this simple GUI)
    // Actually, let's find the clicked item in the list by reference or creating a
    // map is better,
    // but for simplicity, assumes order is preserved.
    // However, slot index might not match list index exactly if items are added
    // weirdly.
    // Better strategy: The GUI re-renders the list in order. So slot 0 = index 0.

    if (slot >= rewards.size())
      return;

    if (clickType == ClickType.SHIFT_LEFT) {
      rewards.remove(slot);
      player.sendMessage(ChatColor.RED + "Deleted reward item.");
    } else if (clickType == ClickType.RIGHT) {
      RewardItem ri = rewards.get(slot);
      ri.setChance(Math.min(100.0, ri.getChance() + 1.0));
      player.sendMessage(ChatColor.GREEN + "Chance: " + ri.getChance() + "%");
    } else if (clickType == ClickType.LEFT) {
      RewardItem ri = rewards.get(slot);
      ri.setChance(Math.max(0.0, ri.getChance() - 1.0));
      player.sendMessage(ChatColor.YELLOW + "Chance: " + ri.getChance() + "%");
    }

    saveRewards();
    openRewardEditor(player, bossId);
  }

  public void addItemToRewards(Player player, String inventoryTitle, ItemStack item) {
    if (!inventoryTitle.startsWith(GUI_TITLE_PREFIX))
      return;
    String bossId = inventoryTitle.substring(GUI_TITLE_PREFIX.length());

    if (item == null || item.getType() == Material.AIR)
      return;

    List<RewardItem> rewards = this.bossRewards.computeIfAbsent(bossId, k -> new ArrayList<>());
    rewards.add(new RewardItem(item.clone(), 10.0));
    saveRewards();
    player.sendMessage(ChatColor.GREEN + "Added reward item (10% chance) to boss " + bossId);
    openRewardEditor(player, bossId);
  }

  public void distributeRewards(BossInstance bossInstance, Map<UUID, Double> damageMap) {
    if (bossInstance == null || damageMap.isEmpty())
      return;

    List<RewardItem> rewards = this.bossRewards.get(bossInstance.getBossId());
    if (rewards == null || rewards.isEmpty()) {
      // plugins.getLogger().info("No rewards configured for " +
      // bossInstance.getBossId());
      // Just broadcast top 3 even if no rewards?
    }

    double totalDamage = damageMap.values().stream().mapToDouble(Double::doubleValue).sum();
    double minDamagePercent = this.plugin.getConfigManager().getConfig()
        .getDouble("reward-requirements.minimum-damage-percent", 10.0);
    boolean mustBeInBossWorld = this.plugin.getConfigManager().getConfig()
        .getBoolean("reward-requirements.must-be-in-boss-world", true);

    String bossWorldName = (bossInstance.getSpawnLocation() != null)
        ? bossInstance.getSpawnLocation().getWorld().getName()
        : null;

    List<Map.Entry<UUID, Double>> sortedDamage = damageMap.entrySet().stream()
        .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
        .collect(Collectors.toList());

    broadcastTop3(bossInstance, sortedDamage);

    if (rewards == null || rewards.isEmpty())
      return;

    for (int i = 0; i < sortedDamage.size(); i++) {
      UUID uuid = sortedDamage.get(i).getKey();
      Player p = Bukkit.getPlayer(uuid);
      if (p != null && p.isOnline()) {
        double playerDamage = sortedDamage.get(i).getValue();
        double damagePercent = (totalDamage > 0) ? (playerDamage / totalDamage * 100.0) : 0;

        if (damagePercent < minDamagePercent) {
          String msg = this.plugin.getConfigManager().getConfig().getString("messages.insufficient-damage",
              "&cInsufficient damage %min%%. You did %actual%%");
          p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg
              .replace("%min%", String.format("%.1f", minDamagePercent))
              .replace("%actual%", String.format("%.1f", damagePercent))));
        } else if (mustBeInBossWorld && bossWorldName != null && !p.getWorld().getName().equals(bossWorldName)) {
          String msg = this.plugin.getConfigManager().getConfig().getString("messages.wrong-world", "&cWrong world!");
          p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        } else {
          double multiplier = 1.0;
          if (i == 0)
            multiplier = 2.0;
          else if (i == 1)
            multiplier = 1.5;
          else if (i == 2)
            multiplier = 1.2;

          giveRewards(p, rewards, multiplier);
        }
      }
    }
  }

  private void giveRewards(Player player, List<RewardItem> rewards, double multiplier) {
    Random random = new Random();
    for (RewardItem ri : rewards) {
      double roll = random.nextDouble() * 100.0;
      if (roll <= ri.getChance() * multiplier) {
        ItemStack item = ri.getItem().clone();
        Map<Integer, ItemStack> left = player.getInventory().addItem(item);
        if (!left.isEmpty()) {
          left.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
        }
      }
    }
  }

  private void broadcastTop3(BossInstance boss, List<Map.Entry<UUID, Double>> sortedDamage) {
    String prefix = this.plugin.getConfigManager().getConfig().getString("messages.prefix", "");
    String header = this.plugin.getConfigManager().getConfig().getString("messages.top-damage-header", "")
        .replace("%boss%", boss.getDisplayName());
    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + header));

    for (int i = 0; i < Math.min(3, sortedDamage.size()); i++) {
      UUID uuid = sortedDamage.get(i).getKey();
      String name = Bukkit.getOfflinePlayer(uuid).getName();
      double damage = sortedDamage.get(i).getValue();
      String line = this.plugin.getConfigManager().getConfig().getString("messages.top-damage-format")
          .replace("%rank%", String.valueOf(i + 1))
          .replace("%player%", (name != null) ? name : "Unknown")
          .replace("%damage%", String.format("%.2f", damage));
      Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + line));
    }
  }

  public String getGuiTitlePrefix() {
    return GUI_TITLE_PREFIX;
  }
}