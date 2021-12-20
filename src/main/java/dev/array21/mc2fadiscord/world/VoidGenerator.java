package dev.array21.mc2fadiscord.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(final World world, final Random random, final int x, final int z, final BiomeGrid biomeGrid) {
        return super.createChunkData(world);
    }
}
