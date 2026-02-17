package me.galax.bossworld.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.galax.bossworld.BossWorld;
import me.galax.bossworld.util.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class BossInstance {
    private final BossWorld plugin;
    private final String bossId;
    private final String displayName;
    private final String mythicMobId;
    private final long spawnIntervalSeconds;
    private final List<Integer> announcementTimes;
    private final String bossImageUrl;

    private Location spawnLocation;
    private UUID currentBossUUID;
    private final Map<UUID, Double> damageMap = new HashMap<>();
    private int timerTaskId = -1;
    private long nextSpawnTime = 0L;

    public BossInstance(BossWorld plugin, String bossId) {
        this.plugin = plugin;
        this.bossId = bossId;
        ConfigurationSection section = plugin.getConfigManager().getConfig()
                .getConfigurationSection("bosses." + bossId);
        if (section == null) {
            throw new IllegalArgumentException("Boss config not found for id: " + bossId);
        }
        this.displayName = section.getString("display-name", bossId);
        this.mythicMobId = section.getString("mythicmob-id", "SkeletalKing");
        this.spawnIntervalSeconds = section.getLong("spawn-interval", 3600L);
        this.announcementTimes = section.getIntegerList("announcement-times");
        this.bossImageUrl = section.getString("boss-image-url", "");
        loadSpawnLocation();
        startTimer();
    }

    public void loadSpawnLocation() {
        FileConfiguration data = this.plugin.getConfigManager().getData();
        if (data.contains("spawn-locations." + this.bossId)) {
            String worldName = data.getString("spawn-locations." + this.bossId + ".world");
            double x = data.getDouble("spawn-locations." + this.bossId + ".x");
            double y = data.getDouble("spawn-locations." + this.bossId + ".y");
            double z = data.getDouble("spawn-locations." + this.bossId + ".z");
            float yaw = (float) data.getDouble("spawn-locations." + this.bossId + ".yaw");
            float pitch = (float) data.getDouble("spawn-locations." + this.bossId + ".pitch");
            World world = Bukkit.getWorld(worldName);
            if (world != null)
                this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
        }
    }

    public void setSpawnLocation(Location loc) {
        this.spawnLocation = loc;
        FileConfiguration data = this.plugin.getConfigManager().getData();
        data.set("spawn-locations." + this.bossId + ".world", loc.getWorld().getName());
        data.set("spawn-locations." + this.bossId + ".x", Double.valueOf(loc.getX()));
        data.set("spawn-locations." + this.bossId + ".y", Double.valueOf(loc.getY()));
        data.set("spawn-locations." + this.bossId + ".z", Double.valueOf(loc.getZ()));
        data.set("spawn-locations." + this.bossId + ".yaw", Float.valueOf(loc.getYaw()));
        data.set("spawn-locations." + this.bossId + ".pitch", Float.valueOf(loc.getPitch()));
        this.plugin.getConfigManager().saveData();
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public String getBossId() {
        return this.bossId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getMythicMobId() {
        return this.mythicMobId;
    }

    public UUID getCurrentBossUUID() {
        return this.currentBossUUID;
    }

    public Map<UUID, Double> getDamageMap() {
        return this.damageMap;
    }

    public void addDamage(UUID playerUUID, double damage) {
        this.damageMap.put(playerUUID, Double.valueOf(
                ((Double) this.damageMap.getOrDefault(playerUUID, Double.valueOf(0.0D))).doubleValue() + damage));
    }

    public void spawnBoss() {
        cleanupCurrentBoss();
        if (this.spawnLocation == null) {
            this.plugin.getLogger().warning("Cannot spawn boss " + this.bossId + ": Spawn location not set!");
            return;
        }
        if (!this.plugin.getMythicMobsHook().isMythicMob(this.mythicMobId)) {
            this.plugin.getLogger()
                    .warning("Cannot spawn boss " + this.bossId + ": MythicMob '" + this.mythicMobId + "' not found!");
            return;
        }
        UUID uuid = this.plugin.getMythicMobsHook().spawnMob(this.mythicMobId, this.spawnLocation);
        if (uuid != null) {
            this.currentBossUUID = uuid;
            this.damageMap.clear();
            String msg = this.plugin.getConfigManager().getConfig().getString("messages.boss-spawned");
            if (msg != null) {
                msg = msg.replace("%boss%", this.displayName).replace("%boss_id%", this.bossId);
                String prefix = this.plugin.getConfigManager().getConfig().getString("messages.prefix", "");
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
            }
            if (this.plugin.getConfigManager().getConfig().getBoolean("discord-webhook.enabled")) {
                String webhookUrl = this.plugin.getConfigManager().getConfig().getString("discord-webhook.url");
                String discordMsg = this.plugin.getConfigManager().getConfig()
                        .getString("discord-webhook.spawn-message")
                        .replace("%boss%", this.displayName)
                        .replace("%boss_id%", this.bossId);
                DiscordWebhook.sendMessage(webhookUrl, discordMsg, this.bossImageUrl);
            }
        }
    }

    public void cleanupCurrentBoss() {
        if (this.currentBossUUID != null) {
            this.plugin.getMythicMobsHook().removeMob(this.currentBossUUID);
            this.currentBossUUID = null;
        }
        this.damageMap.clear();
    }

    public void startTimer() {
        stopTimer();
        this.nextSpawnTime = System.currentTimeMillis() + this.spawnIntervalSeconds * 1000L;
        this.timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) this.plugin, () -> {
            long remainingMillis = this.nextSpawnTime - System.currentTimeMillis();
            if (remainingMillis <= 0L) {
                spawnBoss();
                this.nextSpawnTime = System.currentTimeMillis() + this.spawnIntervalSeconds * 1000L;
                return;
            }
            long remainingSeconds = remainingMillis / 1000L;
            if (this.announcementTimes.contains(Integer.valueOf((int) remainingSeconds))) {
                String msg = this.plugin.getConfigManager().getConfig().getString("messages.boss-warning")
                        .replace("%boss%", this.displayName)
                        .replace("%time%", String.valueOf(remainingSeconds / 60L));
                String prefix = this.plugin.getConfigManager().getConfig().getString("messages.prefix", "");
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
                if (this.plugin.getConfigManager().getConfig().getBoolean("discord-webhook.enabled")) {
                    String webhookUrl = this.plugin.getConfigManager().getConfig().getString("discord-webhook.url");
                    String discordMsg = this.plugin.getConfigManager().getConfig()
                            .getString("discord-webhook.warning-message").replace("%boss%", this.displayName)
                            .replace("%time%", String.valueOf(remainingSeconds / 60L));
                    DiscordWebhook.sendMessage(webhookUrl, discordMsg);
                }
            }
        }, 20L, 20L);
    }

    public void stopTimer() {
        if (this.timerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.timerTaskId);
            this.timerTaskId = -1;
        }
    }
}
