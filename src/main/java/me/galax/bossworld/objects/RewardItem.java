package me.galax.bossworld.objects;

import org.bukkit.inventory.ItemStack;

public class RewardItem {
  private ItemStack item;
  
  private double chance;
  
  public RewardItem(ItemStack item, double chance) {
    this.item = item;
    this.chance = chance;
  }
  
  public ItemStack getItem() {
    return this.item;
  }
  
  public void setItem(ItemStack item) {
    this.item = item;
  }
  
  public double getChance() {
    return this.chance;
  }
  
  public void setChance(double chance) {
    this.chance = Math.max(0.0D, Math.min(100.0D, chance));
  }
}


/* Location:              C:\Users\galax\Downloads\all\BossWorld-1.0-SNAPSHOT.jar!\me\galax\bossworld\objects\RewardItem.class
 * Java compiler version: 21 (65.0)
 * JD-Core Version:       1.1.3
 */