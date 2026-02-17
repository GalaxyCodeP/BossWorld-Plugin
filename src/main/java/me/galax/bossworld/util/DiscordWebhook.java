package me.galax.bossworld.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.bukkit.Bukkit;

public class DiscordWebhook {
  public static void sendMessage(String webhookUrl, String message, String imageUrl) {
    if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL_HERE"))
      return; 
    Bukkit.getAsyncScheduler().runNow(Bukkit.getPluginManager().getPlugin("BossWorld"), task -> {
          try {
            URL url = URI.create(webhookUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            JsonObject json = new JsonObject();
            json.addProperty("content", message);
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("https://example.com/boss-image.png")) {
              JsonArray embeds = new JsonArray();
              JsonObject embed = new JsonObject();
              JsonObject image = new JsonObject();
              image.addProperty("url", imageUrl);
              embed.add("image", (JsonElement)image);
              embed.addProperty("color", Integer.valueOf(16753920));
              embeds.add((JsonElement)embed);
              json.add("embeds", (JsonElement)embeds);
            } 
            String jsonString = json.toString();
            OutputStream os = connection.getOutputStream();
            try {
              byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
              os.write(input, 0, input.length);
              if (os != null)
                os.close(); 
            } catch (Throwable throwable) {
              if (os != null)
                try {
                  os.close();
                } catch (Throwable throwable1) {
                  throwable.addSuppressed(throwable1);
                }  
              throw throwable;
            } 
            int responseCode = connection.getResponseCode();
            if (responseCode != 204 && responseCode != 200)
              Bukkit.getLogger().warning("[BossWorld] Failed to send Discord webhook. Response code: " + responseCode); 
          } catch (Exception e) {
            Bukkit.getLogger().severe("[BossWorld] Error sending Discord webhook: " + e.getMessage());
          } 
        });
  }
  
  public static void sendMessage(String webhookUrl, String message) {
    sendMessage(webhookUrl, message, null);
  }
}


/* Location:              C:\Users\galax\Downloads\all\BossWorld-1.0-SNAPSHOT.jar!\me\galax\bossworl\\util\DiscordWebhook.class
 * Java compiler version: 21 (65.0)
 * JD-Core Version:       1.1.3
 */