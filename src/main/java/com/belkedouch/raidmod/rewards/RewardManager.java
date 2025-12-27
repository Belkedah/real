package com.belkedouch.raidmod.rewards;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Random;

public class RewardManager {
    private static final Random random = new Random();

    public static void giveRewards(ServerLevel level, BlockPos centerPos, int waveNumber) {
        int rewardCount = 3 + waveNumber; // More rewards for higher waves
        
        for (int i = 0; i < rewardCount; i++) {
            BlockPos dropPos = centerPos.offset(
                random.nextInt(5) - 2,
                1,
                random.nextInt(5) - 2
            );
            
            ItemStack reward = generateReward(waveNumber);
            net.minecraft.world.entity.item.ItemEntity itemEntity = 
                new net.minecraft.world.entity.item.ItemEntity(level, 
                    dropPos.getX() + 0.5, dropPos.getY(), dropPos.getZ() + 0.5, reward);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    private static ItemStack generateReward(int waveNumber) {
        // Random rewards based on wave
        int roll = random.nextInt(100);
        
        if (roll < 10) {
            // Rare items
            return new ItemStack(Items.DIAMOND, 1 + random.nextInt(3));
        } else if (roll < 25) {
            // Good items
            return new ItemStack(Items.EMERALD, 2 + random.nextInt(5));
        } else if (roll < 50) {
            // Medium items
            return new ItemStack(Items.GOLD_INGOT, 3 + random.nextInt(8));
        } else if (roll < 75) {
            // Common items
            return new ItemStack(Items.IRON_INGOT, 5 + random.nextInt(10));
        } else {
            // Basic items
            ItemStack[] basics = {
                new ItemStack(Items.BREAD, 5 + random.nextInt(10)),
                new ItemStack(Items.COAL, 10 + random.nextInt(20)),
                new ItemStack(Items.ARROW, 20 + random.nextInt(40)),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 1 + random.nextInt(3))
            };
            return basics[random.nextInt(basics.length)];
        }
    }
}


