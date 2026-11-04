package dev.nightclient.gui;

import dev.nightclient.NightClient;
import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import dev.nightclient.module.modules.client.Fps15;
import dev.nightclient.render.Animation;
import dev.nightclient.render.Render2D;
import dev.nightclient.render.Theme;
import dev.nightclient.render.Toasts;
import dev.nightclient.util.ConfigManager;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * ThunderHack tarzı ClickGUI: sol kategori çubuğu, üstte arama, sağda iki sütunlu modül kartları.
 * Sol tık: modülü aç/kapa. Sağ tık: ayarları aç/kapa. Çubuğun üstünden pencere sürüklenir.
 */
public final class ClickGuiScreen extends Screen {
   private static final int SIDEBAR = 104;
   private static final int PAD = 8;
   private static final int SEARCH_H = 18;
   private static final int GAP = 6;
   private static final int WIN_W = 520;
   private static final int WIN_H = 330;

   private final Map<Category, List<ModuleButton>> cards = new EnumMap<>(Category.class);
   private final Map<Category, Animation> tabAnim = new EnumMap<>(Category.class);
   private final Animation open = new Animation(0.0F);
   private final Animation scrollAnim = new Animation(0.0F);
   private Category selected = Category.COMBAT;
   private String search = "";
   private boolean searchFocused;
   private float scrollTarget;
   private float contentHeight;
   private float winX = -1.0F;
   private float winY = -1.0F;
   private boolean draggingWindow;
   private float dragDX;
   private float dragDY;
   private ModuleButton pressed;

   public ClickGuiScreen() {
      super(Text.literal("NightClient"));
      for (Category category : Category.values()) {
         List<ModuleButton> list = new ArrayList<>();
         for (Module module : NightClient.byCategory(category)) {
            list.add(new ModuleButton(module));
         }

         this.cards.put(category, list);
         this.tabAnim.put(category, new Animation(0.0F));
      }
   }

   protected void init() {
      this.open.set(0.0F);
      if (this.winX < 0.0F) {
         this.winX = ((float)this.width - (float)this.winW()) / 2.0F;
         this.winY = ((float)this.height - (float)this.winH()) / 2.0F;
      }

      for (List<ModuleButton> list : this.cards.values()) {
         for (ModuleButton card : list) {
            card.resetAnimations();
         }
      }
   }

   private int winW() {
      return Math.min(WIN_W, this.width - 16);
   }

   private int winH() {
      return Math.min(WIN_H, this.height - 16);
   }

   private float contentX() {
      return this.winX + (float)SIDEBAR + (float)PAD;
   }

   private float contentW() {
      return (float)(this.winW() - SIDEBAR - PAD * 2);
   }

   private float listTop() {
      return this.winY + (float)PAD + (float)SEARCH_H + (float)GAP;
   }

   private float listBottom() {
      return this.winY + (float)this.winH() - (float)PAD;
   }

   private List<ModuleButton> visibleCards() {
      if (this.search.isEmpty()) {
         return this.cards.get(this.selected);
      }

      String needle = this.search.toLowerCase(Locale.ROOT);
      List<ModuleButton> out = new ArrayList<>();
      for (List<ModuleButton> list : this.cards.values()) {
         for (ModuleButton card : list) {
            if (card.module().getName().toLowerCase(Locale.ROOT).contains(needle)) {
               out.add(card);
            }
         }
      }

      return out;
   }

   /** İki sütuna yerleştirir (en kısa sütuna ekler), içerik yüksekliğini hesaplar. */
   private void layoutCards(float scroll) {
      List<ModuleButton> list = this.visibleCards();
      float colW = (this.contentW() - (float)GAP) / 2.0F;
      float[] colY = new float[]{0.0F, 0.0F};
      for (ModuleButton card : list) {
         int col = colY[0] <= colY[1] ? 0 : 1;
         card.setBounds(this.contentX() + (float)col * (colW + (float)GAP), this.listTop() + colY[col] - scroll, colW);
         colY[col] += card.height() + (float)GAP;
      }

      this.contentHeight = Math.max(colY[0], colY[1]);
   }

