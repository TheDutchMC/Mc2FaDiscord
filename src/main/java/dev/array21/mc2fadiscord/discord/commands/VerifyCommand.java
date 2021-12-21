package dev.array21.mc2fadiscord.discord.commands;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class VerifyCommand extends ListenerAdapter {

    private final Mc2FaDiscord plugin;

    public VerifyCommand(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if(!event.getName().equals("verify")) {
            return;
        }

        OptionMapping codeMapping = event.getOption("code");
        if(codeMapping == null) {
            event.reply("You must supply a code").queue();
            return;
        }

        // Step 3: Verify the code
        String code = codeMapping.getAsString();
        Optional<UUID> verificationUuid = this.plugin.getDatabaseHandler().getVerificationUuid(code);
        if(verificationUuid.isEmpty()) {
            event.reply("Unknown code").queue();
            return;
        }

        Optional<Long> associatedDiscordId = this.plugin.getDatabaseHandler().getAssociatedDiscordUser(verificationUuid.get());
        if(associatedDiscordId.isEmpty()) {
            event.reply("No Discord user is associated with the Player being verififed").queue();
            return;
        }

        if(associatedDiscordId.get() != event.getMember().getIdLong()) {
            event.reply("You may not enter verification codes for this Minecraft account").queue();
            return;
        }

        Player player = Bukkit.getPlayer(verificationUuid.get());
        if(player == null) {
            event.reply("The Player is not online").queue();
            return;
        }

        Optional<Location> oldLocation = this.plugin.getReturnLocation(verificationUuid.get());
        if(oldLocation.isEmpty()) {
            event.reply("No return location is registered for this Player").queue();
            return;
        }

        // Step 4: Teleport the player back
        player.sendMessage(String.format("%sVerification complete", ChatColor.GOLD));
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(oldLocation.get());
            }
        }.runTask(this.plugin);

        this.plugin.removeReturnLocation(verificationUuid.get());

        // Step 5: Mark the player as verified
        long expiry = Instant.now().getEpochSecond() + this.plugin.getConfigManifest().getTrustedDuration();
        this.plugin.getDatabaseHandler().setPlayerVerified(verificationUuid.get(), expiry);

        // Cleanup
        this.plugin.getDatabaseHandler().removeVerificationCode(verificationUuid.get());

        // Run post process commands
        new BukkitRunnable() {
            @Override
            public void run() {
                for(String cmd : VerifyCommand.this.plugin.getConfigManifest().getPostVerifyConsoleCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%PLAYER%", player.getName()));
                }

                for(String cmd : VerifyCommand.this.plugin.getConfigManifest().getPostVerifyPlayerCommands()) {
                    player.performCommand(cmd);
                }
            }
        }.runTask(this.plugin);

        event.reply("Verified!").queue();
    }
}
