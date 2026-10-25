package grcmcs.minecraft.mods.pomkotsmechs.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class GenericPomkotsMonsterPercistant extends GenericPomkotsMonster {
   protected GenericPomkotsMonsterPercistant(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
   }

   public boolean isPersistenceRequired() {
      return true;
   }

   public void checkDespawn() {
      if (!this.isPersistenceRequired()) {
         super.checkDespawn();
      }
   }
}
