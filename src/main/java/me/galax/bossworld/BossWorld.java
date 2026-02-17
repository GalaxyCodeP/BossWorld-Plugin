package me.galax.bossworld;

import me.galax.bossworld.commands.BossCommand;
import me.galax.bossworld.hooks.MythicMobsHook;
import me.galax.bossworld.listeners.BossAntiCheatListener;
import me.galax.bossworld.listeners.BossListener;
import me.galax.bossworld.manager.BossManager;
import me.galax.bossworld.manager.ConfigManager;
import me.galax.bossworld.manager.RewardManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BossWorld extends JavaPlugin {
  private static BossWorld instance;

  private ConfigManager configManager;

  private BossManager bossManager;

  private RewardManager rewardManager;

  private MythicMobsHook mythicMobsHook;

  public void onEnable() {
    instance = this;
    if (getServer().getPluginManager().getPlugin("MythicMobs") == null) {
      getLogger().severe("MythicMobs not found! This plugin requires MythicMobs to work.");
      getServer().getPluginManager().disablePlugin((Plugin) this);
      return;
    }
    this.mythicMobsHook = new MythicMobsHook(this);
    this.configManager = new ConfigManager(this);
    this.rewardManager = new RewardManager(this);
    this.bossManager = new BossManager(this);
    getCommand("bossworld").setExecutor((CommandExecutor) new BossCommand(this));
    getServer().getPluginManager().registerEvents((Listener) new BossListener(this), (Plugin) this);
    getServer().getPluginManager().registerEvents((Listener) new BossAntiCheatListener(this), (Plugin) this);
    getLogger().info("BossWorld has been enabled!");
  }

  public void onDisable() {
    if (this.bossManager != null) {
      this.bossManager.cleanupAllBosses();
      this.bossManager.stopAllTimers();
    }
    getLogger().info("BossWorld has been disabled!");
  }

  public static BossWorld getInstance() {
    return instance;
  }

  public ConfigManager getConfigManager() {
    return this.configManager;
  }

  public BossManager getBossManager() {
    return this.bossManager;
  }

  public RewardManager getRewardManager() {
    return this.rewardManager;
  }

  public MythicMobsHook getMythicMobsHook() {
    return this.mythicMobsHook;
  }
}

/*
 * Location:
 * C:\Users\galax\Downloads\all\BossWorld-1.0-SNAPSHOT.jar!\me\galax\bossworld\
 * BossWorld.class
 * Java compiler version: 21 (65.0)
 * JD-Core Version: 1.1.3
 */