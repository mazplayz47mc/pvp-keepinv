package com.pvpdeath;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player state shared between the death mixins and the respawn mixin.
 *
 * <p>Two things are tracked:</p>
 * <ol>
 *   <li>{@link Decision} - what the victim keeps on <b>this</b> death. Written once by
 *       {@code PlayerDeathMixin} at the start of {@code dropAllDeathLoot} and consumed by
 *       {@code ServerPlayerRespawnMixin} so nothing can leak into a later death.</li>
 *   <li>Recent player hits - the game time a player was last damaged by another player,
 *       used to classify an environmental death as a PvP death.</li>
 * </ol>
 */
public final class KeepInventoryTracker {
	/** What to give back to the player when they respawn. */
	public record Decision(boolean keepItems, boolean keepXp) {
	}

	private static final Map<UUID, Decision> DECISIONS = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> RECENT_PLAYER_HITS = new ConcurrentHashMap<>();

	private KeepInventoryTracker() {
	}

	/** Records that {@code victim} was damaged by another player at {@code gameTime}. */
	public static void recordPlayerHit(UUID victim, long gameTime) {
		RECENT_PLAYER_HITS.put(victim, gameTime);
	}

	/**
	 * True when the victim was hit by another player within {@code windowTicks} of
	 * {@code gameTime}. Entries that have aged out are dropped as they are seen.
	 */
	public static boolean wasRecentlyHitByPlayer(UUID victim, long gameTime, long windowTicks) {
		Long hitAt = RECENT_PLAYER_HITS.get(victim);
		if (hitAt == null) {
			return false;
		}
		if (windowTicks <= 0) {
			return gameTime - hitAt <= 0;
		}
		if (gameTime - hitAt > windowTicks) {
			RECENT_PLAYER_HITS.remove(victim, hitAt);
			return false;
		}
		return true;
	}

	/** Stores the keep/drop decision for the death currently being processed. */
	public static void setDecision(UUID victim, Decision decision) {
		DECISIONS.put(victim, decision);
	}

	/** Looks up the pending decision without consuming it. */
	public static Decision peekDecision(UUID victim) {
		return DECISIONS.get(victim);
	}

	/** Removes and returns the pending decision, or null when there is none. */
	public static Decision consumeDecision(UUID victim) {
		return DECISIONS.remove(victim);
	}

	/** Drops all state for a player, e.g. after they disconnect. */
	public static void forget(UUID player) {
		DECISIONS.remove(player);
		RECENT_PLAYER_HITS.remove(player);
	}
}
