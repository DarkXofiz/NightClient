package dev.nightclient.mixin;

import dev.nightclient.NightClient;
import dev.nightclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public abstract class Mixin4 {
   @Inject(
      method = {"Lnet/minecraft/client/MinecraftClient;handleInputEvents()V"},
      at = {@At("HEAD")}
   )
   private void nightclient$onBufferUpdate(CallbackInfo ci) {
      NightClient.get();
      ModuleManager.bufferUpdate();
   }

   @Inject(
      method = {"Lnet/minecraft/client/MinecraftClient;render(Z)V"},
      at = {@At("HEAD")}
   )
   private void nightclient$onRenderTick(boolean tick, CallbackInfo ci) {
      NightClient.get();
      ModuleManager.renderTick(MinecraftClient.getInstance().getTickDelta());
   }
}
