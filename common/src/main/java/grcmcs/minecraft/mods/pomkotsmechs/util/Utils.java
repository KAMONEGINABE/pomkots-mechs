package grcmcs.minecraft.mods.pomkotsmechs.util;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsUnbreakableBlock;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai.MechAutoController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BluePrintItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.ExplosionNoDrop;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
   protected static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   public static final ArrayList<Vec3i> circlePosRad9 = new ArrayList<>();
   public static final ArrayList<Vec3i> circlePosRad7;
   public static final ArrayList<Vec3i> circlePosRad5;
   private static String prevDestConfig;
   private static final Set<Block> nonDestructiveBlocks;
   private static String prevDropConfig;
   private static final Set<Block> nonDroppableBlocks;
   private static final Random RANDOM;

   public static boolean isRidingPomkotsControllable(Entity entity) {
      return entity != null && entity.m_20202_() instanceof PomkotsControllable;
   }

   public static boolean isRidingPomkotsVehicle(Entity entity) {
      return entity != null && entity.m_20202_() instanceof PomkotsVehicle;
   }

   public static void logInfo(String message) {
      LOGGER.info(message);
   }

   public static void logError(String message, Throwable t) {
      LOGGER.error(message, t);
   }

   private static Vec3 createEvasionVelocityFromLocalVelocity(Entity ent) {
      Vec3 localVelocity = ent.m_20184_().m_82524_((float)Math.toRadians(1.0 * ent.m_146908_()));
      double forward = localVelocity.f_82481_;
      double sideways = localVelocity.f_82479_;
      Vec3 vel;
      if (forward == 0.0 && sideways == 0.0) {
         vel = new Vec3(0.0, 0.0, 1.0);
      } else if (Math.abs(sideways) >= 0.1) {
         vel = new Vec3(sideways, 0.0, 0.0).m_82541_();
      } else {
         vel = new Vec3(sideways, 0.0, forward).m_82541_();
      }

      return vel;
   }

   public static boolean isBlockDestructionAllowed(Entity ent) {
      if (ent == null) {
         return false;
      } else {
         return ent instanceof PomkotsVehicleBase
            ? !ent.m_9236_().f_46443_ && PomkotsMechs.CONFIG.enablePlayerVehicleBlockDestruction
            : !ent.m_9236_().f_46443_ && PomkotsMechs.CONFIG.enableEntityBlockDestruction;
      }
   }

   public static void destroyBlockSphere(int radius, Vec3 offset, BlockPos ownerPos, Float ownerYRot, Level level) {
      destroyBlockSphere(radius, offset, ownerPos, ownerYRot, level, false);
   }

   public static void destroyBlockSphere(int radius, Vec3 offset, BlockPos ownerPos, Float ownerYRot, Level level, boolean erase) {
      int radiusSquared = radius * radius;
      Vec3 vec = offset;
      vec = vec.m_82524_((float)Math.toRadians(-1.0 * ownerYRot.floatValue()));
      BlockPos pos = new BlockPos(ownerPos.m_123341_() + (int)vec.f_82479_, ownerPos.m_123342_() + (int)vec.f_82480_, ownerPos.m_123343_() + (int)vec.f_82481_);

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + y * y + z * z <= radiusSquared) {
                  BlockPos targetPos = pos.m_7918_(x, y, z);
                  BlockState state = level.m_8055_(targetPos);
                  if (!state.m_60795_()) {
                     if (erase) {
                        eraseBlock(level, targetPos);
                     } else {
                        destroyBlock(level, targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                     }
                  }
               }
            }
         }
      }
   }

   public static void destroyBlock(Level level, BlockPos blockPos, boolean dropItem) {
      BlockState state = level.m_8055_(blockPos);
      if (isDestructiveBLock(state)) {
         level.m_46961_(blockPos, dropItem && isDroppableBLock(state));
      }
   }

   public static void eraseBlock(Level level, BlockPos blockPos) {
      if (isDestructiveBLock(level.m_8055_(blockPos))) {
         level.m_46597_(blockPos, Blocks.f_50016_.m_49966_());
      }
   }

   public static void setBlock(Level level, BlockPos blockPos, BlockState blockState, int num) {
      if (isDestructiveBLock(level.m_8055_(blockPos))) {
         level.m_7731_(blockPos, blockState, num);
      }
   }

   public static boolean isDestructiveBLock(BlockState blockState) {
      return isDestructiveBLock(blockState.m_60734_());
   }

   public static boolean isDestructiveBLock(Block block) {
      return !(block instanceof PomkotsUnbreakableBlock) && !nonDestructiveBlocks.contains(block);
   }

   public static void updateDestConfig(PomkotsConfig config) {
      if (!prevDestConfig.equals(config.nonDestructiveBlocks)) {
         prevDestConfig = config.nonDestructiveBlocks;
         if (prevDestConfig != null && !prevDestConfig.isEmpty()) {
            resetNonDestructiveBlocks();

            for (String blockId : prevDestConfig.split(",")) {
               Block block = (Block)BuiltInRegistries.f_256975_.m_7745_(new ResourceLocation(blockId));
               if (!Blocks.f_50016_.equals(block)) {
                  addNonDestructiveBlock(block);
               }
            }
         }
      }
   }

   public static void resetNonDestructiveBlocks() {
      nonDestructiveBlocks.clear();
   }

   public static void addNonDestructiveBlock(Block block) {
      nonDestructiveBlocks.add(block);
   }

   public static boolean isDroppableBLock(BlockState blockState) {
      return isDroppableBLock(blockState.m_60734_());
   }

   public static boolean isDroppableBLock(Block block) {
      return !(block instanceof PomkotsUnbreakableBlock) && !nonDroppableBlocks.contains(block);
   }

   public static void updateDropConfig(PomkotsConfig config) {
      if (!prevDropConfig.equals(config.nonDropBlocks)) {
         prevDropConfig = config.nonDropBlocks;
         if (prevDropConfig != null && !prevDropConfig.isEmpty()) {
            resetNonDroppableBlocks();

            for (String blockId : prevDropConfig.split(",")) {
               Block block = (Block)BuiltInRegistries.f_256975_.m_7745_(new ResourceLocation(blockId));
               if (!Blocks.f_50016_.equals(block)) {
                  addNonDroppableBlock(block);
               }
            }
         }
      }
   }

   public static void resetNonDroppableBlocks() {
      nonDroppableBlocks.clear();
   }

   public static void addNonDroppableBlock(Block block) {
      nonDroppableBlocks.add(block);
   }

   public static boolean shouldRenderCockpit(PomkotsVehicleBase vehicle) {
      if (vehicle instanceof Pmv03Entity) {
         return true;
      }

      if (vehicle instanceof Pmvc01Entity pmvc01) {
         ItemStack headStack = pmvc01.getHeadParts();
         if (headStack != null && !headStack.m_41619_() && headStack.m_41720_() instanceof BasePartsItem.Head headParts && headParts.isFullCovered()) {
            return true;
         }
      }

      return false;
   }

   public static float[] getShootingAngle(Entity bullet, Entity target, boolean useDeviation) {
      return getShootingAngle(bullet, target, useDeviation, false);
   }

   public static float[] getShootingAngle(Entity bullet, Entity target, boolean useDeviation, boolean aimFoot) {
      Vec3 targetPos = getTargetPos(target, useDeviation, aimFoot);
      if (aimFoot) {
         targetPos = new Vec3(targetPos.f_82479_, targetPos.f_82480_ - 3.0, targetPos.f_82481_);
      }

      Vec3 bulletPos = bullet.m_20182_();
      Vec3 bulletDir = targetPos.m_82546_(bulletPos).m_82541_();
      float yaw = (float)(Math.atan2(-bulletDir.f_82479_, bulletDir.f_82481_) * (180.0 / Math.PI));
      float pitch = (float)(Math.asin(-bulletDir.f_82480_) * (180.0 / Math.PI));
      return new float[]{pitch, yaw};
   }

   public static Vec3 getTargetPos(Entity target, boolean useDeviation, boolean aimFoot) {
      useDeviation = true;
      if (target.m_20202_() != null) {
         target = target.m_20202_();
      }

      Vec3 targetPos;
      if (aimFoot) {
         targetPos = target.m_20182_();
      } else {
         targetPos = target.m_20191_().m_82399_();
      }

      if (useDeviation) {
         targetPos = applyDeviation(2, targetPos, target.m_20184_());
      }

      return targetPos;
   }

   private static Vec3 applyDeviation(int num, Vec3 originalPos, Vec3 targetDelta) {
      for (int i = 0; i < num; i++) {
         originalPos = originalPos.m_82549_(targetDelta);
      }

      return originalPos;
   }

   public static float[] getShootingAngle2(Entity bullet, Entity target, boolean useDeviation, boolean aimFoot, float bulletSpeed, float inaccuracy) {
      target = getTarget2(target);
      Vec3 bulletPos = bullet.m_20182_();
      Vec3 targetPos = getTargetPos2(target, aimFoot);
      Vec3 targetVelocity = target.m_20184_();
      return useDeviation ? getShootingAngle(bulletPos, bulletSpeed, targetPos, targetVelocity, RANDOM, inaccuracy) : aimDirect(bulletPos, targetPos);
   }

   public static Entity getTarget2(Entity target) {
      if (target.m_20202_() != null) {
         target = target.m_20202_();
      }

      return target;
   }

   public static Vec3 getTargetPos2(Entity target, boolean aimFoot) {
      Vec3 targetPos;
      if (target instanceof Pmvc01Entity pmvc01) {
         targetPos = pmvc01.getPosHistory(2);
      } else {
         targetPos = target.m_20182_();
      }

      if (!aimFoot) {
         targetPos = targetPos.m_82520_(0.0, target.m_20191_().m_82376_() / 2.0, 0.0);
      }

      return targetPos;
   }

   public static float[] getShootingAngle(Vec3 bulletPos, float bulletSpeed, Vec3 targetPos, Vec3 targetVelocity, Random random, float inaccuracy) {
      Vec3 relativePos = targetPos.m_82546_(bulletPos);
      double a = targetVelocity.m_82526_(targetVelocity) - bulletSpeed * bulletSpeed;
      double b = 2.0 * relativePos.m_82526_(targetVelocity);
      double c = relativePos.m_82526_(relativePos);
      double t = solveQuadratic(a, b, c);
      Vec3 predictedPos = t >= 0.0 ? targetPos.m_82549_(targetVelocity.m_82490_(t)) : targetPos;
      if (inaccuracy > 0.0F) {
         double dist = relativePos.m_82553_();
         double offsetScale = dist * inaccuracy * 0.1;
         predictedPos = predictedPos.m_82520_(
            (random.nextDouble() - 0.5) * 2.0 * offsetScale, (random.nextDouble() - 0.5) * 2.0 * offsetScale, (random.nextDouble() - 0.5) * 2.0 * offsetScale
         );
      }

      Vec3 dir = predictedPos.m_82546_(bulletPos).m_82541_();
      float yaw = (float)(Math.atan2(-dir.f_82479_, dir.f_82481_) * (180.0 / Math.PI));
      float pitch = (float)(Math.asin(-dir.f_82480_) * (180.0 / Math.PI));
      return new float[]{pitch, yaw};
   }

   private static double solveQuadratic(double a, double b, double c) {
      if (Math.abs(a) < 1.0E-6) {
         if (Math.abs(b) < 1.0E-6) {
            return -1.0;
         }

         double t = -c / b;
         return t >= 0.0 ? t : -1.0;
      } else {
         double discriminant = b * b - 4.0 * a * c;
         if (discriminant < 0.0) {
            return -1.0;
         } else {
            double sqrtD = Math.sqrt(discriminant);
            double t1 = (-b - sqrtD) / (2.0 * a);
            double t2 = (-b + sqrtD) / (2.0 * a);
            if (t1 >= 0.0 && t2 >= 0.0) {
               return Math.min(t1, t2);
            } else if (t1 >= 0.0) {
               return t1;
            } else {
               return t2 >= 0.0 ? t2 : -1.0;
            }
         }
      }
   }

   private static float[] aimDirect(Vec3 bulletPos, Vec3 targetPos) {
      Vec3 dir = targetPos.m_82546_(bulletPos).m_82541_();
      float yaw = (float)(Math.atan2(-dir.f_82479_, dir.f_82481_) * (180.0 / Math.PI));
      float pitch = (float)(Math.asin(-dir.f_82480_) * (180.0 / Math.PI));
      return new float[]{pitch, yaw};
   }

   public static boolean isObstructed(Level level, Entity from, Entity to) {
      Vec3 fromPos = from.m_20299_(1.0F);
      Vec3 toPos = to.m_20299_(1.0F);
      BlockHitResult result = level.m_45547_(new ClipContext(fromPos, toPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, from));
      return result.m_6662_() == Type.BLOCK;
   }

   private static void playSoundEffect(SoundEvent event, Entity src) {
      src.m_9236_().m_7785_(src.m_20185_(), src.m_20186_(), src.m_20189_(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
   }

   private static void playSoundEffect(SoundEvent event, Entity src, float volume) {
      src.m_9236_().m_7785_(src.m_20185_(), src.m_20186_(), src.m_20189_(), event, SoundSource.PLAYERS, volume, 1.0F, false);
   }

   public static boolean isWithinHorizontalDistance(Entity entityA, Entity entityB, double distance) {
      Vec3 posA = entityA.m_20182_();
      Vec3 posB = entityB.m_20182_();
      double dx = posA.f_82479_ - posB.f_82479_;
      double dz = posA.f_82481_ - posB.f_82481_;
      double horizontalDistSqr = dx * dx + dz * dz;
      return horizontalDistSqr <= distance * distance;
   }

   public static LivingEntity getProjectileOwner(Projectile projectile) {
      if (projectile instanceof PomkotsThrowableProjectile ptp) {
         LivingEntity shooter = ptp.getShooter();
         if (shooter == null) {
            return null;
         } else if (shooter instanceof PomkotsVehicleBase v) {
            LivingEntity d = v.getDrivingPassenger();
            return Objects.requireNonNullElse(d, v);
         } else {
            return shooter;
         }
      } else {
         return projectile.m_19749_() instanceof LivingEntity l ? l : null;
      }
   }

   public static boolean isSystemicDamage(DamageSource source) {
      return "genericKill".equals(source.m_19385_());
   }

   public static boolean isInRangeOnAxisXZ(Entity a, Entity b, float distance) {
      if (a != null && b != null) {
         double dx = a.m_20185_() - b.m_20185_();
         double dz = a.m_20189_() - b.m_20189_();
         double distanceXZSquared = dx * dx + dz * dz;
         return distanceXZSquared <= distance * distance;
      } else {
         return false;
      }
   }

   public static boolean isMapEditingMode(Level level) {
      MinecraftServer server = level.m_7654_();
      return server == null ? true : level.m_46469_().m_46207_(PomkotsMechs.RULE_MAP_EDIT);
   }

   public static RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
      ResourceLocation rl = new ResourceLocation(typeId);
      RegistrySupplier<EntityType<?>> et = null;

      for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
         if (entityType.getId().equals(rl)) {
            et = entityType;
         }
      }

      return et;
   }

   public static List<ItemStack> getItemStacks(ItemStack mainHandStack, Player player, int totalAmountToConsume) {
      List<ItemStack> stacks = new LinkedList<>();
      int remainingAmount = totalAmountToConsume;
      Item mainHandItem = mainHandStack.m_41720_();

      for (ItemStack stack : player.m_150109_().f_35974_) {
         if (stack != mainHandStack && stack.m_41720_() == mainHandItem) {
            stacks.add(stack);
            int stackCount = stack.m_41613_();
            if (stackCount > remainingAmount) {
               return stacks;
            }

            remainingAmount -= stackCount;
         }
      }

      if (remainingAmount > 0) {
         stacks.add(mainHandStack);
         int mainHandCount = mainHandStack.m_41613_();
         if (mainHandCount <= remainingAmount) {
            remainingAmount -= mainHandCount;
         } else {
            remainingAmount = 0;
         }
      }

      return remainingAmount > 0 ? null : stacks;
   }

   public static void consumeItemStackFromTop(List<ItemStack> stacks, int num) {
      for (ItemStack stack : stacks) {
         if (stack.m_41613_() > num) {
            stack.m_41764_(stack.m_41613_() - num);
            return;
         }

         num -= stack.m_41613_();
         stack.m_41764_(0);
      }
   }

   public static void doCircleAoeDamageAndKnockback(Level level, Entity owner, Vec3 center, double radius, float damage, double knockbackStrength, double lift) {
      AABB box = new AABB(
         center.f_82479_ - radius, center.f_82480_ - 2.0, center.f_82481_ - radius, center.f_82479_ + radius, center.f_82480_ + 2.0, center.f_82481_ + radius
      );

      for (LivingEntity target : level.m_6443_(LivingEntity.class, box, e -> e != owner && !(e instanceof BossHitBoxEntity) && e.m_6084_())) {
         double dx = target.m_20185_() - center.f_82479_;
         double dz = target.m_20189_() - center.f_82481_;
         if (!(dx * dx + dz * dz > radius * radius)) {
            target.m_6469_(owner.m_269291_().m_269264_(), damage);
            Vec3 kb = computeRadialKnockback(center, target, radius, knockbackStrength, lift);
            target.m_20256_(target.m_20184_().m_82549_(kb));
            target.f_19812_ = true;
         }
      }
   }

   public static Vec3 computeRadialKnockback(Vec3 impactCenter, LivingEntity target, double radius, double strength, double lift) {
      Vec3 dir = target.m_20182_().m_82546_(impactCenter);
      Vec3 horizontal = new Vec3(dir.f_82479_, 0.0, dir.f_82481_);
      double dist = horizontal.m_82553_();
      if (dist < 1.0E-4) {
         return Vec3.f_82478_;
      }

      Vec3 norm = horizontal.m_82490_(1.0 / dist);
      double falloff = Mth.m_14008_(1.0 - dist / radius, 0.0, 1.0);
      return new Vec3(norm.f_82479_ * strength * falloff, lift * falloff, norm.f_82481_ * strength * falloff);
   }

   public static void spawnAoeDustParticles(Level level, Vec3 center, double radius, int countPerTick) {
      RandomSource rand = level.f_46441_;

      for (int i = 0; i < countPerTick; i++) {
         Vec3 pos = randomPointInCircle(rand, center, radius);
         level.m_7106_(
            new BlockParticleOption(ParticleTypes.f_123794_, Blocks.f_50493_.m_49966_()),
            pos.f_82479_,
            pos.f_82480_ + 0.2,
            pos.f_82481_,
            rand.m_188583_() * 0.03,
            2.0,
            rand.m_188583_() * 0.03
         );
      }
   }

   public static void spawnAoeDustParticles2(Level level, Vec3 center, double radius, int countPerTick) {
      RandomSource rand = level.f_46441_;

      for (int i = 0; i < countPerTick; i++) {
         Vec3 pos = randomPointInCircle(rand, center, radius);
         level.m_7107_(
            new BlockParticleOption(ParticleTypes.f_123794_, Blocks.f_50493_.m_49966_()),
            pos.f_82479_,
            pos.f_82480_ + 0.2,
            pos.f_82481_,
            rand.m_188583_() * 0.03,
            2.0,
            rand.m_188583_() * 0.03
         );
      }
   }

   private static Vec3 randomPointInCircle(RandomSource rand, Vec3 center, double radius) {
      double r = radius * Math.sqrt(rand.m_188500_());
      double theta = rand.m_188500_() * Math.PI * 2.0;
      double x = center.f_82479_ + r * Math.cos(theta);
      double z = center.f_82481_ + r * Math.sin(theta);
      double y = center.f_82480_;
      return new Vec3(x, y, z);
   }

   public static Vec3 computeAoeCenter(Entity owner, Vec3 offsetLocal) {
      Vec3 offset = owner.m_20182_();
      offsetLocal = offsetLocal.m_82524_((float)Math.toRadians(-1.0 * owner.m_146908_()));
      return offset.m_82549_(offsetLocal);
   }

   public static boolean isRidingPomkotsMechs(Player p) {
      return p == null ? false : p.m_20202_() instanceof PomkotsVehicle;
   }

   public static void explode(Entity entity, double x, double y, double z, float power, ExplosionInteraction explosionInteraction, Level level) {
      explode(entity, x, y, z, power, false, explosionInteraction, level);
   }

   public static void explode(Entity entity, double x, double y, double z, float power, boolean fire, ExplosionInteraction explosionInteraction, Level level) {
      BlockInteraction blockInteraction;
      switch (explosionInteraction) {
         case NONE:
         case MOB:
            blockInteraction = BlockInteraction.KEEP;
            break;
         case BLOCK:
            blockInteraction = BlockInteraction.DESTROY_WITH_DECAY;
            break;
         default:
            return;
      }

      Explosion explosion = new ExplosionNoDrop(level, entity, x, y, z, power, fire, blockInteraction);
      explosion.m_46061_();
      explosion.m_46075_(true);
   }

   public static boolean completeAdvancement(ResourceLocation advancementId, Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         Advancement adv = player.m_20194_().m_129889_().m_136041_(advancementId);
         if (adv == null) {
            return false;
         }

         serverPlayer.m_8960_().m_135988_(adv, "complete");
         return true;
      } else {
         return false;
      }
   }

   public static Component string2Component(String input) {
      if (input == null || input.isEmpty()) {
         return Component.m_237119_();
      } else if (input.startsWith("{") && input.endsWith("}")) {
         String key = input.substring(1, input.length() - 1);
         return Component.m_237115_(key);
      } else {
         return Component.m_237113_(input);
      }
   }

   public static Item getPartsClassFromBluePrint(ItemStack bluePrint) {
      Item bluePrintItem = bluePrint.m_41720_();
      if (!(bluePrintItem instanceof BluePrintItem)) {
         return null;
      }

      ResourceLocation id = BuiltInRegistries.f_257033_.m_7981_(bluePrintItem);
      String partsName = id.m_135815_().replaceAll("blue_print_", "");
      if (partsName.equals("hover_unit")) {
         return (Item)PomkotsMechs.HOVER_UNIT.get();
      }

      if (partsName.equals("rail_slider_unit")) {
         return (Item)PomkotsMechs.RAIL_SLIDER.get();
      }

      if (partsName.equals("proto_super_boost_unit")) {
         return (Item)PomkotsMechs.SB_PROTO.get();
      }

      if (partsName.equals("builder_unit")) {
         return (Item)PomkotsMechs.BUILDER_UNIT.get();
      }

      if (partsName.equals("glider_unit")) {
         return (Item)PomkotsMechs.GLIDER_UNIT.get();
      }

      if (partsName.equals("deneb")) {
         return (Item)PomkotsMechs.DENEB_BODY.get();
      }

      if (partsName.equals("altair")) {
         return (Item)PomkotsMechs.ALTAIR_BODY.get();
      }

      if (partsName.equals("vega")) {
         return (Item)PomkotsMechs.VEGA_BODY.get();
      }

      if (partsName.equals("sirius")) {
         return (Item)PomkotsMechs.SIRIUS_BODY.get();
      }

      if (partsName.equals("aldebaran")) {
         return (Item)PomkotsMechs.ALDEBARAN_BODY.get();
      }

      if (partsName.equals("muknvali")) {
         return (Item)PomkotsMechs.MUKNVALI_BODY.get();
      }

      Item item = (Item)BuiltInRegistries.f_257033_.m_7745_(PomkotsMechs.id(partsName));
      return item.equals(Items.f_41852_) ? null : item;
   }

   public static MechAutoController createMechAutoController(LivingEntity pilot, Pmvc01Entity mech) {
      return new MechAutoController(pilot, mech);
   }

   public static void saveBlockPos(CompoundTag tag, String namePrefix, BlockPos blockPos) {
      tag.m_128405_(PomkotsMechs.nbtName(namePrefix + "X"), blockPos.m_123341_());
      tag.m_128405_(PomkotsMechs.nbtName(namePrefix + "Y"), blockPos.m_123342_());
      tag.m_128405_(PomkotsMechs.nbtName(namePrefix + "Z"), blockPos.m_123343_());
   }

   public static BlockPos loadBlockPos(String namePrefix, CompoundTag tag) {
      return new BlockPos(
         tag.m_128451_(PomkotsMechs.nbtName(namePrefix + "X")),
         tag.m_128451_(PomkotsMechs.nbtName(namePrefix + "Y")),
         tag.m_128451_(PomkotsMechs.nbtName(namePrefix + "Z"))
      );
   }

   static {
      int rad = 5;

      for (int i = 1; i <= 9; i++) {
         circlePosRad9.add(new Vec3i(i - rad, 0, 0));
      }

      for (int i = 1; i <= 9; i++) {
         circlePosRad9.add(new Vec3i(i - rad, 0, 1));
      }

      for (int i = 1; i <= 9; i++) {
         circlePosRad9.add(new Vec3i(i - rad, 0, -1));
      }

      for (int i = 1; i <= 7; i++) {
         circlePosRad9.add(new Vec3i(i - rad + 1, 0, 2));
      }

      for (int i = 1; i <= 7; i++) {
         circlePosRad9.add(new Vec3i(i - rad + 1, 0, -2));
      }

      for (int i = 1; i <= 5; i++) {
         circlePosRad9.add(new Vec3i(i - rad + 2, 0, 3));
      }

      for (int i = 1; i <= 5; i++) {
         circlePosRad9.add(new Vec3i(i - rad + 2, 0, -3));
      }

      for (int i = 1; i <= 3; i++) {
         circlePosRad9.add(new Vec3i(i - rad + 3, 0, 4));
      }

      for (int i = 1; i <= 3; i++) {
         circlePosRad9.add(new Vec3i(i - rad + 3, 0, -4));
      }

      circlePosRad7 = new ArrayList<>();
      rad = 5;

      for (int i = 1; i <= 7; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 1, 0, -1));
      }

      for (int i = 1; i <= 7; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 1, 0, 0));
      }

      for (int i = 1; i <= 7; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 1, 0, 1));
      }

      for (int i = 1; i <= 5; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 2, 0, 2));
      }

      for (int i = 1; i <= 5; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 2, 0, -2));
      }

      for (int i = 1; i <= 3; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 3, 0, 3));
      }

      for (int i = 1; i <= 3; i++) {
         circlePosRad7.add(new Vec3i(i - rad + 3, 0, -3));
      }

      circlePosRad5 = new ArrayList<>();
      rad = 5;

      for (int i = 1; i <= 5; i++) {
         circlePosRad5.add(new Vec3i(i - rad + 2, 0, 1));
      }

      for (int i = 1; i <= 5; i++) {
         circlePosRad5.add(new Vec3i(i - rad + 2, 0, 0));
      }

      for (int i = 1; i <= 5; i++) {
         circlePosRad5.add(new Vec3i(i - rad + 2, 0, -1));
      }

      for (int i = 1; i <= 3; i++) {
         circlePosRad5.add(new Vec3i(i - rad + 3, 0, 2));
      }

      for (int i = 1; i <= 3; i++) {
         circlePosRad5.add(new Vec3i(i - rad + 3, 0, -2));
      }

      prevDestConfig = "hoge";
      nonDestructiveBlocks = new HashSet<>();
      prevDropConfig = "hoge";
      nonDroppableBlocks = new HashSet<>();
      RANDOM = new Random();
   }
}
