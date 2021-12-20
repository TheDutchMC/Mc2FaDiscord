package dev.array21.mc2fadiscord;

import dev.array21.mc2fadiscord.commands.AssociateCommandExecutor;
import dev.array21.mc2fadiscord.config.ConfigHandler;
import dev.array21.mc2fadiscord.config.ConfigManifest;
import dev.array21.mc2fadiscord.database.DatabaseHandler;
import dev.array21.mc2fadiscord.discord.JdaHandler;
import dev.array21.mc2fadiscord.events.AsyncPlayerChatEventListener;
import dev.array21.mc2fadiscord.events.PlayerCommandPreprocessEventListener;
import dev.array21.mc2fadiscord.events.PlayerJoinEventListener;
import dev.array21.mc2fadiscord.events.PlayerMoveEventListener;
import dev.array21.mc2fadiscord.events.PlayerQuitEventListener;
import dev.array21.mc2fadiscord.world.WorldHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class Mc2FaDiscord extends JavaPlugin {

	public static final Logger LOGGER = LogManager.getLogger(Mc2FaDiscord.class);
	private static Mc2FaDiscord instance;

	private ConfigManifest configManifest;
	private DatabaseHandler databaseHandler;
	private World verificationWorld;
	private JdaHandler jdaHandler;
	private ConfigHandler configHandler;

	private HashMap<UUID, Location> originalLocations = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		LOGGER.info("Loading");

		this.configHandler = new ConfigHandler(this);
		Optional<ConfigManifest> maybeConfigManifest;
		try {
			maybeConfigManifest = this.configHandler.read();
		} catch (IOException e) {
			LOGGER.error("Failed to load configuration", e);
			disablePlugin();
			return;
		}

		if(maybeConfigManifest.isEmpty()) {
			return;
		}
		this.configManifest = maybeConfigManifest.get();

		WorldHandler worldHandler = new WorldHandler(this);
		worldHandler.createWorldIfNotExists();
		this.verificationWorld = worldHandler.getWorld();

		this.databaseHandler = new DatabaseHandler(this);
		this.databaseHandler.initialize();

		LOGGER.debug("Registering Minecraft commands");
		PluginCommand associateCommand = Bukkit.getPluginCommand("associate");
		AssociateCommandExecutor associateCommandExecutor = new AssociateCommandExecutor(this);
		associateCommand.setExecutor(associateCommandExecutor);
		associateCommand.setTabCompleter(associateCommandExecutor);

		LOGGER.debug("Registering Minecraft event listeners");
		Bukkit.getPluginManager().registerEvents(new PlayerJoinEventListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerQuitEventListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerMoveEventListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessEventListener(this), this);
		Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatEventListener(this), this);

		this.jdaHandler = new JdaHandler(this);
		try {
			if(this.jdaHandler.load(this.configManifest.getDiscordBotToken()) == null) {
				return;
			}
		} catch(LoginException e) {
			LOGGER.error("Failed to log in to Discord", e);
			disablePlugin();
			return;
		}

		LOGGER.info("Loaded");
	}

	@Override
	public void onDisable() {
		if(this.jdaHandler != null) {
			this.jdaHandler.shutdown();
		}
	}

	public ConfigHandler getConfigHandler() {
		return this.configHandler;
	}

	public World getVerificationWorld() {
		return this.verificationWorld;
	}

	public DatabaseHandler getDatabaseHandler() {
		return this.databaseHandler;
	}

	public ConfigManifest getConfigManifest() {
		return this.configManifest;
	}

	public void addReturnLocation(final UUID playerUuid, final Location location) {
		this.originalLocations.put(playerUuid, location);
	}

	public Optional<Location> getReturnLocation(final UUID playerUuid) {
		return Optional.ofNullable(this.originalLocations.get(playerUuid));
	}

	public void removeReturnLocation(final UUID playerUuid) {
		this.originalLocations.remove(playerUuid);
	}

	public static void disablePlugin() {
		LOGGER.warn("Disabling plugin");
		Bukkit.getPluginManager().disablePlugin(instance);
	}
}
