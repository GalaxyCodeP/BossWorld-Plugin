package me.galax.bossworld.manager;

import java.io.File;
import java.io.IOException;
import me.galax.bossworld.BossWorld;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
  private final BossWorld plugin;
  
  private File configFile;
  
  private FileConfiguration config;
  
  private File dataFile;
  
  private FileConfiguration data;
  
  public ConfigManager(BossWorld plugin) {
    this.plugin = plugin;
    setup();
  }
  
  public void setup() {
    if (!this.plugin.getDataFolder().exists())
      this.plugin.getDataFolder().mkdir(); 
    this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
    if (!this.configFile.exists())
      this.plugin.saveResource("config.yml", false); 
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(this.configFile);
    this.dataFile = new File(this.plugin.getDataFolder(), "data.yml");
    if (!this.dataFile.exists())
      try {
        this.dataFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }  
    this.data = (FileConfiguration)YamlConfiguration.loadConfiguration(this.dataFile);
  }
  
  public FileConfiguration getConfig() {
    return this.config;
  }
  
  public FileConfiguration getData() {
    return this.data;
  }
  
  public void saveData() {
    try {
      this.data.save(this.dataFile);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void reload() {
    this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(this.configFile);
    this.data = (FileConfiguration)YamlConfiguration.loadConfiguration(this.dataFile);
  }
}


/* Location:              C:\Users\galax\Downloads\all\BossWorld-1.0-SNAPSHOT.jar!\me\galax\bossworld\manager\ConfigManager.class
 * Java compiler version: 21 (65.0)
 * JD-Core Version:       1.1.3
 */