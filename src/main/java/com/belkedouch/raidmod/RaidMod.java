package com.belkedouch.raidmod;

import com.belkedouch.raidmod.config.RaidConfig;
import com.belkedouch.raidmod.raid.RaidManager;
import com.belkedouch.raidmod.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RaidMod.MODID)
public class RaidMod {
    public static final String MODID = "raidmod";

    public RaidMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        modEventBus.addListener(this::commonSetup);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RaidConfig.SPEC);
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NetworkHandler.init();
        });
    }
}


