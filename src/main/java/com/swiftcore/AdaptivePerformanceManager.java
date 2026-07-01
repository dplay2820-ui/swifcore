package com.swiftcore;

import com.swiftcore.config.SwiftCoreConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Monitorea el TPS real del servidor (no el "20 TPS" que reporta el reloj,
 * sino el tiempo real que tarda cada tick) y ajusta de forma progresiva:
 *
 *  1) view-distance / simulation-distance del servidor
 *  2) la gamerule randomTickSpeed
 *
 * cuando detecta lag sostenido, restaurando los valores originales en cuanto
 * el servidor se estabiliza. No modifica renderizado ni mallas de chunks,
 * por lo que es compatible con Embeddium/Sodium/Oculus/Iris sin conflictos.
 */
@Mod.EventBusSubscriber(modid = SwiftCore.MOD_ID)
public class AdaptivePerformanceManager {

    private static final double NANOS_PER_TICK_GOAL = 1_000_000_000.0 / 20.0;

    private static long lastTickStart = 0L;
    private static double emaTickTimeNanos = NANOS_PER_TICK_GOAL;
    private static int tickCounter = 0;

    private static int originalViewDistance = -1;
    private static int originalSimulationDistance = -1;
    private static int originalRandomTickSpeed = -1;

    private static int currentViewDistance = -1;
    private static int currentSimulationDistance = -1;
    private static int currentRandomTickSpeed = -1;

    private static int stableGoodChecks = 0;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        originalViewDistance = server.getPlayerList().getViewDistance();
        originalSimulationDistance = server.getPlayerList().getSimulationDistance();

        ServerLevel overworld = server.overworld();
        if (overworld != null) {
            originalRandomTickSpeed = overworld.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        } else {
            originalRandomTickSpeed = 3;
        }

        currentViewDistance = originalViewDistance;
        currentSimulationDistance = originalSimulationDistance;
        currentRandomTickSpeed = originalRandomTickSpeed;
        stableGoodChecks = 0;

        SwiftCore.LOGGER.info("[SwiftCore] Valores originales detectados -> viewDistance={}, simulationDistance={}, randomTickSpeed={}",
                originalViewDistance, originalSimulationDistance, originalRandomTickSpeed);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        long now = System.nanoTime();
        if (lastTickStart != 0L) {
            long delta = now - lastTickStart;
            // Media movil exponencial para suavizar picos puntuales
            emaTickTimeNanos = (emaTickTimeNanos * 0.9) + (delta * 0.1);
        }
        lastTickStart = now;

        tickCounter++;
        if (tickCounter < SwiftCoreConfig.CHECK_INTERVAL_TICKS.get()) return;
        tickCounter = 0;

        MinecraftServer server = event.getServer();
        if (originalViewDistance == -1) return; // aun no arranco del todo

        double estimatedTps = Math.min(20.0, 1_000_000_000.0 / emaTickTimeNanos);

        boolean lagging = estimatedTps < SwiftCoreConfig.TPS_THRESHOLD_LOW.get();
        boolean recovered = estimatedTps > SwiftCoreConfig.TPS_THRESHOLD_RECOVER.get();

