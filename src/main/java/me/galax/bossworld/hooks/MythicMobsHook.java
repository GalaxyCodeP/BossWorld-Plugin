package me.galax.bossworld.hooks;

import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import me.galax.bossworld.BossWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class MythicMobsHook {
  private final BossWorld plugin;
  
  public MythicMobsHook(BossWorld plugin) {
    this.plugin = plugin;
  }
  
  public UUID spawnMob(String mobId, Location location) {
    try {
      Optional<MythicMob> mythicMob = MythicProvider.get().getMobManager().getMythicMob(mobId);
      if (mythicMob.isPresent()) {
        ActiveMob activeMob = ((MythicMob)mythicMob.get()).spawn(BukkitAdapter.adapt(location), 1.0D);
        if (activeMob != null && activeMob.getEntity() != null)
          return activeMob.getEntity().getUniqueId(); 
      } 
    } catch (Exception e) {
      this.plugin.getLogger().warning("Failed to spawn MythicMob " + mobId + ": " + e.getMessage());
    } 
    return null;
  }
  
  public boolean isMythicMob(String mobId) {
    try {
      return MythicProvider.get().getMobManager().getMythicMob(mobId).isPresent();
    } catch (Exception e) {
      return false;
    } 
  }
  
  public Collection<String> getMythicMobNames() {
    try {
      return MythicProvider.get().getMobManager().getMobNames();
    } catch (Exception e) {
      return Collections.emptyList();
    } 
  }
  
  public void removeMob(UUID uuid) {
    if (uuid == null)
      return; 
    Entity entity = Bukkit.getEntity(uuid);
    if (entity != null)
      entity.remove(); 
  }
}


/* Location:              C:\Users\galax\Downloads\all\BossWorld-1.0-SNAPSHOT.jar!\me\galax\bossworld\hooks\MythicMobsHook.class
 * Java compiler version: 21 (65.0)
 * JD-Core Version:       1.1.3
 */