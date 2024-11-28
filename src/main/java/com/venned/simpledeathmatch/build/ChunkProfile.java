package com.venned.simpledeathmatch.build;

import com.venned.simpledeathmatch.Main;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class ChunkProfile {

    private final Chunk chunk;
    private final ChunkSnapshot snapshot;
    private final Set<Block> placedBlocks;

    public ChunkProfile(Chunk chunk) {
        this.chunk = chunk;
        this.snapshot = chunk.getChunkSnapshot();
        this.placedBlocks = new HashSet<>();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void addPlacedBlock(Block block) {
        placedBlocks.add(block);
    }

    public void regenerate(Main plugin) {
        // Regenerate the chunk to its original state asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                            Material originalMaterial = snapshot.getBlockType(x, y, z);
                            Block block = chunk.getBlock(x, y, z);
                            if (block.getType() != originalMaterial) {
                                block.setType(originalMaterial, false);
                            }
                        }
                    }
                }
                // Remove any placed blocks that are not in the original state
                for (Block block : placedBlocks) {
                    Material originalMaterial = snapshot.getBlockType(block.getX() & 15, block.getY(), block.getZ() & 15);
                    if (block.getType() != originalMaterial) {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }.runTask(plugin);
    }
}
