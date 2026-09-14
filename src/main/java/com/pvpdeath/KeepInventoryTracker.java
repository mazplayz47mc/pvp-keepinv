package com.pvpdeath;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class KeepInventoryTracker {
	// Players whose inventory must be transferred to the respawned entity.
	// Vanilla keepInventory works in TWO places: skipping the drop (done by
	// PlayerDeathMixin) AND copying the inventory during respawn via
	// ServerPlayer.restoreFrom(). Since we cancel the drop for natural deaths,
	// the UUID is recorded here so ServerPlayerRespawnMixin can perform the
	// vanilla transfer that would otherwise be skipped.
	public static final Set<UUID> KEEP_ON_RESPAWN = ConcurrentHashMap.newKeySet();

	private KeepInventoryTracker() {
	}
}