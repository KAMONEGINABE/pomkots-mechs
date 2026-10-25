package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CircuitRewardRoller {
   private static final float CIRCUIT_DROP_CHANCE = 0.25F;
   public static final CircuitRewardRoller.WeightedEntry<CircuitPrefix>[] PREFIX_WEIGHTS = new CircuitRewardRoller.WeightedEntry[]{
      new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.OFFENCE, 20),
      new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.DEFENCE, 20),
      new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.ENERGY, 20),
      new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.MOBILITY, 20),
      new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.BALANCED, 10)
   };
   public static final CircuitRewardRoller.WeightedEntry<CircuitPrefix>[] PREFIX_WEIGHTS_ARENA = new CircuitRewardRoller.WeightedEntry[]{
      new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.OFFENCE, 50), new CircuitRewardRoller.WeightedEntry<>(CircuitPrefix.MOBILITY, 50)
   };
   public static final CircuitRewardRoller.WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS = new CircuitRewardRoller.WeightedEntry[]{
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.COMMON, 60),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.RARE, 30),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.EPIC, 10)
   };
   public static final CircuitRewardRoller.WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS_ARENA_SS_S = new CircuitRewardRoller.WeightedEntry[]{
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.COMMON, 0),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.RARE, 40),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.EPIC, 60)
   };
   public static final CircuitRewardRoller.WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS_ARENA_A_B = new CircuitRewardRoller.WeightedEntry[]{
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.COMMON, 50),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.RARE, 40),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.EPIC, 10)
   };
   public static final CircuitRewardRoller.WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS_ARENA_C_D = new CircuitRewardRoller.WeightedEntry[]{
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.COMMON, 90),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.RARE, 9),
      new CircuitRewardRoller.WeightedEntry<>(CircuitRarity.EPIC, 1)
   };

   private CircuitRewardRoller() {
   }

   public static ItemStack roll(RandomSource random) {
      return roll(PREFIX_WEIGHTS, RARITY_WEIGHTS, 0.25F, random);
   }

   public static ItemStack roll(
      CircuitRewardRoller.WeightedEntry<CircuitPrefix>[] weightPrefix,
      CircuitRewardRoller.WeightedEntry<CircuitRarity>[] rarityPrefix,
      float dropChance,
      RandomSource random
   ) {
      if (random.nextFloat() >= dropChance) {
         return ItemStack.EMPTY;
      }

      CircuitPrefix prefix = rollWeighted(random, weightPrefix);
      CircuitRarity rarity = rollWeighted(random, rarityPrefix);
      ItemStack stack = new ItemStack((ItemLike)PomkotsMechs.CIRCUIT_BASE.get());
      CircuitItemStackHelper.setUnidentified(stack, prefix, rarity);
      return stack;
   }

   private static <T> T rollWeighted(RandomSource random, CircuitRewardRoller.WeightedEntry<T>[] entries) {
      int totalWeight = 0;

      for (CircuitRewardRoller.WeightedEntry<T> entry : entries) {
         totalWeight += entry.weight();
      }

      int roll = random.nextInt(totalWeight);
      int current = 0;

      for (CircuitRewardRoller.WeightedEntry<T> entry : entries) {
         current += entry.weight();
         if (roll < current) {
            return entry.value();
         }
      }

      return entries[entries.length - 1].value();
   }

   public record WeightedEntry<T>(T value, int weight) {
   }
}
