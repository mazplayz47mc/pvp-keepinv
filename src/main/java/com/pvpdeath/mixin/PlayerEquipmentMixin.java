package com.pvpdeath.mixin;

import com.pvpdeath.KeepInventoryTracker;
import com.pvpdeath.KeepInventoryTracker.Decision;
import com.pvpdeath.PvpDeath;
import com.pvpdeath.PvpDeathConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Player inventory drop. {@code Player.dropEquipment} is where the actual inventory is
 * emptied onto the ground, so this is the single hook that decides whether the items stay.
 */
@Mixin(Player.class)
public abstract class PlayerEquipmentMixin {
	@Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
	private void pvpdeath$maybeKeepInventory(ServerLevel level, CallbackInfo ci) {
		Player self = (Player) (Object) this;
		Decision decision = KeepInventoryTracker.peekDecision(self.getUUID());
		if (decision == null || !decision.keepItems()) {
			return;
		}

		if (PvpDeathConfig.get().debugLogging) {
			PvpDeath.LOGGER.info("[pvpdeath] {} kept their inventory", self.getScoreboardName());
		}
		ci.cancel();
	}
}
