package com.belkedouch.raidmod.raid;

import com.belkedouch.raidmod.config.RaidConfig;
import com.belkedouch.raidmod.mobs.RaidMobHandler;
import com.belkedouch.raidmod.rewards.RewardManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import java.util.HashMap;
import java.util.Map;

public class RaidInstance {
    private final ServerLevel level;
    private final List<UUID> spawnedMobs = new ArrayList<>();
    private int waveNumber = 0;
    private int waveTimer = 0;
    private int raidStartTime = 0;
    private boolean finished = false;
    private BlockPos targetPosition;
    private final Set<BlockPos> brokenBlocks = new HashSet<>();
    private final Map<BlockPos, net.minecraft.world.level.block.state.BlockState> originalBlocks = new HashMap<>();
    
    public void storeOriginalBlock(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (!originalBlocks.containsKey(pos)) {
            originalBlocks.put(pos, state);
        }
    }

    public RaidInstance(ServerLevel level) {
        this.level = level;
    }

    public void start() {
        this.raidStartTime = level.getGameTime();
        this.waveNumber = 1;
        this.waveTimer = 0;
        this.targetPosition = findTargetPosition();
        
        broadcastMessage(Component.literal("§c[RAID] Un raid commence ! Préparez-vous !"));
        spawnWave();
    }

    public void tick() {
        if (finished) return;

        waveTimer++;
        
        // Check if all mobs are dead
        if (spawnedMobs.isEmpty() && waveTimer > 100) {
            finishRaid(true);
            return;
        }

        // Spawn next wave if needed
        if (waveTimer > 6000 && spawnedMobs.size() < 3) { // 5 minutes
            waveNumber++;
            spawnWave();
            waveTimer = 0;
        }

        // Apply glow effect if raid lasts long
        if (level.getGameTime() - raidStartTime > RaidConfig.GLOW_DURATION.get()) {
            RaidMobHandler.applyGlowToRaidMobs(level, spawnedMobs);
        }

        // Update mob behavior
        RaidMobHandler.updateRaidMobs(level, spawnedMobs, targetPosition, brokenBlocks);
        
        // Check for breach (mobs reached target)
        checkForBreach();
    }

    private void spawnWave() {
        int dayCount = (int) (level.getDayTime() / 24000);
        double difficultyMultiplier = Math.pow(RaidConfig.DIFFICULTY_SCALING.get(), dayCount);
        int mobCount = (int) (RaidConfig.BASE_MOB_COUNT.get() * difficultyMultiplier * waveNumber);

        broadcastMessage(Component.literal("§c[RAID] Vague " + waveNumber + " - " + mobCount + " ennemis !"));

        List<UUID> newMobs = RaidMobHandler.spawnRaidWave(level, targetPosition, mobCount, waveNumber);
        spawnedMobs.addAll(newMobs);
    }

    private BlockPos findTargetPosition() {
        // Find nearest player or bed
        BlockPos bedPos = findNearestBed();
        Player nearestPlayer = findNearestPlayer();
        
        if (bedPos != null && nearestPlayer != null) {
            double bedDist = bedPos.distSqr(nearestPlayer.blockPosition());
            if (bedDist < 10000) { // Within 100 blocks
                return bedPos;
            }
        }
        
        if (bedPos != null) {
            return bedPos;
        }
        
        if (nearestPlayer != null) {
            return nearestPlayer.blockPosition();
        }
        
        return new BlockPos(0, 64, 0); // Default spawn
    }

    private BlockPos findNearestBed() {
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (ServerPlayer player : level.players()) {
            BlockPos bedPos = player.getRespawnPosition();
            if (bedPos != null) {
                BlockState state = level.getBlockState(bedPos);
                if (state.getBlock() instanceof BedBlock) {
                    double dist = bedPos.distSqr(player.blockPosition());
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = bedPos;
                    }
                }
            }
        }
        
        return nearest;
    }

    private Player findNearestPlayer() {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (ServerPlayer player : level.players()) {
            if (player.isAlive()) {
                double dist = targetPosition != null ? 
                    targetPosition.distSqr(player.blockPosition()) : 0;
                if (nearest == null || dist < nearestDist) {
                    nearestDist = dist;
                    nearest = player;
                }
            }
        }
        
        return nearest;
    }

    private void finishRaid(boolean victory) {
        finished = true;
        
        if (victory) {
            broadcastMessage(Component.literal("§a[RAID] Victoire ! Le raid est terminé !"));
            RewardManager.giveRewards(level, targetPosition, waveNumber);
            repairWalls();
        } else {
            broadcastMessage(Component.literal("§c[RAID] Le raid a échoué."));
        }
        
        // Clean up remaining mobs
        RaidMobHandler.cleanupRaidMobs(level, spawnedMobs);
    }

    private void repairWalls() {
        for (BlockPos pos : brokenBlocks) {
            net.minecraft.world.level.block.state.BlockState originalState = originalBlocks.get(pos);
            if (originalState != null) {
                level.setBlock(pos, originalState, 3);
            }
        }
        brokenBlocks.clear();
        originalBlocks.clear();
    }

    private void broadcastMessage(Component message) {
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public BlockPos getTargetPosition() {
        return targetPosition;
    }

    public void addBrokenBlock(BlockPos pos) {
        brokenBlocks.add(pos);
    }
    
    private void checkForBreach() {
        if (targetPosition == null) return;
        
        for (UUID uuid : spawnedMobs) {
            if (level.getEntity(uuid) instanceof net.minecraft.world.entity.Mob mob) {
                if (mob.blockPosition().distSqr(targetPosition) < 25) { // Within 5 blocks
                    broadcastMessage(Component.literal("§c[RAID] Les ennemis ont percé vos défenses !"));
                    finishRaid(false);
                    return;
                }
            }
        }
    }
}

