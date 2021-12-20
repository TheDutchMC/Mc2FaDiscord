package dev.array21.mc2fadiscord.events;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessEventListener implements Listener {

    private final Mc2FaDiscord plugin;

    public PlayerCommandPreprocessEventListener(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocessEventListener(final PlayerCommandPreprocessEvent event) {
        if(event.getPlayer().getWorld().equals(this.plugin.getVerificationWorld())) {
            event.setCancelled(true);
        }
    }
}
