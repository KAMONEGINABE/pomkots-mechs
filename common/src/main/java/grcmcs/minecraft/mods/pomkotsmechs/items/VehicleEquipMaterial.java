package grcmcs.minecraft.mods.pomkotsmechs.items;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class VehicleEquipMaterial implements Tier {
   public static final VehicleEquipMaterial INSTANCE = new VehicleEquipMaterial();

   public int getUses() {
      return 0;
   }

   public float getSpeed() {
      return 0.0F;
   }

   public float getAttackDamageBonus() {
      return 0.0F;
   }

   public int getLevel() {
      return 0;
   }

   public int getEnchantmentValue() {
      return 0;
   }

   public Ingredient getRepairIngredient() {
      return null;
   }
}
