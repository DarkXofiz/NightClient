package dev.nightclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/** Arka planda çalışan FPS iyileştirmesi: menüde/GUI'de görünmez, bir kez uygulanır. */
public final class PerfBoost {
   private static boolean done;

   private PerfBoost() {
   }

   public static void apply(MinecraftClient mc) {
      if (done || mc == null || mc.options == null || mc.worldRenderer == null) {
         return;
      }

      done = true;
      GameOptions o = mc.options;
      try {
         o.getGraphicsMode().setValue(GraphicsMode.FAST);
         o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
         o.getParticles().setValue(ParticlesMode.MINIMAL);
         o.getEntityShadows().setValue(false);
         o.getAo().setValue(false);
         o.getBiomeBlendRadius().setValue(0);
         o.getEntityDistanceScaling().setValue(0.75);
         o.getViewDistance().setValue(Math.min(o.getViewDistance().getValue(), 10));
         o.getSimulationDistance().setValue(Math.min(o.getSimulationDistance().getValue(), 8));
      } catch (Throwable ignored) {
      }
   }
}
