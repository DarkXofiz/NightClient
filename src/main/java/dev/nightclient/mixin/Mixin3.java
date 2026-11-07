package dev.nightclient.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({MinecraftClient.class})
public interface Mixin3 {
   @Accessor("mouse")
   Mouse nightclient$getMouse();

   @Accessor("itemUseCooldown")
   int nightclient$getItemUseCooldown();

   @Accessor("itemUseCooldown")
   void nightclient$setItemUseCooldown(int var1);
}
