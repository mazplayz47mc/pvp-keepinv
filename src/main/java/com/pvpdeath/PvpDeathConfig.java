package com.pvpdeath;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Runtime configuration, stored as JSON in {@code config/pvpdeath.json}.
 *
 * <p>Reload at any time with {@code /pvpdeath reload}.</p>
 */
public final class PvpDeathConfig {
	/** Master switch. When false the mod does nothing and vanilla behaviour applies. */
	public boolean enabled = true;

	/** Natural (non-PvP) death: keep the inventory. */
	public boolean keepItemsOnNaturalDeath = true;

	/** Natural (non-PvP) death: keep experience and levels. */
	public boolean keepXpOnNaturalDeath = true;

	/** PvP death: drop the inventory. Inverse of the old "keep on pvp" switch. */
	public boolean dropItemsOnPvp = true;

	/** PvP death: drop experience. Inverse of the old "keep xp on pvp" switch. */
	public boolean dropXpOnPvp = true;

	/**
	 * When a player was hit by another player recently, a later death counts as a
	 * PvP death even if the killing blow was environmental (fall, drowning, fire...).
	 * This is the fix for "killed by fall damage right after my friend hit me".
	 */
	public boolean recentPlayerHitCountsAsPvp = true;

	/** Length of the recent-player-hit window, in seconds. */
	public double recentPlayerHitWindowSeconds = 15.0;

	/** Extra logging for diagnosing why a death was classified one way or the other. */
	public boolean debugLogging = false;

	private static PvpDeathConfig instance = new PvpDeathConfig();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private PvpDeathConfig() {
	}

	public static PvpDeathConfig get() {
		return instance;
	}

	public static Path configFile() {
		return FabricLoader.getInstance().getConfigDir().resolve("pvpdeath.json");
	}

	/** Loads the config file, creating it with defaults on first run. Never throws. */
	public static void load() {
		Path file = configFile();
		try {
			if (Files.exists(file)) {
				try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
					PvpDeathConfig loaded = GSON.fromJson(reader, PvpDeathConfig.class);
					if (loaded != null) {
						instance = loaded;
					}
				}
			} else {
				save();
			}
		} catch (Exception e) {
			PvpDeath.LOGGER.error("Could not read {}, keeping current settings", file, e);
		}
	}

	/** Writes the current settings back to disk. Never throws. */
	public static void save() {
		Path file = configFile();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, writer);
			}
		} catch (Exception e) {
			PvpDeath.LOGGER.error("Could not write {}", file, e);
		}
	}

	/** True when this death happened within the configured window of a player hit. */
	public long recentHitWindowTicks() {
		double seconds = recentPlayerHitWindowSeconds;
		if (seconds < 0.0) {
			seconds = 0.0;
		}
		return Math.round(seconds * 20.0);
	}
}
