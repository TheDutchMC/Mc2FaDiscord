package dev.array21.mc2fadiscord.database;

import dev.array21.jdbd.DatabaseDriver;
import dev.array21.jdbd.datatypes.PreparedStatement;
import dev.array21.jdbd.datatypes.SqlRow;
import dev.array21.jdbd.drivers.MysqlDriverFactory;
import dev.array21.jdbd.exceptions.SqlException;
import dev.array21.mc2fadiscord.Mc2FaDiscord;
import dev.array21.mc2fadiscord.config.ConfigManifest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.PropertyPermission;
import java.util.UUID;

public class DatabaseHandler {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseHandler.class);
    private final Mc2FaDiscord plugin;
    private DatabaseDriver driver;

    public DatabaseHandler(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            this.driver = loadDriver();
        } catch (IOException e) {
            LOGGER.error("Failed to load MySQL driver", e);
            Mc2FaDiscord.disablePlugin();
            return;
        }

        try {
            applyMigrations();
        } catch (SqlException e) {
            LOGGER.error("Failed to apply migrations", e);
            Mc2FaDiscord.disablePlugin();
            return;
        }
    }

    private void applyMigrations() throws SqlException {
        MigrationHandler migrationHandler = new MigrationHandler(this.driver);
        migrationHandler.apply();
    }

    private DatabaseDriver loadDriver() throws IOException {
        LOGGER.debug("Loading MySQL driver");
        ConfigManifest.MysqlConfig config = this.plugin.getConfigManifest().getMysql();
        return new MysqlDriverFactory()
                .setHost(config.getHost())
                .setDatabase(config.getDatabase())
                .setUsername(config.getUsername())
                .setPassword(config.getPassword())
                .build();
    }

    public void addVerificationCode(final String code, final UUID playerUuid) {
        PreparedStatement pr = new PreparedStatement("INSERT INTO VerificationCodes (PlayerUuid, Code) VALUES ('?', '?')");
        pr.bind(0, playerUuid.toString());
        pr.bind(1, code);
        executeStatement(pr);
    }

    public void removeVerificationCode(final UUID playerUuid) {
        PreparedStatement pr = new PreparedStatement("DELETE FROM VerificationCodes WHERE PlayerUuid = '?'");
        pr.bind(0, playerUuid.toString());
        executeStatement(pr);
    }

    public Optional<UUID> getVerificationUuid(final String code) {
        PreparedStatement pr = new PreparedStatement("SELECT PlayerUuid from VerificationCodes WHERE Code = '?'");
        pr.bind(0, code);
        SqlRow[] result = executeStatement(pr);
        if(result.length == 0) {
            return Optional.empty();
        }
        SqlRow zeroth = result[0];
        String uuidString = zeroth.getString("PlayerUuid");
        return Optional.of(UUID.fromString(uuidString));
    }

    public Optional<UUID> getAssociatedMinecraftuser(final long discordId) {
        PreparedStatement pr = new PreparedStatement("SELECT PlayerUuid FROM PlayerDiscord WHERE DiscordId = '?'");
        pr.bind(0, discordId);
        SqlRow[] result = executeStatement(pr);
        if(result.length == 0) {
            return Optional.empty();
        }
        SqlRow zeroth = result[0];
        return Optional.of(UUID.fromString(zeroth.getString("PlayerUuid")));
    }

    public Optional<Long> getAssociatedDiscordUser(final UUID playerUuid) {
        PreparedStatement pr = new PreparedStatement("SELECT DiscordId FROM PlayerDiscord WHERE PlayerUuid = '?'");
        pr.bind(0, playerUuid.toString());
        SqlRow[] result = executeStatement(pr);
        if(result.length == 0) {
            return Optional.empty();
        }
        SqlRow zeroth = result[0];
        return Optional.of(zeroth.getLong("DiscordId"));
    }

    public void setAssociatedDiscordUser(final UUID playerUuid, final long discordId) {
        PreparedStatement pr = new PreparedStatement("INSERT INTO PlayerDiscord (PlayerUuid, DiscordId) VALUES ('?', '?')");
        pr.bind(0, playerUuid.toString());
        pr.bind(1, discordId);
        executeStatement(pr);
    }

    public void setPlayerVerified(final UUID playerUuid, final long expiry) {
        PreparedStatement pr = new PreparedStatement("INSERT INTO VerifiedPlayers (PlayerUuid, Expiry) VALUES ('?', '?')");
        pr.bind(0, playerUuid.toString());
        pr.bind(1, expiry);
        executeStatement(pr);
    }

    public boolean isPlayerVerified(final UUID playerUuid) {
        PreparedStatement pr = new PreparedStatement("SELECT Expiry FROM VerifiedPlayers WHERE PlayerUuid = '?'");
        pr.bind(0, playerUuid.toString());
        SqlRow[] result = executeStatement(pr);
        if(result.length == 0) {
            return false;
        }
        SqlRow zeroth = result[0];
        long expiry = zeroth.getLong("Expiry");
        long epochNow = Instant.now().getEpochSecond();

        if(expiry > epochNow) {
            return true;
        } else {
            PreparedStatement delPr = new PreparedStatement("DELETE FROM VerifiedPlayers WHERE PlayerUuid = '?'");
            delPr.bind(0, playerUuid.toString());
            executeStatement(delPr);
            return false;
        }
    }

    public void addAssociationCode(final UUID playerUuid, final String code) {
        PreparedStatement pr = new PreparedStatement("INSERT INTO AssociationCodes (PlayerUuid, Code) VALUES ('?', '?')");
        pr.bind(0, playerUuid.toString());
        pr.bind(1, code);
        executeStatement(pr);
    }

    public Optional<UUID> retrieveAssociationCode(final String code) {
        PreparedStatement pr = new PreparedStatement("SELECT PlayerUuid FROM AssociationCodes WHERE Code = '?'");
        pr.bind(0, code);
        SqlRow[] result = executeStatement(pr);
        if(result.length == 0) {
            return Optional.empty();
        }
        SqlRow zeroth = result[0];
        return Optional.of(UUID.fromString(zeroth.getString("PlayerUuid")));
    }

    private SqlRow[] executeStatement(final PreparedStatement pr) {
        try {
            return this.driver.query(pr);
        } catch (SqlException e) {
            LOGGER.error("Failed to execute statement", e);
            return new SqlRow[0];
        }
    }
}
