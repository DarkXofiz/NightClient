package dev.nightclient.mixin;

import dev.nightclient.NightClient;
import dev.nightclient.module.modules.combat.Fps31;
import dev.nightclient.util.CombatUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class Mixin2 {
   @Inject(
      method = {"interactBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nightclient$forceRocketUse(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
      Fps31 elyFix = NightClient.get(Fps31.class);
      if (elyFix != null && elyFix.redirects(player.getStackInHand(hand), hitResult.getBlockPos())) {
         cir.setReturnValue(ActionResult.PASS);
      }
   }

   @Inject(
      method = {"attackEntity"},
      at = {@At("HEAD")}
   )
   private void nightclient$noteAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
      CombatUtils.noteInteracted(target);
   }
}