   @Override
   public void renderBackground(DrawContext context) {
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      float alpha = this.open.animate(1.0F, 12.0F);
      context.fill(0, 0, this.width, this.height, ((int)(alpha * 130.0F) & 0xFF) << 24);
      float visible = this.listBottom() - this.listTop();
      float maxScroll = Math.max(0.0F, this.contentHeight - visible);
      this.scrollTarget = Math.max(0.0F, Math.min(maxScroll, this.scrollTarget));
      float scroll = this.scrollAnim.animate(this.scrollTarget, 14.0F);

      float x = this.winX;
      float y = this.winY + (1.0F - alpha) * 10.0F;
      float w = (float)this.winW();
      float h = (float)this.winH();
      // gölge + pencere
      Render2D.roundedRect(context, x - 2.0F, y - 2.0F, w + 4.0F, h + 4.0F, 11.0F, Theme.scaleAlpha(0xFF000000, alpha * 0.35F));
      Render2D.roundedRect(context, x, y, w, h, 9.0F, Theme.scaleAlpha(0xFF101015, alpha));
      Render2D.roundedOutline(context, x, y, w, h, 9.0F, 1.0F, Theme.scaleAlpha(Theme.withAlpha(Theme.ACCENT, 90), alpha));

      this.renderSidebar(context, mouseX, mouseY, alpha, x, y, h);
      this.renderSearch(context, alpha);

      this.layoutCards(scroll);
      context.enableScissor((int)this.contentX() - 1, (int)this.listTop(), (int)(this.contentX() + this.contentW() + 1.0F), (int)this.listBottom());
      boolean inList = mouseY >= (int)this.listTop() && mouseY <= (int)this.listBottom();
      for (ModuleButton card : this.visibleCards()) {
         if (card.y() + card.height() >= this.listTop() && card.y() <= this.listBottom()) {
            card.render(context, mouseX, inList ? mouseY : -9999, delta, alpha);
         }
      }

      context.disableScissor();
      if (this.visibleCards().isEmpty()) {
         Render2D.textCentered(context, "modül bulunamadı", this.contentX() + this.contentW() / 2.0F, this.listTop() + 12.0F, Theme.scaleAlpha(0xFF6A6A77, alpha));
      }

      if (maxScroll > 0.0F) {
         float trackH = visible;
         float thumbH = Math.max(18.0F, trackH * (visible / this.contentHeight));
         float thumbY = this.listTop() + (trackH - thumbH) * (scroll / maxScroll);
         Render2D.roundedRect(context, x + w - 5.0F, thumbY, 2.0F, thumbH, 1.0F, Theme.scaleAlpha(Theme.ACCENT_DIM, alpha));
      }

      super.render(context, mouseX, mouseY, delta);
      Toasts.render(context);
   }

   private void renderSidebar(DrawContext context, int mouseX, int mouseY, float alpha, float x, float y, float h) {
      Render2D.roundedRect(context, x, y, (float)SIDEBAR, h, 9.0F, Theme.scaleAlpha(0xFF0B0B0F, alpha));
      context.fill((int)(x + (float)SIDEBAR - 9.0F), (int)y + 1, (int)(x + (float)SIDEBAR), (int)(y + h) - 1, Theme.scaleAlpha(0xFF0B0B0F, alpha));
      context.fill((int)(x + (float)SIDEBAR - 1.0F), (int)y + 8, (int)(x + (float)SIDEBAR), (int)(y + h) - 8, Theme.scaleAlpha(0xFF22222B, alpha));

      Render2D.text(context, "Night", x + 14.0F, y + 14.0F, Theme.scaleAlpha(Theme.ACCENT, alpha), true);
      Render2D.text(context, "Client", x + 14.0F + (float)Render2D.textWidth("Night"), y + 14.0F, Theme.scaleAlpha(0xFFE8E8F0, alpha), true);
      Render2D.text(context, "v" + NightClient.VERSION, x + 14.0F, y + 26.0F, Theme.scaleAlpha(0xFF5A5A66, alpha));

      float ty = y + 48.0F;
      for (Category category : Category.values()) {
         boolean sel = this.search.isEmpty() && category == this.selected;
         boolean hov = mouseX >= x + 6.0F && mouseX <= x + (float)SIDEBAR - 6.0F && (float)mouseY >= ty && (float)mouseY < ty + 24.0F;
         float t = this.tabAnim.get(category).animate(sel ? 1.0F : (hov ? 0.45F : 0.0F), 14.0F);
         if (t > 0.01F) {
            Render2D.roundedRect(context, x + 6.0F, ty, (float)SIDEBAR - 12.0F, 24.0F, 6.0F, Theme.scaleAlpha(Theme.withAlpha(Theme.ACCENT, (int)(48.0F * t)), alpha));
         }

         if (sel) {
            Render2D.roundedRect(context, x + 6.0F, ty + 5.0F, 2.0F, 14.0F, 1.0F, Theme.scaleAlpha(Theme.ACCENT, alpha));
         }

         int color = Theme.mix(0xFF8A8A97, Theme.ACCENT, t);
         Render2D.text(context, category.label, x + 18.0F, ty + 8.0F, Theme.scaleAlpha(color, alpha));
         String count = Integer.toString(this.cards.get(category).size());
         Render2D.text(context, count, x + (float)SIDEBAR - 16.0F - (float)Render2D.textWidth(count), ty + 8.0F, Theme.scaleAlpha(0xFF55555F, alpha));
         ty += 28.0F;
      }
   }

