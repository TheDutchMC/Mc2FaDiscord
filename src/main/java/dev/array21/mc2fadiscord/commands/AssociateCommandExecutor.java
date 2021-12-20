package dev.array21.mc2fadiscord.commands;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import dev.array21.mc2fadiscord.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class AssociateCommandExecutor implements CommandExecutor, TabCompleter {

    private final Mc2FaDiscord plugin;

    public AssociateCommandExecutor(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command may only be executed by Players");
            return true;
        }

        Player p = (Player) sender;

        Optional<Long> associatedDiscord = this.plugin.getDatabaseHandler().getAssociatedDiscordUser(p.getUniqueId());
        if(associatedDiscord.isPresent()) {
            sender.sendMessage(String.format("%sThis account is already associated with a Discord account", ChatColor.GOLD));
            return true;
        }

        String code = Util.generateCode(6);

        this.plugin.getDatabaseHandler().addAssociationCode(p.getUniqueId(), code);
        sender.sendMessage(String.format("%sPlease verify that you own this account. Please open Discord and execute the %sassociate%s command against %s%s%s with the following code: %s%s",
                ChatColor.GOLD,
                ChatColor.RED,
                ChatColor.GOLD,
                ChatColor.RED,
                this.plugin.getConfigManifest().getBotName(),
                ChatColor.GOLD,
                ChatColor.RED,
                code));

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        // We do not want tab completion
        return null;
    }
}
