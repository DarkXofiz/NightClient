package dev.nightclient.sound;

import dev.nightclient.NightClient;
import dev.nightclient.module.modules.client.SoundsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * NightClient sound set (enable / disable / click-gui sounds).
 * Played as plain UI sounds, so no registry entry is needed and it works on every server.
 */
public final class Sounds {
   public static final Identifier ENABLE = new Identifier("nightclient", "enable");
   public static final Identifier DISABLE = new Identifier("nightclient", "disable");
   public static final Identifier KEYPRESS = new Identifier("nightclient", "keypress");
   public static final Identifier KEYRELEASE = new Identifier("nightclient", "keyrelease");
   public static final Identifier BOOLEAN = new Identifier("nightclient", "boolean");
   public static final Identifier SCROLL = new Identifier("nightclient", "scroll");
   public static final Identifier SWIPE_IN = new Identifier("nightclient", "swipein");
   public static final Identifier SWIPE_OUT = new Identifier("nightclient", "swipeout");

   private static long lastScroll;
   private static long lastSlider;

   private Sounds() {
   }

   private static SoundsModule config() {
      return NightClient.get(SoundsModule.class);
   }

   private static void play(Identifier id, float volume) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc == null || mc.getSoundManager() == null || volume <= 0.0F) {
         return;
      }

      mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(id), 1.0F, volume));
   }

   public static void toggle(boolean enabled) {
      SoundsModule cfg = config();
      if (cfg != null && cfg.isEnabled() && cfg.moduleSounds()) {
         play(enabled ? ENABLE : DISABLE, cfg.volume());
      }
   }

   private static void gui(Identifier id) {
      SoundsModule cfg = config();
      if (cfg != null && cfg.isEnabled() && cfg.guiSounds()) {
         play(id, cfg.volume());
      }
   }

   public static void bool() {
      gui(BOOLEAN);
   }

   public static void swipeIn() {
      gui(SWIPE_IN);
   }

   public static void swipeOut() {
      gui(SWIPE_OUT);
   }

   public static void keyPress() {
      gui(KEYPRESS);
   }

   public static void keyRelease() {
      gui(KEYRELEASE);
   }

   /** Hover tick, rate limited like NightClient (one every 50 ms). */
   public static void scroll() {
      long now = System.currentTimeMillis();
      if (now - lastScroll >= 50L) {
         lastScroll = now;
         gui(SCROLL);
      }
   }

   public static void slider() {
      long now = System.currentTimeMillis();
      if (now - lastSlider >= 50L) {
         lastSlider = now;
         gui(SCROLL);
      }
   }
}
