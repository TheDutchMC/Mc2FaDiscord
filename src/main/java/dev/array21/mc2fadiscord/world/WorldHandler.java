package dev.array21.mc2fadiscord.world;

import dev.array21.mc2fadiscord.Mc2FaDiscord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.data.BlockData;

import java.util.UUID;

public class WorldHandler {

    private static final Logger LOGGER = LogManager.getLogger(WorldHandler.class);

    private World world;
    private final Mc2FaDiscord plugin;

    public WorldHandler(final Mc2FaDiscord plugin) {
        this.plugin = plugin;
    }

    public World getWorld() {
        return this.world;
    }

    public void createWorldIfNotExists() {
        UUID worldUuid = this.plugin.getConfigManifest().getWorldUuid();
        if(worldUuid == null) {
            this.world = generateWorld();
            this.plugin.getConfigHandler().setWorldUuid(this.world.getUID());
        } else {
            World maybeWorld = Bukkit.getWorld(worldUuid);
            if(maybeWorld == null) {
                LOGGER.warn("worldUuid refers to a nonexistent world");
                this.world = generateWorld();
                this.plugin.getConfigHandler().setWorldUuid(this.world.getUID());
            } else {
                this.world = maybeWorld;
            }
        }

        this.world.setBlockData(0, 99, 0, Material.BARRIER.createBlockData());
    }

    private World generateWorld() {
        LOGGER.debug("Generating void world");
        WorldCreator worldCreator = new WorldCreator("mc2fadiscord");
        worldCreator.generator(new VoidGenerator());
        return worldCreator.createWorld();
    }
}
