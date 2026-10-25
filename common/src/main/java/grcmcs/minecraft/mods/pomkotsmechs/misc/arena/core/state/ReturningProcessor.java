package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlock;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class ReturningProcessor extends AbstractArenaMatchStateProcessor {
   private static final long TIMEOUT = 1000L;

   @Override
   public void onEnter(ArenaMatchContext context) {
      ArenaFighterData challengerInfo = context.arena().getFighter(context.match().getChallengerId());
      ArenaFighterData opponentInfo = context.arena().getFighter(context.match().getOpponentId());
      this.returnFighter(challengerInfo, context.getChallengerEntity(), context.getChallengerMechEntity(), context.match().getGatePosA(), context.getLevel());
      this.returnFighter(opponentInfo, context.getOpponentEntity(), context.getOpponentMechEntity(), context.match().getGatePosB(), context.getLevel());
   }

   private void returnFighter(ArenaFighterData info, Entity pilotEntity, Entity mechEntity, BlockPos pos, ServerLevel level) {
      if (info.getType() == ArenaFighterData.FighterType.NPC) {
         if (pilotEntity instanceof MechPilotEntity) {
            pilotEntity.m_146870_();
         }

         if (mechEntity instanceof Pmvc01Entity) {
            mechEntity.m_146870_();
         }
      } else {
         float yaw = this.getYaw(level, pos);
         pos = pos.m_6630_(2);
         if (pilotEntity instanceof Player player) {
            player.m_21195_(MobEffects.f_19606_);
         }

         if (mechEntity instanceof Pmvc01Entity mech && mechEntity.m_6084_()) {
            if (mech.getDrivingPassenger() != null && mech.getDrivingPassenger().equals(pilotEntity)) {
               this.setYaw(mech, yaw);
               this.setYaw(pilotEntity, yaw);
               mech.m_7678_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), yaw, 0.0F);
               return;
            }

            mech.m_6021_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
            this.setYaw(mech, yaw);
         }

         if (pilotEntity != null && pilotEntity.m_6084_()) {
            pilotEntity.m_6021_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
            this.setYaw(pilotEntity, yaw);
         }
      }
   }

   private void setYaw(Entity ent, float yaw) {
      ent.m_146922_(yaw);
      ent.m_5618_(yaw);
      ent.m_5616_(yaw);
   }

   @Override
   public void tick(ArenaMatchContext context) {
      long elapsed = context.elapsedMillis();
      if (elapsed >= 1000L) {
         ArenaMatchProcessor.changeState(context, ArenaMatchState.FINISHED);
      }
   }

   private float getYaw(ServerLevel level, BlockPos pos) {
      BlockState state = level.m_8055_(pos);
      Direction facing = (Direction)state.m_61143_(ArenaGateBlock.FACING);
      Direction exitDirection = facing.m_122424_();
      return this.directionToYaw(exitDirection);
   }

   public float directionToYaw(Direction direction) {
      return switch (direction) {
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case NORTH -> 180.0F;
         case EAST -> -90.0F;
         default -> 0.0F;
      };
   }

   @Override
   public void onExit(ArenaMatchContext context) {
   }

   @Override
   public void onCancel(ArenaMatchContext context) {
   }
}
