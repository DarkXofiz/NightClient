package dev.nightclient.module.modules.render;

import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import dev.nightclient.module.modules.combat.AimAssistModule;
import dev.nightclient.module.modules.combat.AutoFirework;
import dev.nightclient.module.setting.BooleanSetting;
import dev.nightclient.module.setting.ModeSetting;
import dev.nightclient.module.setting.NumberSetting;
import dev.nightclient.module.setting.Setting;
import dev.nightclient.NightClient;
import dev.nightclient.render.EspRenderer;
import dev.nightclient.util.CombatUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/** Target target ESP: draws an animated effect around the entity Aim Assist / Auto Firework / your last hit is on. */
public final class TargetEsp extends Module {
   private final ModeSetting<EspRenderer.Style> style = new ModeSetting<>("Style", EspRenderer.Style.Helix);
   private final ModeSetting<EspRenderer.Colors> colors = new ModeSetting<>("Color", EspRenderer.Colors.Accent)
      .describe("Accent = GUI Colors gradient, Health = red to green, Rainbow");
   private final NumberSetting opacity = new NumberSetting("Opacity", 85.0, 10.0, 100.0, 1.0);
   private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.2, 3.0, 0.1);
   private final NumberSetting size = new NumberSetting("Size", 1.0, 0.5, 2.0, 0.05);
   private final BooleanSetting walls = new BooleanSetting("Through Walls", true);
   private final BooleanSetting fade = new BooleanSetting("Fade", true).describe("Fade in and out when the target changes");
   private final BooleanSetting hitFlash = new BooleanSetting("Hit Flash", true).describe("Brightens when the target takes damage");
   private final BooleanSetting lastHit = new BooleanSetting("Last Hit", true).describe("Fall back to the entity you hit last");
   private Entity shown;
   private float alpha;
   private long lastFrame = System.nanoTime();

   public TargetEsp() {
      super("Target ESP", "Target ESP around your current target", Category.RENDER);
      this.addSettings(new Setting[]{this.style, this.colors, this.opacity, this.speed, this.size, this.walls, this.fade, this.hitFlash, this.lastHit});
   }

   @Override
   public void onDisable() {
      this.shown = null;
      this.alpha = 0.0F;
   }

   private Entity pick() {
      for (Module module : NightClient.modules()) {
         if (module instanceof AimAssistModule assist && module.isEnabled() && assist.currentTarget() != null) {
            return assist.currentTarget();
         }
      }

      AutoFirework firework = NightClient.get(AutoFirework.class);
      if (firework != null && firework.focus() != null) {
         return firework.focus();
      }

      if (this.lastHit.get()) {
         Entity last = CombatUtils.getLastInteracted();
         if (last != null && last.isAlive() && !last.isRemoved() && this.getMC().player != null && last.distanceTo(this.getMC().player) < 12.0F) {
            return last;
         }
      }

      return null;
   }

   /** Called every frame from WorldRenderEvents.LAST. */
   public void render(MatrixStack stack, float delta) {
      if (this.getMC().player == null || this.getMC().world == null || stack == null) {
         return;
      }

      long now = System.nanoTime();
      float dt = Math.min((float)(now - this.lastFrame) / 1.0E9F, 0.1F);
      this.lastFrame = now;
      Entity picked = this.pick();
      if (picked != null) {
         this.shown = picked;
      }

      float goal = picked != null ? 1.0F : 0.0F;
      if (this.fade.get()) {
         this.alpha += (goal - this.alpha) * (1.0F - (float)Math.exp((double)(-10.0F * dt)));
         if (picked == null && this.alpha < 0.02F) {
            this.shown = null;
         }
      } else {
         this.alpha = goal;
         if (picked == null) {
            this.shown = null;
         }
      }

      Entity target = this.shown;
      if (target == null || target.isRemoved() || this.alpha < 0.02F) {
         return;
      }

      float flash = 0.0F;
      if (this.hitFlash.get() && target instanceof LivingEntity living && living.hurtTime > 0) {
         flash = MathHelper.clamp((float)living.hurtTime / 10.0F, 0.0F, 1.0F);
      }

      float a = MathHelper.clamp(this.opacity.getFloat() / 100.0F * this.alpha * (1.0F + flash * 0.6F), 0.0F, 1.0F);
      float health = EspRenderer.health(target);
      EspRenderer.Colors mode = this.colors.getValue();
      float flashF = flash;
      EspRenderer.Palette palette = count -> {
         int base;
         if (mode == EspRenderer.Colors.Health) {
            base = dev.nightclient.render.Theme.mix(0xFFFF3A3A, 0xFF3AFF6A, health);
         } else if (mode == EspRenderer.Colors.Rainbow) {
            base = java.awt.Color.HSBtoRGB((float)((System.currentTimeMillis() / 20L + (long)count) % 360L) / 360.0F, 0.65F, 1.0F);
         } else {
            base = dev.nightclient.render.Theme.accent((float)(count % 360) / 360.0F);
         }

         return flashF > 0.0F ? dev.nightclient.render.Theme.mix(base, 0xFFFFFFFF, flashF * 0.6F) : base;
      };
      EspRenderer.Params params = new EspRenderer.Params(a, this.speed.getFloat(), this.size.getFloat(), this.walls.get(), palette);
      switch (this.style.getValue()) {
         case Helix -> EspRenderer.renderHelix(stack, target, delta, params);
         case Orbit -> EspRenderer.renderOrbit(stack, target, delta, params);
         case Halo -> EspRenderer.renderHalo(stack, target, delta, params);
         case Pulse -> EspRenderer.renderPulse(stack, target, delta, params);
         case Cage -> EspRenderer.renderCage(stack, target, delta, params);
      }
   }

   public static enum Style {
      Helix,
      Orbit,
      Halo,
      Pulse,
      Cage;
   }

   public static enum Colors {
      Accent,
      Health,
      Rainbow;
   }
}
