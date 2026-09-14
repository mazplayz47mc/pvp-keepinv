package com.pvpdeath.mixin;

import com.pvpdeath.KeepInventoryTracker;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerDeathMixin {
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
	private void pvpdeath$keepItemsOnNaturalDeath(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
		// Only affect players. Mobs keep their normal vanilla death loot.
		if (!((Object) this instanceof Player player)) {
			return;
		}

		// PvP death: the true attacker is another player -> keep vanilla drop behaviour.
		Entity attacker = damageSource.getEntity();
		if (attacker instanceof Player pvpKiller && pvpKiller != player) {
			return;
		}

		// Natural death (fall, drowning, fire, mobs, void, ...): keep all items (and XP).
		ci.cancel();
		KeepInventoryTracker.KEEP_ON_RESPAWN.add(player.getUUID());
	}
}