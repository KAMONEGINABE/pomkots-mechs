package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TransportToArenaProcessor extends AbstractArenaMatchStateProcessor {
   private static final long TIMEOUT = 3500L;

   @Override
   public void onEnter(ArenaMatchContext context) {
      this.startElevator(context.getLevel(), context.match().getGatePosA());
      this.startElevator(context.getLevel(), context.match().getGatePosB());
   }

   private boolean startElevator(Level level, BlockPos gatePos) {
      if (level.getBlockEntity(gatePos) instanceof ArenaGateBlockEntity gateEntity) {
         ElevatorEntity elev = gateEntity.getElevator();
         if (elev == null) {
            return false;
         }

         elev.beginMove();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick(ArenaMatchContext context) {
      long elapsed = context.elapsedMillis();
      if (elapsed >= 3500L) {
         ArenaMatchProcessor.changeState(context, ArenaMatchState.OPENING);
      }
   }

   @Override
   public void onExit(ArenaMatchContext context) {
   }

   @Override
   public void onCancel(ArenaMatchContext context) {
   }
}
