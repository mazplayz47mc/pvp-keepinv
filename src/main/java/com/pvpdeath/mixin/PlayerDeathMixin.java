package com.pvpdeath.mixin;

import com.pvpdeath.KeepInventoryTracker;
import com.pvpdeath.KeepInventoryTracker.Decision;
import com.pvpdeath.PvpDeath;
import com.pvpdeath.PvpDeathConfig;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PlayerDeathMixin {
	/**
	 * Records every hit a player takes from another player, no matter what the damage
	 * source is. Vanilla only remembers the killer for a fixed 100 ticks and only looks at
	 * the source entity of the killing blow, so a fall/water/lava death right after a punch
	 * looked like a natural death. This is the tracker that fixes that.
	 */
	@Inject(method = "resolvePlayerResponsibleForDamage", at = @At("HEAD"))
	private void pvpdeath$recordPlayerHit(DamageSource damageSource, CallbackInfoReturnable<Player> cir) {
		if (!((Object) this instanceof Player victim)) {
			return;
		}
		if (!(damageSource.getEntity() instanceof Player attacker) || attacker == victim) {
			return;
		}

		KeepInventoryTracker.recordPlayerHit(victim.getUUID(), victim.level().getGameTime());
		PvpDeath.debug("Recorded PvP hit: " + attacker.getScoreboardName()
				+ " -> " + victim.getScoreboardName()
				+ " @ tick " + victim.level().getGameTime());
	}

	/**
	 * Computes the keep/drop decision for this death. Runs for players only; every other
	 * mob keeps vanilla loot.
	 */
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
	private void pvpdeath$decideWhatToKeep(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
		// Only affect players. Mobs keep their normal vanilla death loot.
		if (!((Object) this instanceof Player player)) {
			return;
		}

		PvpDeathConfig config = PvpDeathConfig.get();
		if (!config.enabled) {
			KeepInventoryTracker.consumeDecision(player.getUUID());
			return;
		}

		boolean pvp = isPvpDeath(player, damageSource, level);
		boolean keepItems = pvp ? !config.dropItemsOnPvp : config.keepItemsOnNaturalDeath;
		boolean keepXp = pvp ? !config.dropXpOnPvp : config.keepXpOnNaturalDeath;

		KeepInventoryTracker.setDecision(player.getUUID(), new Decision(keepItems, keepXp));

		if (config.debugLogging) {
			PvpDeath.LOGGER.info("[pvpdeath] {} died ({}): pvp={}, keepItems={}, keepXp={}",
					player.getScoreboardName(), damageSource.getMsgId(), pvp, keepItems, keepXp);
		}

		// Keeping everything: skip loot, equipment and XP orbs in one go.
		if (keepItems && keepXp) {
			ci.cancel();
		}
	}

	/**
	 * A death counts as PvP when another player is the direct source, or when the victim
	 * was punched by another player inside the configured window.
	 */
	private static boolean isPvpDeath(Player victim, DamageSource damageSource, ServerLevel level) {
		Entity attacker = damageSource.getEntity();
		if (attacker instanceof Player direct && direct != victim) {
			return true;
		}

		PvpDeathConfig config = PvpDeathConfig.get();
		return config.recentPlayerHitCountsAsPvp
				&& KeepInventoryTracker.wasRecentlyHitByPlayer(
						victim.getUUID(), level.getGameTime(), config.recentHitWindowTicks());
	}

	/**
	 * XP orbs. Cancelled when the decision says the victim keeps their experience.
	 * Only reached when items and XP are not both being kept, since the combined case is
	 * cancelled in {@link #pvpdeath$decideWhatToKeep}.
	 */
	@Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
	private void pvpdeath$maybeKeepExperience(ServerLevel level, Entity killer, CallbackInfo ci) {
		if (!((Object) this instanceof Player player)) {
			return;
		}
		Decision decision = KeepInventoryTracker.peekDecision(player.getUUID());
		if (decision != null && decision.keepXp()) {
			if (PvpDeathConfig.get().debugLogging) {
				PvpDeath.LOGGER.info("[pvpdeath] {} kept XP (no orbs dropped)", player.getScoreboardName());
			}
			ci.cancel();
		}
	}
}
