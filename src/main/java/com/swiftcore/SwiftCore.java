package com.swiftcore;

import com.swiftcore.config.SwiftCoreConfig;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SwiftCore - Mod de optimizacion server-side para Forge 1.20.1.
 *
 * No toca el pipeline de renderizado en ningun momento, por lo que
 * es compatible con Embeddium, Sodium, Oculus, Iris y cualquier mod
 * de mejora visual. Toda la logica ocurre en el lado del servidor
 * (o del servidor integrado en singleplayer).
 */
@Mod(SwiftCore.MOD_ID)
public class SwiftCore {

    public static final String MOD_ID = "swiftcore";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SwiftCore() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);

        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(ModConfig.Type.COMMON, SwiftCoreConfig.SPEC);

        LOGGER.info("[SwiftCore] Mod cargado. Optimizacion adaptativa server-side lista.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SwiftCoreCommand.register(event.getDispatcher());
    }
}
