# BossWorld Plugin

A powerful Minecraft plugin for management of World Bosses with multi-boss support, independent spawn intervals, rewards, and anti-cheat system.

## Features

- **Multi-Boss Support**: Run multiple bosses simultaneously with independent configurations.
- **Independent Spawning**: Each boss has its own spawn location, interval, and announcement times.
- **Custom Rewards**: GUI-based reward management for each boss individually.
- **Anti-Cheat System**: Prevents flying, god mode, and unauthorized commands in boss areas.
- **Discord Integration**: Webhook notifications for boss spawning and death with boss-specific images.
- **Easy Configuration**: Simple but flexible YAML configuration.

## Requirements

- **Server Version**: PaperMC 1.21 or higher
- **Java Version**: 21
- **Dependencies**: 
  - [MythicMobs](https://mythicmobs.net/) (Required)
  - [CMI](https://www.spigotmc.org/resources/cmi-content-management-interface.30510/) (Optional, for enhanced anti-cheat integration)

## Commands

### Player Commands
- `/bossworld` - Teleport to the default boss warp.
- `/bossworld <bossId>` - Teleport to a specific boss warp.

### Admin Commands
- `/bossworld list` - Show all active boss IDs.
- `/bossworld setspawn <bossId>` - Set spawn location for a boss.
- `/bossworld spawn <bossId>` - Force spawn a specific boss.
- `/bossworld clear [bossId]` - Clear active bosses (removes all if bossId is omitted).
- `/bossworld reward edit <bossId>` - Open reward editor GUI for a specific boss.
- `/bossworld reload` - Reload plugin configuration.

## Installation

1. Download the latest `BossWorld.jar`.
2. Place it in your server's `plugins/` folder.
3. Restart the server.
4. Configure `plugins/BossWorld/config.yml`.
5. Run `/bossworld reload` in-game.
6. Set spawn locations using `/bossworld setspawn <bossId>`.

## Developer

Developed by **GalaxyCodeP**.

---
*For detailed system updates and multi-boss setup instructions, see [README_MULTI_BOSS.md](README_MULTI_BOSS.md).*
