package com.belkedouch.raidmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RaidConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MODDED_MOBS;
    public static final ForgeConfigSpec.IntValue MIN_RAID_INTERVAL;
    public static final ForgeConfigSpec.IntValue MAX_RAID_INTERVAL;
    public static final ForgeConfigSpec.IntValue BASE_MOB_COUNT;
    public static final ForgeConfigSpec.DoubleValue DIFFICULTY_SCALING;
    public static final ForgeConfigSpec.DoubleValue ELITE_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CROWNED_CHANCE;
    public static final ForgeConfigSpec.IntValue GLOW_DURATION;
    public static final ForgeConfigSpec.BooleanValue MOBS_BREAK_WALLS;
    public static final ForgeConfigSpec.BooleanValue MOBS_CREATE_PATHS;

    static {
        BUILDER.push("Raid Settings");
        
        ENABLE_MODDED_MOBS = BUILDER
                .comment("Enable spawning of modded mobs in raids")
                .define("enableModdedMobs", true);
        
        MIN_RAID_INTERVAL = BUILDER
                .comment("Minimum time between raids (in ticks, 20 ticks = 1 second)")
                .defineInRange("minRaidInterval", 12000, 1000, 100000);
        
        MAX_RAID_INTERVAL = BUILDER
                .comment("Maximum time between raids (in ticks)")
                .defineInRange("maxRaidInterval", 24000, 2000, 200000);
        
        BASE_MOB_COUNT = BUILDER
                .comment("Base number of mobs per wave")
                .defineInRange("baseMobCount", 5, 1, 50);
        
        DIFFICULTY_SCALING = BUILDER
                .comment("Difficulty scaling per day (multiplier)")
                .defineInRange("difficultyScaling", 1.1, 1.0, 2.0);
        
        ELITE_CHANCE = BUILDER
                .comment("Chance for elite mobs to spawn (0.0 to 1.0)")
                .defineInRange("eliteChance", 0.15, 0.0, 1.0);
        
        CROWNED_CHANCE = BUILDER
                .comment("Chance for crowned mobs to spawn (0.0 to 1.0)")
                .defineInRange("crownedChance", 0.05, 0.0, 1.0);
        
        GLOW_DURATION = BUILDER
                .comment("Duration before mobs start glowing (in ticks)")
                .defineInRange("glowDuration", 6000, 1000, 20000);
        
        MOBS_BREAK_WALLS = BUILDER
                .comment("Allow mobs to break walls")
                .define("mobsBreakWalls", true);
        
        MOBS_CREATE_PATHS = BUILDER
                .comment("Allow mobs to create climbable paths")
                .define("mobsCreatePaths", true);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}


