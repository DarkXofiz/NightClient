package dev.nightclient.util;

import dev.nightclient.mixin.Mixin3;
import dev.nightclient.mixin.Mixin5;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.MinecraftClient;

public final class InputUtils {
   private static final MinecraftClient MC = MinecraftClient.getInstance();
   private static final Set<Integer> PRESSED = ConcurrentHashMap.newKeySet();
   private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(task -> {
      Thread thread = new Thread(task, "nightclient-input");
      thread.setDaemon(true);
      return thread;
   });

   private InputUtils() {
   }

   public static boolean isPressed(int button) {
      return PRESSED.contains(button);
   }

   public static void press(int button) {
      MC.execute(() -> {
         Mixin5 mouse = (Mixin5)((Mixin3)MC).nightclient$getMouse();
         mouse.nightclient$onMouseButton(MC.getWindow().getHandle(), button, 1, 0);
         PRESSED.add(button);
      });
   }

   public static void release(int button) {
      MC.execute(() -> {
         Mixin5 mouse = (Mixin5)((Mixin3)MC).nightclient$getMouse();
         mouse.nightclient$onMouseButton(MC.getWindow().getHandle(), button, 0, 0);
         PRESSED.remove(button);
      });
   }

   public static void click(int button, int millis) {
      press(button);
      TIMER.schedule(() -> release(button), (long)millis, TimeUnit.MILLISECONDS);
   }

   public static void click(int button) {
      click(button, 35);
   }
}
