package dev.nightclient.module.modules.combat;

import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;

public final class Fps31 extends Module {
   public Fps31() {
      super("ElyFix", "Fireworks boost through walls and cobwebs", Category.COMBAT);
      this.setEnabled(true);
   }

   public boolean redirects(ItemStack stack, BlockPos target) {
      if (this.isEnabled() && stack.isOf(Items.FIREWORK_ROCKET)) {
         ClientWorld world = this.mc.world;
         return world == null || world.getBlockEntity(target) == null;
      } else {
         return false;
      }
   }

   public boolean ignoresCobweb(Entity entity) {
      if (!this.isEnabled() || this.mc == null || entity != this.mc.player || !(entity instanceof PlayerEntity player)) {
         return false;
      }

      if (player.isFallFlying()) {
         return true;
      }

      // Elytra takılıysa: havadayken ya da elinde roket varken ağ yavaşlatması/takılması uygulanmasın,
      // böylece ağın içinden süzülmeye veya roketle çıkmaya başlanabilir.
      if (!player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
         return false;
      }

      return !player.isOnGround()
         || player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)
         || player.getOffHandStack().isOf(Items.FIREWORK_ROCKET);
   }
}
