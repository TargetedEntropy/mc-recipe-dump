package com.recipedump;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RecipeDumpMod.MODID)
public class RecipeDumpMod {
    public static final String MODID = "recipedump";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RecipeDumpMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new RecipeDumpCommand());

        LOGGER.info("Recipe Dump mod initialized");
    }
}