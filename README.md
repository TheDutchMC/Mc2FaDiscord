# Mc2FaDiscord
Minecraft Two Factor authorization via Discord for OPs or Players with certain permissions

## Installation
### Requirments
- MySQL server
- Minecraft 1.16+
- Java 16+

### Instructions
1. Download the plugin JAR from [releases](https://github.com/TheDutchMC/Mc2FaDiscord/releases)
2. Place the JAR in your plugins folder
3. Start your server
4. Stop your server
5. Configure the plugin
    1. Create a Discord bot [here](https://discord.com/developers)
    2. Copy the Bot token into the configuration file
    3. Invite the bot to your server:
        1. Go to the OAuth2 tab, then General
        2. Set the authorization mode to `In-app Authorization`
        3. Go to the OAuth2 tab, then URL Generator
        4. Select the scopes `bot` and `applications.commands`
        5. Copy the URL at the bottom of the page and open it
        6. Complete the process
    4. Do NOT set up roles just yet, set `applyToOp` to false
    5. The `guildId` can be obtained by right clicking the server icon and selecting Copy ID
    6. Configure the details for your MySQL Server
6. Start the server
7. Associate every Minecraft account with a Discord account with `/associate`
8. Set up your roles and set `applyToOp` to true, if you wish
9. Restart your server once more, and you are done

## Usage
When a player joins who is either OP, and if `applyToOp` is enabled, or has one of the specified permissions, they will be teleported to a void world. In this world they can not execute any commands, send any chat messages or move. 
They will get a chat message in-game instructing them what to do. After the player has completed the verification process they will be teleported back.

>Note: If the player leaves while in this void world, they must be manually teleported back by an operator. This is intentional  

>Note: If the player is in the void world when the server stops, they must be manually teleported back by an operator.  


## Default configuration
```jsonc
{
  "discordBotToken": null,      // The bot token obtained at discord.com/developers
  "botName": null,              // The name of the bot, this is only used in messages to players
  "guildId": null,              // The ID of the guild the bot will operate in
  "mysql": {
    "host": null,
    "database": null,
    "username": null,
    "password": null
  },
  "permissions": [
    "foo.bar"
  ],
  "applyToOp": true,
  "trustedDuration": 3600       // Time in seconds
}
```
## License
Mc2FaDiscord is licensed under the MIT license

This project depends on:
- https://github.com/DV8FromTheWorld/JDA, Apache-2.0 license
- https://github.com/google/gson, Apache-2.0 license
- https://github.com/TheDutchMC/ClassValidator, Apache-2.0 license
- https://github.com/TheDutchMC/JDBD, MIT license
- https://logging.apache.org/log4j/2.x/, Apache-2.0 license