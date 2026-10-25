package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CircuitItemProperties {
   private static final String TAG_CIRCUIT = "Circuit";
   private static final String TAG_PREFIX = "Prefix";
   public static final ResourceLocation CIRCUIT_PREFIX_PROPERTY = new ResourceLocation("pomkotsmechs", "circuit_prefix");

   private CircuitItemProperties() {
   }

   public static void register() {
      registerPrefixProperty("circuit_offence", "pomkotsmechs:offence");
      registerPrefixProperty("circuit_defence", "pomkotsmechs:defence");
      registerPrefixProperty("circuit_energy", "pomkotsmechs:energy");
      registerPrefixProperty("circuit_mobility", "pomkotsmechs:mobility");
      registerPrefixProperty("circuit_utility", "pomkotsmechs:utility");
      registerPrefixProperty("circuit_balanced", "pomkotsmechs:balanced");
   }

   private static void registerPrefixProperty(String propertyName, String targetPrefix) {
      ItemPropertiesRegistry.register(
         (ItemLike)PomkotsMechs.CIRCUIT_BASE.get(),
         new ResourceLocation("pomkotsmechs", propertyName),
         (stack, level, entity, seed) -> targetPrefix.equals(readPrefix(stack)) ? 1.0F : 0.0F
      );
   }

   private static String readPrefix(ItemStack stack) {
      if (stack.hasTag() && stack.getTag() != null) {
         CompoundTag root = stack.getTag();
         if (!root.contains("Circuit", 10)) {
            return "";
         }

         CompoundTag circuitTag = root.getCompound("Circuit");
         return !circuitTag.contains("Prefix", 8) ? "" : circuitTag.getString("Prefix");
      } else {
         return "";
      }
   }
}
