package com.belkedouch.raidmod.mobs;

import com.belkedouch.raidmod.config.RaidConfig;
import com.belkedouch.raidmod.raid.RaidInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class RaidMobHandler {
    private static final Random random = new Random();
    private static final List<EntityType<? extends Mob>> HOSTILE_MOBS = Arrays.asList(
            EntityType.ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CREEPER,
            EntityType.ENDERMAN,
            EntityType.WITCH,
            EntityType.PILLAGER,
            EntityType.VINDICATOR,
            EntityType.EVOKER,
            EntityType.VEX
    );

    public static List<UUID> spawnRaidWave(ServerLevel level, BlockPos targetPos, int count, int waveNumber) {
        List<UUID> spawned = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = findSpawnPosition(level, targetPos);
            if (spawnPos == null) continue;

            EntityType<? extends Mob> mobType = selectMobType(waveNumber);
            Mob mob = mobType.create(level);
            
            if (mob != null) {
                mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 
                          random.nextFloat() * 360, 0);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), 
                                 MobSpawnType.EVENT, null, null);
                
                // Apply variants
                applyMobVariant(mob, waveNumber);
                
                // Buff mob
                buffMob(mob, waveNumber);
                
                // Set custom AI
                setupRaidMobAI(mob, targetPos);
                
                level.addFreshEntity(mob);
                spawned.add(mob.getUUID());
            }
        }
        
        return spawned;
    }

    private static EntityType<? extends Mob> selectMobType(int waveNumber) {
        // Random selection from hostile mobs
        return HOSTILE_MOBS.get(random.nextInt(HOSTILE_MOBS.size()));
    }

    private static void applyMobVariant(Mob mob, int waveNumber) {
        double rand = random.nextDouble();
        
        if (rand < RaidConfig.CROWNED_CHANCE.get()) {
            // Crowned variant - strongest
            mob.setCustomName(net.minecraft.network.chat.Component.literal("§6[Couronné] " + mob.getName().getString()));
            mob.setCustomNameVisible(true);
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 2));
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 1));
        } else if (rand < RaidConfig.CROWNED_CHANCE.get() + RaidConfig.ELITE_CHANCE.get()) {
            // Elite variant
            mob.setCustomName(net.minecraft.network.chat.Component.literal("§5[Élite] " + mob.getName().getString()));
            mob.setCustomNameVisible(true);
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 1));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, 0));
        }
    }

    private static void buffMob(Mob mob, int waveNumber) {
        // Base buffs for all raid mobs
        mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
        
        // Scale with wave
        if (waveNumber > 3) {
            mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, 0));
        }
    }

    private static void setupRaidMobAI(Mob mob, BlockPos targetPos) {
        // Clear default goals and add custom raid behavior
        mob.goalSelector.removeAllGoals();
        mob.targetSelector.removeAllGoals();
        
        // Add attack player goal
        mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, Player.class, true));
        
        // Add move to target goal with pathfinding
        mob.goalSelector.addGoal(1, new RaidMoveToTargetGoal(mob, targetPos));
    }

    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos targetPos) {
        // Spawn in a circle around target
        int radius = 30 + random.nextInt(20);
        double angle = random.nextDouble() * Math.PI * 2;
        
        int x = targetPos.getX() + (int)(radius * Math.cos(angle));
        int z = targetPos.getZ() + (int)(radius * Math.sin(angle));
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
        
        BlockPos pos = new BlockPos(x, y, z);
        
        // Find valid spawn position
        for (int i = 0; i < 10; i++) {
            BlockPos checkPos = pos.above(i);
            if (level.getBlockState(checkPos).isAir() && 
                level.getBlockState(checkPos.below()).isSolid()) {
                return checkPos;
            }
        }
        
        return pos;
    }

    public static void updateRaidMobs(ServerLevel level, List<UUID> mobUUIDs, BlockPos targetPos, Set<BlockPos> brokenBlocks) {
        for (UUID uuid : new ArrayList<>(mobUUIDs)) {
            if (level.getEntity(uuid) instanceof Mob mob) {
                if (!mob.isAlive()) {
                    mobUUIDs.remove(uuid);
                    continue;
                }
                
                // Update target if bed is closer
                BlockPos newTarget = findBestTarget(level, mob, targetPos);
                if (newTarget != null) {
                    updateMobTarget(mob, newTarget, brokenBlocks);
                }
            } else {
                mobUUIDs.remove(uuid);
            }
        }
    }

    private static BlockPos findBestTarget(ServerLevel level, Mob mob, BlockPos currentTarget) {
        BlockPos mobPos = mob.blockPosition();
        BlockPos bedPos = findNearestBed(level, mobPos);
        Player nearestPlayer = findNearestPlayer(level, mobPos);
        
        if (bedPos != null) {
            double bedDist = mobPos.distSqr(bedPos);
            if (nearestPlayer != null) {
                double playerDist = mobPos.distSqr(nearestPlayer.blockPosition());
                if (bedDist < playerDist * 1.5) { // Prefer bed if within 1.5x distance
                    return bedPos;
                }
            } else {
                return bedPos;
            }
        }
        
        if (nearestPlayer != null) {
            return nearestPlayer.blockPosition();
        }
        
        return currentTarget;
    }

    private static BlockPos findNearestBed(ServerLevel level, BlockPos from) {
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (Player player : level.players()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                BlockPos bedPos = serverPlayer.getRespawnPosition();
                if (bedPos != null) {
                    BlockState state = level.getBlockState(bedPos);
                    if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                        double dist = from.distSqr(bedPos);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = bedPos;
                        }
                    }
                }
            }
        }
        
        return nearest;
    }

    private static Player findNearestPlayer(ServerLevel level, BlockPos from) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        
        for (Player player : level.players()) {
            if (player.isAlive()) {
                double dist = from.distSqr(player.blockPosition());
                if (nearest == null || dist < nearestDist) {
                    nearestDist = dist;
                    nearest = player;
                }
            }
        }
        
        return nearest;
    }

    private static void updateMobTarget(Mob mob, BlockPos target, Set<BlockPos> brokenBlocks) {
        if (!RaidConfig.MOBS_BREAK_WALLS.get() && !RaidConfig.MOBS_CREATE_PATHS.get()) {
            return;
        }
        
        BlockPos mobPos = mob.blockPosition();
        BlockPos pathTarget = findPathToTarget(mob.level(), mobPos, target, brokenBlocks);
        
        if (pathTarget != null) {
            mob.getNavigation().moveTo(pathTarget.getX(), pathTarget.getY(), pathTarget.getZ(), 1.0);
        }
    }

    private static BlockPos findPathToTarget(net.minecraft.world.level.Level level, BlockPos from, BlockPos to, Set<BlockPos> brokenBlocks) {
        // Simple pathfinding - break walls or create paths
        if (RaidConfig.MOBS_BREAK_WALLS.get()) {
            BlockPos direction = to.subtract(from);
            BlockPos next = from.offset(
                Integer.signum(direction.getX()),
                0,
                Integer.signum(direction.getZ())
            );
            
            BlockState state = level.getBlockState(next);
            if (!state.isAir() && state.isSolid() && 
                !state.getBlock().equals(Blocks.BEDROCK) &&
                !brokenBlocks.contains(next)) {
                // Store original block state before breaking
                if (level instanceof ServerLevel serverLevel) {
                    RaidInstance raid = com.belkedouch.raidmod.raid.RaidManager.getActiveRaid(serverLevel);
                    if (raid != null) {
                        raid.storeOriginalBlock(next, state);
                    }
                }
                level.destroyBlock(next, true);
                brokenBlocks.add(next);
            }
        }
        
        if (RaidConfig.MOBS_CREATE_PATHS.get()) {
            // Create climbable path if needed
            BlockPos above = from.above();
            if (!level.getBlockState(above).isAir() && 
                level.getBlockState(above.above()).isAir()) {
                level.setBlock(above, Blocks.LADDER.defaultBlockState(), 3);
            }
        }
        
        return to;
    }

    public static void applyGlowToRaidMobs(ServerLevel level, List<UUID> mobUUIDs) {
        for (UUID uuid : mobUUIDs) {
            if (level.getEntity(uuid) instanceof LivingEntity entity) {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
            }
        }
    }

    public static void cleanupRaidMobs(ServerLevel level, List<UUID> mobUUIDs) {
        for (UUID uuid : new ArrayList<>(mobUUIDs)) {
            if (level.getEntity(uuid) instanceof Mob mob) {
                mob.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
        }
        mobUUIDs.clear();
    }
}

