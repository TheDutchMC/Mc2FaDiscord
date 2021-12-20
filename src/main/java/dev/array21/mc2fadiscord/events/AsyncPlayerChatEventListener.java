package dev.array21.mc2fadiscord.events;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatEventListener implements Listener {

    private final Mc2FaDiscord plugin;

    public AsyncPlayerChatEventListener(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if(event.getPlayer().getWorld().equals(this.plugin.getVerificationWorld())) {
            event.setCancelled(true);
        }
    }
}
