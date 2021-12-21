package dev.array21.mc2fadiscord.events;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import dev.array21.mc2fadiscord.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Arrays;
import java.util.List;

public class PlayerJoinEventListener implements Listener {

    private static final Logger LOGGER = LogManager.getLogger(PlayerJoinEventListener.class);
    private final Mc2FaDiscord plugin;

    public PlayerJoinEventListener(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(final PlayerJoinEvent event) {
        Player p = event.getPlayer();

        if(this.plugin.getConfigManifest().isApplyToOp() && p.isOp()) {
            LOGGER.debug("Player is OP and isApplyToOp is true, running verification");
            doVerification(p);
            return;
        }

        List<String> appliesTo = Arrays.asList(this.plugin.getConfigManifest().getPermissions());
        boolean hasRequiredPermission = p.getEffectivePermissions()
                .stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(appliesTo::contains)
                .toList()
                .size() > 0;

        if(hasRequiredPermission) {
            LOGGER.debug("Player has one of the permissions that require verification, running verification");
            doVerification(p);
        }
    }

    private void doVerification(final Player p) {
        // First check if the Player is already verified
        if(this.plugin.getDatabaseHandler().isPlayerVerified(p.getUniqueId())) {
            LOGGER.debug("Player is already verified");
            return;
        }

        // Verification involves several steps
        // 1. TP the player to the 2FA World
        // 2. Message the Player a code in Minecraft
        // 3. The player must DM the bot with that code
        // 4. If the code is correct, the Player gets teleported back to where they joined
        // 5. Mark the player as trusted for the configured duration

        LOGGER.debug("Storing old Player Location");
        Location originalLocation = p.getLocation();
        this.plugin.addReturnLocation(p.getUniqueId(), originalLocation);

        // 1: Teleport the Player
        LOGGER.debug("Teleporting player to verification World");
        Location newLocation = new Location(this.plugin.getVerificationWorld(), 0, 100, 0, 0f, 0f);
        p.teleport(newLocation);

        // 2: Send the Player the code
        LOGGER.debug("Generating and storing Verification Code");
        String code = Util.generateCode(6);
        this.plugin.getDatabaseHandler().addVerificationCode(code, p.getUniqueId());
        p.sendMessage(String.format("%sPlease verify that you own this account. Please open Discord and execute the %sverify%s command against %s%s%s with the following code: %s%s",
                ChatColor.GOLD,
                ChatColor.RED,
                ChatColor.GOLD,
                ChatColor.RED,
                this.plugin.getConfigManifest().getBotName(),
                ChatColor.GOLD,
                ChatColor.RED,
                code));

        // This is where we stop
        // The rest is handled elsewhere
    }
}
