package dev.array21.mc2fadiscord.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.array21.classvalidator.ClassValidator;
import dev.array21.classvalidator.Pair;
import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

public class ConfigHandler {

	private final Mc2FaDiscord plugin;
	private static final Logger LOGGER = LogManager.getLogger(ConfigHandler.class);
	private ConfigManifest configManifest;

	public ConfigHandler(final Mc2FaDiscord plugin) {
		this.plugin = plugin;
	}

	public void setWorldUuid(final UUID worldUuid) {
		this.configManifest.setWorldUuid(worldUuid);

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(this.configManifest, ConfigManifest.class);

		File configFile = new File(this.plugin.getDataFolder(), "config.json");
		try {
			Files.writeString(configFile.toPath(), json);
		} catch (IOException e) {
			LOGGER.error("Failed to write to config file", e);
			Mc2FaDiscord.disablePlugin();
		}
	}

	public Optional<ConfigManifest> read() throws IOException {
		LOGGER.debug("Loading configuration file");

		final File f = new File(this.plugin.getDataFolder(), "config.json");
		if(!f.exists()) {
			LOGGER.debug("Configuration does not yet exist. Saving from JAR");
			saveDefault(f);

			LOGGER.info("A default configuration has been saved. Please configure this plugin and then restart");
			Mc2FaDiscord.disablePlugin();
			return Optional.empty();
		}

		final Gson gson = new Gson();
		final Reader reader = Files.newBufferedReader(f.toPath());

		ConfigManifest manifest = gson.fromJson(reader, ConfigManifest.class);

		LOGGER.debug("Configuration loaded");

		LOGGER.debug("Validating configuration");
		if(!checkValid(manifest)) {
			return Optional.empty();
		}
		LOGGER.debug("Configuration is valid");

		this.configManifest = manifest;
		return Optional.of(manifest);
	}

	private void saveDefault(final File f) {
		f.getParentFile().mkdirs();
		this.plugin.saveResource("config.json", false);
	}

	private boolean checkValid(final ConfigManifest manifest) {
		Pair<Boolean, String> validationResult = ClassValidator.validateType(manifest);
		if(validationResult.getA() == null) {
			LOGGER.error(String.format("Failed to validate configuration: %s", validationResult.getB()));
			Mc2FaDiscord.disablePlugin();
			return false;
		}

		if(!validationResult.getA()) {
			LOGGER.error(String.format("Invalid configuration: %s", validationResult.getB()));
			Mc2FaDiscord.disablePlugin();
			return false;
		}

		return true;
	}
}
