package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaBossBarUtil;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.TitleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WaitForEntryProcessor implements IArenaMatchStateProcessor {
   private static final long TIMEOUT = 180000L;

   @Override
   public void onEnter(ArenaMatchContext context) {
      if (ArenaUtil.initPilotEntities(context)) {
         Entity challengerEntity = context.getChallengerEntity();
         Entity opponentEntity = context.getOpponentEntity();
         if (challengerEntity != null && opponentEntity != null) {
            if (this.setupElevator(context.getLevel(), context.match().getGatePosA(), challengerEntity)) {
               if (this.setupElevator(context.getLevel(), context.match().getGatePosB(), opponentEntity)) {
                  if (challengerEntity instanceof ServerPlayer player) {
                     TitleUtil.showArenaMessage(player, "Move To Gate", "");
                  }

                  if (opponentEntity instanceof ServerPlayer player) {
                     TitleUtil.showArenaMessage(player, "New Challenger", "Check Arena by data pad");

                     for (ItemStack dataPad : PomkotsDatapadItem.getPlayersDataPads(player)) {
                        PomkotsDatapadItem.addArenaRequest(dataPad, challengerEntity.getUUID(), context.match().getMatchId(), context.arena().getArenaId());
                     }
                  }

                  ArenaUtil.playSounds((SoundEvent)PomkotsMechs.SE_COIN.get(), context);
                  ArenaBossBarUtil.createCountdownBar(context, "Time Left", 180000L);
               }
            }
         }
      }
   }

   private boolean setupElevator(ServerLevel level, BlockPos pos, Entity target) {
      if (level.getBlockEntity(pos) instanceof ArenaGateBlockEntity gateEntity) {
         ElevatorEntity elevator = gateEntity.refreshElevator();
         if (elevator == null) {
            return false;
         }

         elevator.setMode(ElevatorEntity.Mode.WAITING);
         elevator.setPassengerUUID(target.getUUID());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick(ArenaMatchContext context) {
      Entity challenger = context.getChallengerEntity();
      Entity opponent = context.getOpponentEntity();
      if (challenger != null && opponent != null) {
         long elapsed = context.elapsedMillis();
         ArenaBossBarUtil.updateCountdownBar(context, "Time Left", elapsed, 180000L);
         if (elapsed >= 180000L) {
            ArenaMatchProcessor.cancelMatch(context, "Entry timeout.");
         } else if (this.isMechEntried(context.getLevel(), context.match().getGatePosA()) && challenger.getVehicle() instanceof Pmvc01Entity mech) {
            if (context.runtime().runOnce("WFE_01")) {
               ArenaUtil.supplyMechConsumables(mech);
               context.match().setChallengerMechId(mech.getUUID());
               ArenaManager.update(context.server(), context.arena().getArenaId());
            }

            if (this.isMechEntried(context.getLevel(), context.match().getGatePosB()) && opponent.getVehicle() instanceof Pmvc01Entity mech2) {
               if (context.runtime().runOnce("WFE_02")) {
                  ArenaUtil.supplyMechConsumables(mech2);
                  context.match().setOpponentMechId(mech2.getUUID());
                  if (context.getOpponentEntity() instanceof ServerPlayer player) {
                     for (ItemStack dataPad : PomkotsDatapadItem.getPlayersDataPads(player)) {
                        PomkotsDatapadItem.removeArenaRequest(dataPad, context.match().getMatchId());
                     }
                  }

                  ArenaManager.update(context.server(), context.arena().getArenaId());
               }

               ArenaMatchProcessor.changeState(context, ArenaMatchState.TRANSPORT_TO_ARENA);
            }
         }
      } else {
         ArenaMatchProcessor.cancelMatch(context, "Player disconnected.");
      }
   }

   private boolean isMechEntried(Level level, BlockPos gatePos) {
      if (level.getBlockEntity(gatePos) instanceof ArenaGateBlockEntity gateEntity) {
         ElevatorEntity elev = gateEntity.getElevator();
         return elev == null ? false : !elev.getPassengers().isEmpty();
      } else {
         return false;
      }
   }

   @Override
   public void onExit(ArenaMatchContext context) {
      ArenaBossBarUtil.removeBar(context);
   }

   @Override
   public void onCancel(ArenaMatchContext context) {
      ArenaBossBarUtil.removeBar(context);
   }
}
