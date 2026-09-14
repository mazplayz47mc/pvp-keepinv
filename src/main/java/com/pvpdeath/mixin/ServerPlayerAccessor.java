package com.pvpdeath.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
	@Invoker("transferInventoryXpAndScore")
	void pvpdeath$transferInventoryXpAndScore(Player oldPlayer);
}