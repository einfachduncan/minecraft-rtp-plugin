# Minecraft RTP Plugin

## Overview

The Minecraft RTP (Random Teleportation) Plugin allows players to teleport randomly within a defined radius. This plugin is designed for use with the Paper and Spigot API, ensuring high performance and compatibility with various Minecraft server setups.

## Features
- **Configurable Radius**: Server administrators can configure the radius within which players will be teleported, allowing for customizable gameplay experiences.
- **Easy to Use**: The plugin can be set up with minimal configuration, making it accessible for all server owners.
- **Optimized Performance**: Built with performance in mind, this plugin minimizes lag during teleportation.

## Building from Source

No need to install Maven! Use the included Maven Wrapper:

**Windows (PowerShell):**
```powershell
.\mvnw.cmd package
```

**Linux / macOS:**
```bash
./mvnw package
```

The built JAR will be in `target/minecraft-rtp-plugin-1.0.0.jar`.

> **Prerequisite:** Java 17 or newer must be installed. Download from [https://adoptium.net](https://adoptium.net).

## Installation
1. Download the plugin JAR file (or build it yourself, see above).
2. Place the JAR file in the `plugins` directory of your Minecraft server.
3. Restart the server to enable the plugin.
4. Adjust the configuration settings as needed.

## Configuration
The plugin configuration can be found in the `config.yml` file. Here you can set the teleportation radius and other customized settings according to your server needs.

## Support
For support or feature requests, please create an issue on the GitHub repository.