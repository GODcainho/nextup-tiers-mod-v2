package com.nextup.tiers.mixin;

import com.nextup.tiers.TierManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Coloca o tier na frente do nick acima da cabeca do jogador. */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void tiers$addPrefix(CallbackInfoReturnable<Text> cir) {
        if ((Object) this instanceof AbstractClientPlayerEntity player) {
            cir.setReturnValue(TierManager.decorateNametag(player.getName().getString(), cir.getReturnValue()));
        }
    }
}