        if (lagging) {
            stableGoodChecks = 0;
            if (SwiftCoreConfig.ENABLE_ADAPTIVE_DISTANCE.get()) reduceDistances(server, estimatedTps);
            if (SwiftCoreConfig.ENABLE_ADAPTIVE_RANDOM_TICK.get()) reduceRandomTick(server, estimatedTps);
        } else if (recovered) {
            stableGoodChecks++;
            if (stableGoodChecks >= SwiftCoreConfig.RECOVERY_STABLE_CHECKS.get()) {
                stableGoodChecks = 0;
                if (SwiftCoreConfig.ENABLE_ADAPTIVE_DISTANCE.get()) restoreDistances(server, estimatedTps);
                if (SwiftCoreConfig.ENABLE_ADAPTIVE_RANDOM_TICK.get()) restoreRandomTick(server, estimatedTps);
            }
        } else {
            stableGoodChecks = 0;
        }
    }

    private static void reduceDistances(MinecraftServer server, double tps) {
        int minView = SwiftCoreConfig.MIN_VIEW_DISTANCE.get();
        int minSim = SwiftCoreConfig.MIN_SIMULATION_DISTANCE.get();
        boolean changed = false;

        if (currentSimulationDistance > minSim) {
            currentSimulationDistance--;
            server.getPlayerList().setSimulationDistance(currentSimulationDistance);
            changed = true;
        }
        if (currentViewDistance > minView) {
            currentViewDistance--;
            server.getPlayerList().setViewDistance(currentViewDistance);
            changed = true;
        }

        if (changed && SwiftCoreConfig.LOG_ADJUSTMENTS.get()) {
            SwiftCore.LOGGER.info("[SwiftCore] TPS bajo ({}) -> reduciendo a viewDistance={}, simulationDistance={}",
                    String.format("%.1f", tps), currentViewDistance, currentSimulationDistance);
        }
    }

    private static void restoreDistances(MinecraftServer server, double tps) {
        boolean changed = false;

        if (currentSimulationDistance < originalSimulationDistance) {
            currentSimulationDistance++;
            server.getPlayerList().setSimulationDistance(currentSimulationDistance);
            changed = true;
        }
        if (currentViewDistance < originalViewDistance) {
            currentViewDistance++;
            server.getPlayerList().setViewDistance(currentViewDistance);
            changed = true;
        }

        if (changed && SwiftCoreConfig.LOG_ADJUSTMENTS.get()) {
            SwiftCore.LOGGER.info("[SwiftCore] TPS estable ({}) -> restaurando a viewDistance={}, simulationDistance={}",
                    String.format("%.1f", tps), currentViewDistance, currentSimulationDistance);
        }
    }

    private static void reduceRandomTick(MinecraftServer server, double tps) {
        int min = SwiftCoreConfig.MIN_RANDOM_TICK_SPEED.get();
        if (currentRandomTickSpeed <= min) return;

        currentRandomTickSpeed--;
        applyRandomTickSpeed(server, currentRandomTickSpeed);

        if (SwiftCoreConfig.LOG_ADJUSTMENTS.get()) {
            SwiftCore.LOGGER.info("[SwiftCore] TPS bajo ({}) -> randomTickSpeed={}",
                    String.format("%.1f", tps), currentRandomTickSpeed);
        }
    }

    private static void restoreRandomTick(MinecraftServer server, double tps) {
        if (currentRandomTickSpeed >= originalRandomTickSpeed) return;

        currentRandomTickSpeed++;
        applyRandomTickSpeed(server, currentRandomTickSpeed);

        if (SwiftCoreConfig.LOG_ADJUSTMENTS.get()) {
            SwiftCore.LOGGER.info("[SwiftCore] TPS estable ({}) -> randomTickSpeed={}",
                    String.format("%.1f", tps), currentRandomTickSpeed);
        }
    }

    private static void applyRandomTickSpeed(MinecraftServer server, int value) {
        for (ServerLevel level : server.getAllLevels()) {
            level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(value, server);
        }
    }

    public static double getEstimatedTps() {
        return Math.min(20.0, 1_000_000_000.0 / emaTickTimeNanos);
    }

    public static int getCurrentViewDistance() { return currentViewDistance; }
    public static int getCurrentSimulationDistance() { return currentSimulationDistance; }
    public static int getCurrentRandomTickSpeed() { return currentRandomTickSpeed; }
    public static int getOriginalViewDistance() { return originalViewDistance; }
    public static int getOriginalSimulationDistance() { return originalSimulationDistance; }
    public static int getOriginalRandomTickSpeed() { return originalRandomTickSpeed; }
}
