package com.swiftcore.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuracion del mod, generada en config/swiftcore-common.toml
 * al iniciar el servidor/cliente por primera vez.
 */
public class SwiftCoreConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // --- Toggles generales ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADAPTIVE_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADAPTIVE_RANDOM_TICK;
    public static final ForgeConfigSpec.BooleanValue LOG_ADJUSTMENTS;

    // --- Distancia adaptativa ---
    public static final ForgeConfigSpec.DoubleValue TPS_THRESHOLD_LOW;
    public static final ForgeConfigSpec.DoubleValue TPS_THRESHOLD_RECOVER;
    public static final ForgeConfigSpec.IntValue MIN_VIEW_DISTANCE;
    public static final ForgeConfigSpec.IntValue MIN_SIMULATION_DISTANCE;
    public static final ForgeConfigSpec.IntValue CHECK_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue RECOVERY_STABLE_CHECKS;

    // --- Random tick adaptativo ---
    public static final ForgeConfigSpec.IntValue MIN_RANDOM_TICK_SPEED;

    static {
        BUILDER.push("General");

        ENABLE_ADAPTIVE_DISTANCE = BUILDER
                .comment("Activa el ajuste automatico de view-distance / simulation-distance segun el TPS del servidor.")
                .define("enableAdaptiveDistance", true);

        ENABLE_ADAPTIVE_RANDOM_TICK = BUILDER
                .comment("Activa el ajuste automatico de randomTickSpeed (crecimiento de cultivos, fuego, hojas) segun el TPS.")
                .define("enableAdaptiveRandomTick", true);

        LOG_ADJUSTMENTS = BUILDER
                .comment("Muestra en consola cuando SwiftCore ajusta distancias o random tick speed.")
                .define("logAdjustments", true);

        BUILDER.pop();

        BUILDER.push("TPS Thresholds");

        TPS_THRESHOLD_LOW = BUILDER
                .comment("TPS por debajo del cual SwiftCore empieza a reducir distancias/random tick (de 20.0 max).")
                .defineInRange("tpsThresholdLow", 18.0, 1.0, 20.0);

        TPS_THRESHOLD_RECOVER = BUILDER
                .comment("TPS por encima del cual SwiftCore empieza a restaurar los valores originales.")
                .defineInRange("tpsThresholdRecover", 19.5, 1.0, 20.0);

        RECOVERY_STABLE_CHECKS = BUILDER
                .comment("Numero de comprobaciones consecutivas estables por encima del umbral de recuperacion antes de restaurar un nivel.")
                .defineInRange("recoveryStableChecks", 3, 1, 20);

        BUILDER.pop();

        BUILDER.push("Distance Settings");

        MIN_VIEW_DISTANCE = BUILDER
                .comment("Distancia de vision minima a la que SwiftCore puede reducir el servidor (chunks).")
                .defineInRange("minViewDistance", 6, 3, 32);

        MIN_SIMULATION_DISTANCE = BUILDER
                .comment("Distancia de simulacion minima a la que SwiftCore puede reducir el servidor (chunks).")
                .defineInRange("minSimulationDistance", 4, 3, 32);

        CHECK_INTERVAL_TICKS = BUILDER
                .comment("Cada cuantos ticks se revisa el TPS para decidir ajustes (20 ticks = 1 segundo).")
                .defineInRange("checkIntervalTicks", 100, 20, 1200);

        BUILDER.pop();

        BUILDER.push("Random Tick Settings");

        MIN_RANDOM_TICK_SPEED = BUILDER
                .comment("Valor minimo al que SwiftCore puede reducir la gamerule randomTickSpeed durante lag.")
                .defineInRange("minRandomTickSpeed", 1, 0, 10);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
