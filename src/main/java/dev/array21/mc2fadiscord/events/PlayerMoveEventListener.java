package dev.array21.mc2fadiscord.events;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveEventListener implements Listener {

    private final Mc2FaDiscord plugin;

    public PlayerMoveEventListener(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMoveEvent(final PlayerMoveEvent event) {
        if(event.getPlayer().getWorld().equals(this.plugin.getVerificationWorld())) {
            event.setCancelled(true);
        }
    }
}
