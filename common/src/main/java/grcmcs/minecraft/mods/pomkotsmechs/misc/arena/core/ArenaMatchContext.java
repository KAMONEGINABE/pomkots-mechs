package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaInstance;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ArenaMatchContext {
   private final MinecraftServer server;
   private final ServerLevel level;
   private final ArenaInstance arena;
   private final ArenaMatchData match;
   private final ArenaMatchRuntime runtime;

   public ArenaMatchContext(MinecraftServer server, ServerLevel level, ArenaInstance arena, ArenaMatchData match, ArenaMatchRuntime runtime) {
      this.server = server;
      this.level = level;
      this.arena = arena;
      this.match = match;
      this.runtime = runtime;
   }

   public MinecraftServer server() {
      return this.server;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public ArenaInstance arena() {
      return this.arena;
   }

   public ArenaMatchData match() {
      return this.match;
   }

   public ArenaMatchRuntime runtime() {
      return this.runtime;
   }

   public long elapsedMillis() {
      return System.currentTimeMillis() - this.match.getStateChangedTime();
   }

   public Entity getChallengerEntity() {
      Entity entity = this.runtime.getChallengerEntity();
      if (entity != null && entity.isAlive()) {
         return entity;
      }

      entity = ArenaUtil.findEntity(this.level, this.match.getChallengerEntityId(), this.arena.getFighter(this.match.getChallengerId()).getType());
      this.runtime.setChallengerEntity(entity);
      return entity;
   }

   public Entity getChallengerMechEntity() {
      Entity entity = this.runtime.getChallengerMech();
      if (entity != null && entity.isAlive()) {
         return entity;
      }

      entity = ArenaUtil.findEntity(this.level, this.match.getChallengerMechId());
      this.runtime.setChallengerMech(entity);
      return entity;
   }

   public Entity getOpponentEntity() {
      Entity entity = this.runtime.getOpponentEntity();
      if (entity != null && entity.isAlive()) {
         return entity;
      }

      entity = ArenaUtil.findEntity(this.level, this.match.getOpponentEntityId(), this.arena.getFighter(this.match.getOpponentId()).getType());
      this.runtime.setOpponentEntity(entity);
      return entity;
   }

   public Entity getOpponentMechEntity() {
      Entity entity = this.runtime.getOpponentMech();
      if (entity != null && entity.isAlive()) {
         return entity;
      }

      entity = ArenaUtil.findEntity(this.level, this.match.getOpponentMechId());
      this.runtime.setOpponentMech(entity);
      return entity;
   }
}
