package dev.array21.mc2fadiscord.config;

import dev.array21.classvalidator.annotations.Required;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConfigManifest {
    @Required
    private String discordBotToken;
    @Required
    private String botName;
    @Required
    private MysqlConfig mysql;
    private String[] permissions;
    @Required
    private boolean applyToOp;
    private UUID worldUuid;

    @Required
    private Long guildId;

    @Required
    private Long trustedDuration;

    public String getBotName() {
        return this.botName;
    }

    public long getTrustedDuration() {
        return this.trustedDuration;
    }

    public String getDiscordBotToken() {
        return this.discordBotToken;
    }

    public MysqlConfig getMysql() {
        return this.mysql;
    }

    @Nullable
    public UUID getWorldUuid() {
        return this.worldUuid;
    }

    public String[] getPermissions() {
        return this.permissions != null ? this.permissions : new String[0];
    }

    public boolean isApplyToOp() {
        return this.applyToOp;
    }

    public void setWorldUuid(UUID worldUuid) {
        this.worldUuid = worldUuid;
    }

    public long getGuildId() {
        return this.guildId;
    }

    public class MysqlConfig {
        @Required
        private String host, username, password, database;

        public String getHost() {
            return this.host;
        }

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public String getDatabase() {
            return this.database;
        }
    }
}
