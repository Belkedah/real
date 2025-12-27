package com.belkedouch.raidmod.raid;

import com.belkedouch.raidmod.config.RaidConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber
public class RaidManager {
    private static final Map<ServerLevel, RaidInstance> activeRaids = new HashMap<>();
    private static final Random random = new Random();
    private static int nextRaidTimer = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }

        ServerLevel level = (ServerLevel) event.level;
        
        // Check if there's an active raid
        RaidInstance raid = activeRaids.get(level);
        if (raid != null) {
            raid.tick();
            if (raid.isFinished()) {
                activeRaids.remove(level);
            }
            return;
        }

        // Schedule next raid
        if (nextRaidTimer <= 0) {
            startNewRaid(level);
            nextRaidTimer = RaidConfig.MIN_RAID_INTERVAL.get() + 
                          random.nextInt(RaidConfig.MAX_RAID_INTERVAL.get() - RaidConfig.MIN_RAID_INTERVAL.get());
        } else {
            nextRaidTimer--;
        }
    }

    private static void startNewRaid(ServerLevel level) {
        if (activeRaids.containsKey(level)) {
            return; // Already has an active raid
        }

        RaidInstance raid = new RaidInstance(level);
        activeRaids.put(level, raid);
        raid.start();
    }

    public static RaidInstance getActiveRaid(ServerLevel level) {
        return activeRaids.get(level);
    }

    public static boolean hasActiveRaid(ServerLevel level) {
        return activeRaids.containsKey(level);
    }
}


