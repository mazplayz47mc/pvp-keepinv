package com.pvpdeath;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvpDeath implements ModInitializer {
	public static final String MOD_ID = "pvpdeath";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PvpDeathConfig.load();
		registerCommands();

		LOGGER.info("PvP Death Drops {} initialized (config: {})",
				version(), PvpDeathConfig.configFile());
	}

	/** {@code /pvpdeath reload} and {@code /pvpdeath} for a status summary. */
	private static void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("pvpdeath")
						.requires(source -> source.permissions().hasPermission(
								new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
						.executes(context -> {
							PvpDeathConfig config = PvpDeathConfig.get();
							context.getSource().sendSuccess(() -> Component.literal(String.format(
									"PvP Death Drops %s | enabled=%s, natural: keepItems=%s keepXp=%s, "
											+ "pvp: dropItems=%s dropXp=%s, hitWindow=%ss (recentHits=%s), debug=%s",
									version(),
									config.enabled,
									config.keepItemsOnNaturalDeath, config.keepXpOnNaturalDeath,
									config.dropItemsOnPvp, config.dropXpOnPvp,
									config.recentPlayerHitWindowSeconds, config.recentPlayerHitCountsAsPvp,
									config.debugLogging)), false);
							return 1;
						})
						.then(Commands.literal("reload").executes(context -> {
							PvpDeathConfig.load();
							PvpDeathConfig config = PvpDeathConfig.get();
							context.getSource().sendSuccess(() -> Component.literal(
									"Reloaded " + PvpDeathConfig.configFile()
											+ " (enabled=" + config.enabled + ")"), false);
							return 1;
						}))));
	}

	private static String version() {
		return net.fabricmc.loader.api.FabricLoader.getInstance()
				.getModContainer(MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("dev");
	}

	/** Debug helper: logs only when {@code debugLogging} is on in the config. */
	public static void debug(String message) {
		if (PvpDeathConfig.get().debugLogging) {
			LOGGER.info("[pvpdeath] {}", message);
		}
	}
}
