package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class CancelledProcessor implements IArenaMatchStateProcessor {
   @Override
   public void onEnter(ArenaMatchContext context) {
      if (context.runtime().getBossBar() != null) {
         context.runtime().getBossBar().m_7706_();
      }

      this.processPilots(context.getChallengerEntity(), context);
      this.processPilots(context.getOpponentEntity(), context);
      this.ejectElevatorsPassengers(context);
      context.arena().setActiveMatch(null);
      ArenaSavedData.get(context.server()).m_77762_();
   }

   private void processPilots(Entity pilot, ArenaMatchContext context) {
      if (pilot instanceof ServerPlayer player) {
         String reason = context.runtime().getCancelReason();
         if (reason != null) {
            player.m_213846_(Component.m_237113_(reason));
         }
      } else if (pilot != null) {
         Entity mech = pilot.m_20202_();
         if (mech != null) {
            mech.m_146870_();
         }

         pilot.m_146870_();
      }
   }

   @Override
   public void tick(ArenaMatchContext context) {
   }

   @Override
   public void onExit(ArenaMatchContext context) {
   }

   @Override
   public void onCancel(ArenaMatchContext context) {
   }

   private void ejectElevatorsPassengers(ArenaMatchContext context) {
      this.ejectElevator(context.getLevel(), context.match().getGatePosA());
      this.ejectElevator(context.getLevel(), context.match().getGatePosB());
   }

   private void ejectElevator(Level level, BlockPos pos) {
      if (level.m_7702_(pos) instanceof ArenaGateBlockEntity gateEntity) {
         gateEntity.refreshElevator();
      }
   }
}