   private void renderSearch(DrawContext context, float alpha) {
      float sx = this.contentX();
      float sy = this.winY + (float)PAD;
      float sw = this.contentW();
      Render2D.roundedRect(context, sx, sy, sw, (float)SEARCH_H, 6.0F, Theme.scaleAlpha(0xFF17171E, alpha));
      if (this.searchFocused) {
         Render2D.roundedOutline(context, sx, sy, sw, (float)SEARCH_H, 6.0F, 1.0F, Theme.scaleAlpha(Theme.ACCENT, alpha));
      }

      boolean blink = this.searchFocused && System.currentTimeMillis() / 500L % 2L == 0L;
      String shown = this.search.isEmpty() && !this.searchFocused ? "Ara..." : this.search + (blink ? "_" : "");
      int color = this.search.isEmpty() ? 0xFF5A5A66 : 0xFFE8E8F0;
      Render2D.text(context, Render2D.truncate(shown, (int)sw - 14), sx + 8.0F, sy + 5.0F, Theme.scaleAlpha(color, alpha));
   }

   private boolean inList(double mx, double my) {
      return mx >= (double)this.contentX() && mx <= (double)(this.contentX() + this.contentW()) && my >= (double)this.listTop() && my <= (double)this.listBottom();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      float x = this.winX;
      float y = this.winY;
      // sidebar
      if (mouseX >= (double)x && mouseX <= (double)(x + (float)SIDEBAR) && mouseY >= (double)y && mouseY <= (double)(y + (float)this.winH())) {
         float ty = y + 48.0F;
         for (Category category : Category.values()) {
            if (button == 0 && mouseY >= (double)ty && mouseY < (double)(ty + 24.0F)) {
               this.selected = category;
               this.search = "";
               this.searchFocused = false;
               this.scrollTarget = 0.0F;
               this.scrollAnim.set(0.0F);
               return true;
            }

            ty += 28.0F;
         }

         if (button == 0 && mouseY < (double)(y + 44.0F)) {
            this.draggingWindow = true;
            this.dragDX = (float)mouseX - this.winX;
            this.dragDY = (float)mouseY - this.winY;
         }

         return true;
      }

      // arama kutusu
      float sy = y + (float)PAD;
      boolean onSearch = mouseX >= (double)this.contentX() && mouseX <= (double)(this.contentX() + this.contentW()) && mouseY >= (double)sy && mouseY <= (double)(sy + (float)SEARCH_H);
      if (onSearch && button == 0) {
         this.searchFocused = true;
         return true;
      }

      this.searchFocused = false;
      if (this.inList(mouseX, mouseY)) {
         for (ModuleButton card : this.visibleCards()) {
            if (card.mouseClicked(mouseX, mouseY, button)) {
               this.pressed = card;
               return true;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.draggingWindow && button == 0) {
         this.winX = Math.max(0.0F, Math.min((float)this.width - (float)this.winW(), (float)mouseX - this.dragDX));
         this.winY = Math.max(0.0F, Math.min((float)this.height - (float)this.winH(), (float)mouseY - this.dragDY));
         return true;
      }

      for (ModuleButton card : this.visibleCards()) {
         card.mouseDragged(mouseX, mouseY, button);
      }

      return true;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingWindow = false;
      for (List<ModuleButton> list : this.cards.values()) {
         for (ModuleButton card : list) {
            card.mouseReleased();
         }
      }

      this.pressed = null;
      ConfigManager.save(NightClient.modules());
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.inList(mouseX, mouseY)) {
         this.scrollTarget -= (float)(amount * 24.0);
      }

      return true;
   }

   private boolean anyCapturing() {
      for (ModuleButton card : this.visibleCards()) {
         if (card.capturingInput()) {
            return true;
         }
      }

      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (ModuleButton card : this.visibleCards()) {
         if (card.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      if (this.searchFocused) {
         if (keyCode == 259) {
            if (!this.search.isEmpty()) {
               this.search = this.search.substring(0, this.search.length() - 1);
               this.scrollTarget = 0.0F;
            }

            return true;
         }

         if (keyCode == 256 || keyCode == 257) {
            this.searchFocused = false;
            return true;
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char chr, int modifiers) {
      for (ModuleButton card : this.visibleCards()) {
         if (card.charTyped(chr)) {
            return true;
         }
      }

      if (this.searchFocused && !this.anyCapturing() && chr >= ' ' && this.search.length() < 24) {
         this.search = this.search + chr;
         this.scrollTarget = 0.0F;
         return true;
      }

      return super.charTyped(chr, modifiers);
   }

   public void close() {
      Fps15 module = NightClient.get(Fps15.class);
      if (module != null && module.isEnabled()) {
         module.setEnabled(false);
      } else {
         super.close();
      }
   }

   public void removed() {
      Fps15 module = NightClient.get(Fps15.class);
      if (module != null) {
         module.onScreenClosed();
      }

      ConfigManager.save(NightClient.modules());
   }

   public boolean shouldPause() {
      return false;
   }
}
