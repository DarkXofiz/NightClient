package dev.nightclient.module;

public enum Category {
   COMBAT("Combat"),
   HUD("HUD");

   public final String label;

   private Category(String label) {
      this.label = label;
   }
}
