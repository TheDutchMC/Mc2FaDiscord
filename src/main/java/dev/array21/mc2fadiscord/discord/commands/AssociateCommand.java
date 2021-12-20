package dev.array21.mc2fadiscord.discord.commands;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class AssociateCommand extends ListenerAdapter {

    private Mc2FaDiscord plugin;

    public AssociateCommand(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommand(final SlashCommandEvent event) {
        if(!event.getName().equals("associate")) {
            return;
        }

        OptionMapping codeMapping = event.getOption("code");
        if(codeMapping == null) {
            event.reply("You must supply a code").queue();
            return;
        }

        String code = codeMapping.getAsString();
        Optional<UUID> associatedPlayer = this.plugin.getDatabaseHandler().retrieveAssociationCode(code);
        if(associatedPlayer.isEmpty()) {
            event.reply("Unknown code").queue();
            return;
        }

        Player player = Bukkit.getPlayer(associatedPlayer.get());
        if(player == null) {
            event.reply("The Minecraft player must be online").queue();
            return;
        }

        this.plugin.getDatabaseHandler().setAssociatedDiscordUser(associatedPlayer.get(), event.getMember().getIdLong());
        event.reply(String.format("This account is now associated with the Minecraft player %s", player.getName())).queue();
        player.sendMessage(String.format("%sThis account is now associated with the Discord user %s%s",
                ChatColor.GOLD,
                ChatColor.RED,
                event.getMember().getUser().getName()));
    }
}
