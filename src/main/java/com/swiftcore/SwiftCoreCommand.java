package com.swiftcore;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SwiftCoreCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("swiftcore")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("status").executes(SwiftCoreCommand::status))
        );
    }

    private static int status(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        double tps = AdaptivePerformanceManager.getEstimatedTps();

        source.sendSuccess(() -> Component.literal(String.format("§b[SwiftCore] TPS estimado: §f%.1f / 20.0", tps)), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "§b[SwiftCore] View distance: §f%d §7(original: %d)",
                AdaptivePerformanceManager.getCurrentViewDistance(),
                AdaptivePerformanceManager.getOriginalViewDistance())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "§b[SwiftCore] Simulation distance: §f%d §7(original: %d)",
                AdaptivePerformanceManager.getCurrentSimulationDistance(),
                AdaptivePerformanceManager.getOriginalSimulationDistance())), false);
        source.sendSuccess(() -> Component.literal(String.format(
                "§b[SwiftCore] Random tick speed: §f%d §7(original: %d)",
                AdaptivePerformanceManager.getCurrentRandomTickSpeed(),
                AdaptivePerformanceManager.getOriginalRandomTickSpeed())), false);

        return 1;
    }
}
