package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaBattleResultMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMatchResultData;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DodoItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaInstance;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMechData;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ArenaUtil {
   public static Entity findEntity(ServerLevel level, UUID uuid) {
      return level.m_8791_(uuid);
   }

   public static Entity findEntity(ServerLevel level, UUID uuid, ArenaFighterData.FighterType type) {
      if (uuid == null) {
         return null;
      } else {
         return (Entity)(type == ArenaFighterData.FighterType.PLAYER ? level.m_46003_(uuid) : level.m_8791_(uuid));
      }
   }

   public static boolean initPilotEntities(ArenaMatchContext context) {
      ArenaMatchData match = context.match();
      ArenaFighterData f1 = context.arena().getFighter(match.getChallengerId());
      if (f1.getType() == ArenaFighterData.FighterType.PLAYER) {
         Entity ent = findEntity(context.getLevel(), f1.getFighterId(), ArenaFighterData.FighterType.PLAYER);
         if (ent == null) {
            return false;
         }

         match.setChallengerEntityId(ent.m_20148_());
      } else {
         BlockPos gatePos = match.getGatePosA();
         float yaw = getBlockYaw(gatePos, context.getLevel());
         MechPilotEntity npc = setupNpc(f1, gatePos, context.getLevel(), yaw);
         match.setChallengerEntityId(npc.m_20148_());
         Pmvc01Entity mech = setupNpcMech(f1, gatePos, context.getLevel(), yaw);
         npc.m_20329_(mech);
      }

      ArenaFighterData f2 = context.arena().getFighter(match.getOpponentId());
      if (f2.getType() == ArenaFighterData.FighterType.PLAYER) {
         Entity ent = findEntity(context.getLevel(), f2.getFighterId(), ArenaFighterData.FighterType.PLAYER);
         if (ent == null) {
            return false;
         }

         match.setOpponentEntityId(ent.m_20148_());
      } else {
         BlockPos gatePos = match.getGatePosB();
         float yaw = getBlockYaw(gatePos, context.getLevel());
         MechPilotEntity npc = setupNpc(f2, gatePos.m_6630_(1), context.getLevel(), yaw);
         rotateEntity(npc, yaw);
         match.setOpponentEntityId(npc.m_20148_());
         Pmvc01Entity mech = setupNpcMech(f2, gatePos.m_6630_(1), context.getLevel(), yaw);
         rotateEntity(mech, yaw);
         npc.m_20329_(mech);
      }

      ArenaManager.update(context.server(), context.arena().getArenaId());
      return true;
   }

   public static void supplyMechConsumables(Pmvc01Entity mech) {
      supplyConsumables(mech);
      mech.m_21153_(mech.m_21233_());
      mech.m_6596_();
   }

   private static float getBlockYaw(BlockPos pos, ServerLevel level) {
      BlockState state = level.m_8055_(pos);
      Direction facing = (Direction)state.m_61143_(ArenaGateBlock.FACING);

      return switch (facing) {
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case NORTH -> 180.0F;
         case EAST -> -90.0F;
         default -> 0.0F;
      };
   }

   private static void rotateEntity(Entity ent, float yaw) {
      ent.m_146922_(yaw);
      ent.f_19859_ = yaw;
      ent.m_5616_(yaw);
      ent.m_5618_(yaw);
   }

   private static MechPilotEntity setupNpc(ArenaFighterData fighter, BlockPos pos, ServerLevel level, float yaw) {
      MechPilotEntity npc = new MechPilotEntity((EntityType<? extends PathfinderMob>)PomkotsMechs.MECH_PILOT.get(), level);
      npc.setModelLocation(fighter.getModel().toString());
      npc.setTextureLocation(fighter.getTexture().toString());
      ItemStack licenseItem = getLicenseItem(fighter.getAiLevel());
      ItemStack roleItem = new ItemStack((ItemLike)PomkotsMechs.PILOT_ROLE_GLADIATOR_ITEM.get());
      npc.m_21008_(InteractionHand.MAIN_HAND, licenseItem);
      npc.m_21008_(InteractionHand.OFF_HAND, roleItem);
      npc.setShouldSave(false);
      npc.m_20035_(pos, 0.0F, 0.0F);
      level.m_7967_(npc);
      return npc;
   }

   private static ItemStack getLicenseItem(String aiLevel) {
      if ("novice".equals(aiLevel)) {
         return new ItemStack((ItemLike)PomkotsMechs.PILOT_LICENSE_NOVICE_ITEM.get());
      } else if ("intermediate".equals(aiLevel)) {
         return new ItemStack((ItemLike)PomkotsMechs.PILOT_LICENSE_INTERMEDIATE_ITEM.get());
      } else {
         return "advanced".equals(aiLevel)
            ? new ItemStack((ItemLike)PomkotsMechs.PILOT_LICENSE_ADVANCED_ITEM.get())
            : new ItemStack((ItemLike)PomkotsMechs.PILOT_LICENSE_LEGEND_ITEM.get());
      }
   }

   private static Pmvc01Entity setupNpcMech(ArenaFighterData fighter, BlockPos pos, ServerLevel level, float yaw) {
      Pmvc01Entity mech = new Pmvc01Entity((EntityType<? extends LivingEntity>)PomkotsMechs.PMVC01.get(), level);
      ArenaMechData mechDef = fighter.getMechData();
      level.m_7967_(mech);
      mech.m_6836_(0, (ItemStack)mechDef.getParts().get(0));
      mech.m_6836_(1, (ItemStack)mechDef.getParts().get(1));
      mech.m_6836_(2, (ItemStack)mechDef.getParts().get(2));
      mech.m_6836_(3, (ItemStack)mechDef.getParts().get(3));
      mech.m_6836_(4, (ItemStack)mechDef.getParts().get(4));
      mech.m_6836_(5, (ItemStack)mechDef.getParts().get(5));
      mech.m_6836_(6, (ItemStack)mechDef.getParts().get(6));
      mech.m_6836_(7, (ItemStack)mechDef.getParts().get(7));
      mech.m_6836_(8, (ItemStack)mechDef.getParts().get(8));
      mech.m_6836_(9, (ItemStack)mechDef.getParts().get(9));
      mech.m_6836_(10, (ItemStack)mechDef.getParts().get(10));
      mech.m_6836_(11, (ItemStack)mechDef.getParts().get(11));
      mech.setTextureColor(mechDef.getTextureColor());
      supplyConsumables(mech);
      mech.m_6596_();
      mech.setShouldSave(false);
      mech.m_21153_(mech.m_21233_());
      mech.m_20035_(pos, 0.0F, 0.0F);
      return mech;
   }

   private static void supplyConsumables(Pmvc01Entity mech) {
      supplyWeaponMagazines(mech, 6, 12);
      supplyWeaponMagazines(mech, 7, 13);
      supplyWeaponMagazines(mech, 8, 14);
      supplyWeaponMagazines(mech, 9, 15);
      mech.m_6836_(16, getItemStack((BasePartsItem)PomkotsMechs.PELLET.get(), 64));
   }

   private static void supplyWeaponMagazines(Pmvc01Entity mech, int weaponSlot, int magazineSlot) {
      ItemStack itemStack = mech.m_8020_(weaponSlot);
      if (itemStack.m_41720_() instanceof BasePartsItem.Weapon weapon) {
         if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.RIFLE) {
            mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.RIFLE_MAGAZINE.get(), 32));
         } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.SHOT_GUN) {
            mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.SHOTGUN_MAGAZINE.get(), 32));
         } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.MACHINE_GUN) {
            if (weapon.isMatchAmmo((BasePartsItem.Magazine)PomkotsMechs.MACHINE_GUN_MAGAZINE.get())) {
               mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.MACHINE_GUN_MAGAZINE.get(), 32));
            } else {
               mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.GATLING_MAGAZINE.get(), 32));
            }
         } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.MISSILE) {
            if (weapon instanceof DodoItem) {
               mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.MISSILE_LARGE_MAGAZINE.get(), 2));
            } else {
               mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.MISSILE_MAGAZINE.get(), 12));
            }
         } else if (weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.GRENADE) {
            mech.m_6836_(magazineSlot, getItemStack((BasePartsItem)PomkotsMechs.GRENADE_MAGAZINE.get(), 12));
         }
      }
   }

   private static ItemStack getItemStack(BasePartsItem item, int num) {
      ItemStack stack = new ItemStack(item, num);
      item.setLevel(stack, 1);
      return stack;
   }

   public static boolean isNear(Entity entity, BlockPos center, double range) {
      AABB area = new AABB(center.m_252807_(), center.m_252807_()).m_82400_(range);
      System.out.println(center + ":" + entity + ":" + entity.m_20202_() + ":" + area.m_82390_(entity.m_20182_()));
      return area.m_82390_(entity.m_20182_());
   }

   public static void setupCameras(ArenaMatchContext context) {
      setupCamera(context, context.getChallengerEntity(), context.getOpponentEntity());
      setupCamera(context, context.getOpponentEntity(), context.getChallengerEntity());
   }

   public static void setupCamera(ArenaMatchContext context, Entity self, Entity target) {
      if (self instanceof ServerPlayer player) {
         ArenaCameraEntity camera = new ArenaCameraEntity((EntityType<? extends LivingEntity>)PomkotsMechs.ARENA_CAMERA.get(), player.m_9236_());
         camera.initialize(player, target);
         player.m_9236_().m_7967_(camera);
         sendCameraPacket("aros", player);
         player.f_8906_.m_9829_(new ClientboundSetCameraPacket(camera));
         context.runtime().put("camera_" + player.m_20148_(), camera);
      }
   }

   public static void playSounds(SoundEvent se, ArenaMatchContext context) {
      if (context.getChallengerEntity() instanceof ServerPlayer sp1) {
         sp1.m_6330_(se, SoundSource.MASTER, 1.0F, 0.7F);
      }

      if (context.getOpponentMechEntity() instanceof ServerPlayer sp2) {
         sp2.m_6330_(se, SoundSource.MASTER, 1.0F, 0.7F);
      }
   }

   public static void restoreCameras(ArenaMatchContext context) {
      restoreCamera(context, context.getChallengerEntity());
      restoreCamera(context, context.getOpponentEntity());
   }

   public static void restoreCamera(ArenaMatchContext context, Entity self) {
      if (self instanceof ServerPlayer player) {
         player.f_8906_.m_9829_(new ClientboundSetCameraPacket(player));
         sendCameraPacket("aroe", player);
         context.runtime().remove("camera_" + player.m_20148_());
      }
   }

   public static void startScreenFade(ArenaMatchContext context, int in, int keep, int out) {
      Entity p1 = context.getChallengerEntity();
      Entity p2 = context.getOpponentEntity();
      if (p1 instanceof ServerPlayer sp1) {
         sendScreenFadePacket(sp1, in, keep, out);
      }

      if (p2 instanceof ServerPlayer sp2) {
         sendScreenFadePacket(sp2, in, keep, out);
      }
   }

   public static void sendScreenFadePacket(ServerPlayer player, int in, int keep, int out) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.writeInt(in);
      buf.writeInt(keep);
      buf.writeInt(out);
      NetworkManager.sendToPlayer(player, PomkotsMechs.id("gsf"), buf);
   }

   private static void sendCameraPacket(String packet, ServerPlayer player) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      NetworkManager.sendToPlayer(player, PomkotsMechs.id(packet), buf);
   }

   public static ArenaFighterData getChallengerData(ArenaMatchContext context) {
      return getFighterData(context.match().getChallengerId(), context);
   }

   public static ArenaFighterData getOpponentData(ArenaMatchContext context) {
      return getFighterData(context.match().getOpponentId(), context);
   }

   public static ArenaFighterData getFighterData(UUID fighterUUID, ArenaMatchContext context) {
      return context.arena().getFighter(fighterUUID);
   }

   public static void openResultScreen(ServerPlayer serverPlayer, ArenaMatchResultData.Entry data) {
      MenuRegistry.openExtendedMenu(
         serverPlayer,
         new SimpleMenuProvider((id, inv, player) -> new ArenaBattleResultMenu(id, inv, serverPlayer, data), Component.m_237113_("Arena Result")),
         buf -> ArenaMatchResultData.write(buf, data)
      );
   }

   public static void finalizeMatch(ArenaMatchData match, ArenaInstance arena, ServerLevel level) {
      finalizeGate(match.getGatePosA(), match, arena, level);
      finalizeGate(match.getGatePosB(), match, arena, level);
      finalizeFighter(match.getChallengerId(), match.getChallengerEntityId(), match.getChallengerMechId(), match, arena, level);
      finalizeFighter(match.getOpponentId(), match.getOpponentEntityId(), match.getOpponentMechId(), match, arena, level);
   }

   private static void finalizeFighter(UUID fighterId, UUID fighterEntityId, UUID mechEntityId, ArenaMatchData match, ArenaInstance arena, ServerLevel level) {
      if (fighterId != null) {
         ArenaFighterData fighterInfo = arena.getFighter(fighterId);
         if (fighterInfo != null) {
            if (fighterInfo.getType() == ArenaFighterData.FighterType.NPC) {
               if (mechEntityId != null) {
                  Entity ent = level.m_8791_(mechEntityId);
                  if (ent instanceof Pmvc01Entity) {
                     ent.m_20153_();
                     ent.m_146870_();
                  }
               }

               if (fighterEntityId != null) {
                  Entity ent = level.m_8791_(fighterEntityId);
                  if (ent instanceof MechPilotEntity) {
                     ent.m_146870_();
                  }
               }
            }
         }
      }
   }

   private static void finalizeGate(BlockPos gatePos, ArenaMatchData match, ArenaInstance arena, ServerLevel level) {
      if (gatePos != null && level.m_7702_(gatePos) instanceof ArenaGateBlockEntity ent) {
         ent.refreshElevator();
      }
   }
}
