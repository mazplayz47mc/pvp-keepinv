package com.pvpdeath;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvpDeath implements ModInitializer {
	public static final String MOD_ID = "pvpdeath";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Server-side only mod. All logic lives in the death-drop mixin.
		LOGGER.info("PvP Death Drops initialized. Items are dropped only when a player is killed by another player.");
	}
}