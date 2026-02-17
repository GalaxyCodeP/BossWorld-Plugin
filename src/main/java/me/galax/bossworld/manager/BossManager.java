package me.galax.bossworld.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import me.galax.bossworld.BossWorld;
import me.galax.bossworld.objects.BossInstance;

public class BossManager {
    private final BossWorld plugin;
    private final Map<String, BossInstance> bosses = new HashMap<>();

    public BossManager(BossWorld plugin) {
        this.plugin = plugin;
        loadBosses();
    }

    public void loadBosses() {
        this.bosses.values().forEach(BossInstance::stopTimer);
        this.bosses.clear();

        if (plugin.getConfigManager().getConfig().isConfigurationSection("bosses")) {
            for (String bossId : plugin.getConfigManager().getConfig().getConfigurationSection("bosses")
                    .getKeys(false)) {
                try {
                    BossInstance instance = new BossInstance(plugin, bossId);
                    this.bosses.put(bossId, instance);
                    plugin.getLogger().info("Loaded boss: " + bossId);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load boss '" + bossId + "': " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("No 'bosses' section found in config.yml!");
        }
    }

    public BossInstance getBoss(String bossId) {
        return this.bosses.get(bossId);
    }

    public Collection<BossInstance> getBosses() {
        return this.bosses.values();
    }

    public BossInstance getBossByUUID(UUID uuid) {
        if (uuid == null)
            return null;
        for (BossInstance instance : this.bosses.values()) {
            if (uuid.equals(instance.getCurrentBossUUID())) {
                return instance;
            }
        }
        return null;
    }

    public BossInstance getBossByLocation(Location loc, double radius) {
        if (loc == null || loc.getWorld() == null)
            return null;
        for (BossInstance instance : this.bosses.values()) {
            Location spawnLoc = instance.getSpawnLocation();
            if (spawnLoc != null && spawnLoc.getWorld().equals(loc.getWorld())) {
                if (spawnLoc.distance(loc) <= radius) {
                    return instance;
                }
            }
        }
        return null;
    }

    public void cleanupAllBosses() {
        this.bosses.values().forEach(BossInstance::cleanupCurrentBoss);
    }

    public void stopAllTimers() {
        this.bosses.values().forEach(BossInstance::stopTimer);
    }
}