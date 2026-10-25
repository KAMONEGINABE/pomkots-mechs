package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public record WeaponAIProfile(float minRange, float maxRange, boolean burstFire, int burstDuration, int minFireDelay, int maxFireDelay) {
   public static WeaponAIProfile PROF_DEFAULT = new WeaponAIProfile(5.0F, 150.0F, false, 0, 10, 40);

   public static WeaponAIProfile getProfile(BasePartsItem.WeaponCategory category, MechAutoController.CombatStyle style, MechAutoController.PilotRank rank) {
      float baseDelayModifier = 1.0F;
      switch (rank) {
         case NOVICE:
            baseDelayModifier = 2.5F;
            break;
         case INTERMEDIATE:
            baseDelayModifier = 2.0F;
            break;
         case ADVANCED:
            baseDelayModifier = 1.5F;
      }

      float missileDelayModifier = 1.0F;
      if (style == MechAutoController.CombatStyle.LONG_RANGE) {
         missileDelayModifier = 0.5F;
      }
      return switch (category) {
         case RIFLE -> new WeaponAIProfile(5.0F, 150.0F, false, 0, (int)(10.0F * baseDelayModifier), (int)(40.0F * baseDelayModifier));
         case MACHINE_GUN -> new WeaponAIProfile(0.0F, 80.0F, true, 60, (int)(20.0F * baseDelayModifier), (int)(60.0F * baseDelayModifier));
         case SHOT_GUN -> new WeaponAIProfile(0.0F, 40.0F, false, 0, (int)(10.0F * baseDelayModifier), (int)(40.0F * baseDelayModifier));
         case GRENADE -> new WeaponAIProfile(40.0F, 200.0F, false, 0, (int)(100.0F * baseDelayModifier), (int)(120.0F * baseDelayModifier));
         case MELEE -> new WeaponAIProfile(0.0F, 40.0F, false, 0, (int)(10.0F * baseDelayModifier), (int)(30.0F * baseDelayModifier));
         case MISSILE -> new WeaponAIProfile(
            20.0F, 200.0F, false, 0, (int)(100.0F * baseDelayModifier * missileDelayModifier), (int)(150.0F * baseDelayModifier * missileDelayModifier)
         );
         default -> PROF_DEFAULT;
      };
   }
}
