package dev.array21.mc2fadiscord.discord;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import dev.array21.mc2fadiscord.discord.commands.AssociateCommand;
import dev.array21.mc2fadiscord.discord.commands.VerifyCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;

public class JdaHandler {

    private static final Logger LOGGER = LogManager.getLogger(JdaHandler.class);
    private final Mc2FaDiscord plugin;

    public JdaHandler(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    private JDA jda;

    public JDA load(final String token) throws LoginException {
        LOGGER.debug("Creating JDA object");
        JDA jda = JDABuilder.createDefault(token, GatewayIntent.DIRECT_MESSAGES)
                .setActivity(Activity.watching("Big brother is always watching"))
                .disableCache(CacheFlag.EMOTE, CacheFlag.VOICE_STATE)
                .setAutoReconnect(true)
                .build();

        LOGGER.debug("Waiting for JDA to be ready");
        try {
            jda.awaitReady();
        } catch (InterruptedException ignored) {

        }

        Guild guild = jda.getGuildById(this.plugin.getConfigManifest().getGuildId());
        if(guild == null) {
            LOGGER.error("Guild does not exist--Is the provided guildId correct?");
            Mc2FaDiscord.disablePlugin();
            return null;
        }

        LOGGER.debug("Upserting Commands");
        guild.upsertCommand(new CommandData("associate", "Associate a Minecraft user to this Discord account")
                .addOption(OptionType.INTEGER, "code", "The association code"))
                .queue();
        guild.upsertCommand(new CommandData("verify", "Verify a Minecraft login")
                .addOption(OptionType.INTEGER, "code", "The verification code"))
                .queue();

        LOGGER.debug("Registering Discord Event Listeners");
        jda.addEventListener(new AssociateCommand(this.plugin));
        jda.addEventListener(new VerifyCommand(this.plugin));

        LOGGER.debug("JDA loaded");
        this.jda = jda;
        return jda;
    }

    public void shutdown() {
        try {
            this.jda.shutdownNow();
        } catch (Exception ignored) {

        }
    }
}
