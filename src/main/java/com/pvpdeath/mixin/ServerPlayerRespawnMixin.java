package com.pvpdeath.mixin;

import java.util.UUID;

import com.pvpdeath.KeepInventoryTracker;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRespawnMixin {
	@Inject(method = "restoreFrom", at = @At("HEAD"))
	private void pvpdeath$keepItemsOnRespawn(ServerPlayer oldPlayer, boolean keepEverything, CallbackInfo ci) {
		// Take the record whether we act on it or not, so it can never leak between deaths.
		UUID id = oldPlayer.getUUID();
		if (!KeepInventoryTracker.KEEP_ON_RESPAWN.remove(id)) {
			return;
		}

		// Vanilla already transfers inventory/xp/score in these cases; don't double it.
		if (keepEverything || oldPlayer.isSpectator()
				|| ((ServerPlayer) (Object) this).level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}

		// Natural death with drops cancelled: perform the vanilla inventory/xp/score
		// transfer that restoreFrom() would otherwise skip.
		((ServerPlayerAccessor) (Object) this).pvpdeath$transferInventoryXpAndScore(oldPlayer);
	}
}