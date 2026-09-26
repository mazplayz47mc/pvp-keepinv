package com.pvpdeath.mixin;

import java.util.UUID;

import com.pvpdeath.KeepInventoryTracker;
import com.pvpdeath.KeepInventoryTracker.Decision;
import com.pvpdeath.PvpDeath;
import com.pvpdeath.PvpDeathConfig;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla transfers the inventory, XP and score back to the respawned player in exactly
 * one place: {@code ServerPlayer.restoreFrom}, and only when {@code keepInventory} or a
 * spectator respawn says so. The drops were suppressed at death instead, so this mixin
 * performs the matching transfer - either fully or only for the half of the state the
 * death decision kept.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRespawnMixin {
	@Inject(method = "restoreFrom", at = @At("HEAD"))
	private void pvpdeath$restoreKeptState(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
		// Consume the decision even if we do not act on it, so it can never leak between deaths.
		UUID id = oldPlayer.getUUID();
		Decision decision = KeepInventoryTracker.consumeDecision(id);
		if (decision == null) {
			return;
		}

		ServerPlayer self = (ServerPlayer) (Object) this;

		// Vanilla already transfers inventory/xp/score in these cases; don't double it.
		if (keepEverything || oldPlayer.isSpectator()
				|| self.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}

		if (decision.keepItems() && decision.keepXp()) {
			// The vanilla inventory/xp/score transfer restoreFrom() would otherwise skip.
			((ServerPlayerAccessor) (Object) this).pvpdeath$transferInventoryXpAndScore(oldPlayer);
		} else if (decision.keepItems()) {
			self.getInventory().replaceWith(oldPlayer.getInventory());
			self.setScore(oldPlayer.getScore());
		} else if (decision.keepXp()) {
			self.experienceLevel = oldPlayer.experienceLevel;
			self.totalExperience = oldPlayer.totalExperience;
			self.experienceProgress = oldPlayer.experienceProgress;
			self.setScore(oldPlayer.getScore());
		}

		if (PvpDeathConfig.get().debugLogging) {
			PvpDeath.LOGGER.info("[pvpdeath] {} respawned keeping items={}, xp={}",
					self.getScoreboardName(), decision.keepItems(), decision.keepXp());
		}
	}
}
