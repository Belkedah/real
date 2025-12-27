package com.belkedouch.raidmod.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public class RaidMoveToTargetGoal extends Goal {
    private final Mob mob;
    private final BlockPos targetPos;
    private int stuckTimer = 0;

    public RaidMoveToTargetGoal(Mob mob, BlockPos targetPos) {
        this.mob = mob;
        this.targetPos = targetPos;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.isAlive() && targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && mob.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 4.0;
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
    }

    @Override
    public void tick() {
        // Update target to nearest player or bed
        BlockPos currentTarget = findCurrentTarget();
        
        if (currentTarget != null) {
            mob.getNavigation().moveTo(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), 1.0);
            mob.getLookControl().setLookAt(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ());
        }
        
        // Check if stuck
        if (mob.getNavigation().isDone()) {
            stuckTimer++;
            if (stuckTimer > 20) {
                // Try to jump or break block
                BlockPos mobPos = mob.blockPosition();
                if (!mob.level().getBlockState(mobPos.above()).isAir()) {
                    // Block above, try to break it or go around
                    avoidObstacle();
                }
                stuckTimer = 0;
            }
        } else {
            stuckTimer = 0;
        }
    }

    private BlockPos findCurrentTarget() {
        // Find nearest player
        Player nearestPlayer = mob.level().getNearestPlayer(mob, 64.0);
        if (nearestPlayer != null) {
            return nearestPlayer.blockPosition();
        }
        
        return targetPos;
    }

    private void avoidObstacle() {
        // Simple obstacle avoidance - move to side
        BlockPos mobPos = mob.blockPosition();
        BlockPos[] offsets = {
            mobPos.north(),
            mobPos.south(),
            mobPos.east(),
            mobPos.west(),
            mobPos.north().east(),
            mobPos.north().west(),
            mobPos.south().east(),
            mobPos.south().west()
        };
        
        for (BlockPos offset : offsets) {
            if (mob.level().getBlockState(offset).isAir() && 
                mob.level().getBlockState(offset.below()).isSolid()) {
                mob.getNavigation().moveTo(offset.getX(), offset.getY(), offset.getZ(), 1.0);
                break;
            }
        }
    }
}

