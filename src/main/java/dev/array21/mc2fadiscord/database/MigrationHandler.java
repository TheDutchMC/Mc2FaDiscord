package dev.array21.mc2fadiscord.database;

import com.sun.jdi.InvocationException;
import dev.array21.jdbd.DatabaseDriver;
import dev.array21.jdbd.datatypes.PreparedStatement;
import dev.array21.jdbd.datatypes.SqlRow;
import dev.array21.jdbd.exceptions.SqlException;
import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MigrationHandler {

    private static final Logger LOGGER = LogManager.getLogger(MigrationHandler.class);
    private DatabaseDriver driver;

    public MigrationHandler(final DatabaseDriver driver) {
        this.driver = driver;
    }

    public void apply() throws SqlException {
        LOGGER.debug("Applying migrations");
        createMigTable();
        int[] appliedMigs = getAppliedMigs();
        Method[] toApply = getUnappliedMigs(appliedMigs.length - 1);
        LOGGER.debug(String.format("Applying %d migrations", toApply.length));
        try {
            for(int i = 0; i < toApply.length; i++) {
                LOGGER.debug(String.format("Applying migration %d", toApply.length - 1 + i));
                toApply[i].invoke(this);
                markApplied(toApply.length - 1 + i);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Failed to apply migrations", e);
            Mc2FaDiscord.disablePlugin();
            return;
        }
    }

    private void markApplied(final int id) throws SqlException {
        LOGGER.debug(String.format("Marking migration %d as applied", id));
        PreparedStatement pr = new PreparedStatement("INSERT INTO __migrations (id) VALUES ('?')");
        pr.bind(0, id);
        this.driver.execute(pr);
    }

    private Method[] getUnappliedMigs(final int lastApplied) {
        try {
            return Arrays.stream(MigrationHandler.class.getDeclaredMethods())
                    .filter(f -> f.getName().startsWith("mig_"))
                    .filter(f -> {
                        int mig = Integer.parseInt(f.getName().substring(4));
                        return mig > lastApplied;
                    })
                    .toList()
                    .toArray(new Method[0]);
        } catch(Exception e) {
            LOGGER.error("Failed to retrieve unapplied migrations", e);
            Mc2FaDiscord.disablePlugin();
            return new Method[0];
        }
    }

    private int[] getAppliedMigs() throws SqlException {
        LOGGER.debug("Fetching already applied migrations");
        PreparedStatement pr = new PreparedStatement("SELECT id FROM __migrations");
        SqlRow[] rows = this.driver.query(pr);
        Integer[] objectResult = Arrays.stream(rows)
                .map(f -> f.getLong("id"))
                .map(f -> (int)(long) f)
                .toList()
                .toArray(new Integer[0]);

        return objecToPrimitive(objectResult);
    }

    private int[] objecToPrimitive(final Integer[] input) {
        int[] res = new int[input.length];
        for(int i = 0; i < res.length; i++) {
            res[i] = input[i];
        }

        return res;
    }

    private void createMigTable() throws SqlException {
        LOGGER.debug("Created __migrations table if it did not yet exist");
        PreparedStatement pr = new PreparedStatement("CREATE TABLE IF NOT EXISTS __migrations (id INT PRIMARY KEY NOT NULL)");
        this.driver.execute(pr);
    }

    private void mig_0() throws SqlException {
        PreparedStatement pr0 = new PreparedStatement("CREATE TABLE VerificationCodes (PlayerUuid VARCHAR(36) NOT NULL, Code VARCHAR(32) PRIMARY KEY NOT NULL)");
        this.driver.execute(pr0);
        PreparedStatement pr1 = new PreparedStatement("CREATE TABLE PlayerDiscord (PlayerUuid VARCHAR(36) NOT NULL, DiscordId BIGINT PRIMARY KEY NOT NULL)");
        this.driver.execute(pr1);
        PreparedStatement pr2 = new PreparedStatement("CREATE TABLE VerifiedPlayers (PlayerUuid VARCHAR(36) PRIMARY KEY NOT NULL, Expiry BIGINT NOT NULL)");
        this.driver.execute(pr2);
        PreparedStatement pr3 = new PreparedStatement("CREATE TABLE AssociationCodes (PlayerUuid VARCHAR(36) NOT NULL, Code VARCHAR(32) PRIMARY KEY NOT NULL)");
        this.driver.execute(pr3);
    }
}
