package dev.array21.mc2fadiscord.events;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private final Mc2FaDiscord plugin;

    public PlayerQuitEventListener(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuitEvent(final PlayerQuitEvent event) {
        Player p = event.getPlayer();
        this.plugin.removeReturnLocation(p.getUniqueId());
    }

}
