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
      return entity != null && entity.getVehicle() instanceof PomkotsControllable;
   }

   public static boolean isRidingPomkotsVehicle(Entity entity) {
      return entity != null && entity.getVehicle() instanceof PomkotsVehicle;
   }

   public static void logInfo(String message) {
      LOGGER.info(message);
   }

   public static void logError(String message, Throwable t) {
      LOGGER.error(message, t);
   }

   private static Vec3 createEvasionVelocityFromLocalVelocity(Entity ent) {
      Vec3 localVelocity = ent.getDeltaMovement().yRot((float)Math.toRadians(1.0 * ent.getYRot()));
      double forward = localVelocity.z;
      double sideways = localVelocity.x;
      Vec3 vel;
      if (forward == 0.0 && sideways == 0.0) {
         vel = new Vec3(0.0, 0.0, 1.0);
      } else if (Math.abs(sideways) >= 0.1) {
         vel = new Vec3(sideways, 0.0, 0.0).normalize();
      } else {
         vel = new Vec3(sideways, 0.0, forward).normalize();
      }

      return vel;
   }

   public static boolean isBlockDestructionAllowed(Entity ent) {
      if (ent == null) {
         return false;
      } else {
         return ent instanceof PomkotsVehicleBase
            ? !ent.level().isClientSide && PomkotsMechs.CONFIG.enablePlayerVehicleBlockDestruction
            : !ent.level().isClientSide && PomkotsMechs.CONFIG.enableEntityBlockDestruction;
      }
   }

   public static void destroyBlockSphere(int radius, Vec3 offset, BlockPos ownerPos, Float ownerYRot, Level level) {
      destroyBlockSphere(radius, offset, ownerPos, ownerYRot, level, false);
   }

   public static void destroyBlockSphere(int radius, Vec3 offset, BlockPos ownerPos, Float ownerYRot, Level level, boolean erase) {
      int radiusSquared = radius * radius;
      Vec3 vec = offset;
      vec = vec.yRot((float)Math.toRadians(-1.0 * ownerYRot.floatValue()));
      BlockPos pos = new BlockPos(ownerPos.getX() + (int)vec.x, ownerPos.getY() + (int)vec.y, ownerPos.getZ() + (int)vec.z);

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + y * y + z * z <= radiusSquared) {
                  BlockPos targetPos = pos.offset(x, y, z);
                  BlockState state = level.getBlockState(targetPos);
                  if (!state.isAir()) {
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
      BlockState state = level.getBlockState(blockPos);
      if (isDestructiveBLock(state)) {
         level.destroyBlock(blockPos, dropItem && isDroppableBLock(state));
      }
   }

   public static void eraseBlock(Level level, BlockPos blockPos) {
      if (isDestructiveBLock(level.getBlockState(blockPos))) {
         level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
      }
   }

   public static void setBlock(Level level, BlockPos blockPos, BlockState blockState, int num) {
      if (isDestructiveBLock(level.getBlockState(blockPos))) {
         level.setBlock(blockPos, blockState, num);
      }
   }

   public static boolean isDestructiveBLock(BlockState blockState) {
      return isDestructiveBLock(blockState.getBlock());
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
               Block block = (Block)BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));
               if (!Blocks.AIR.equals(block)) {
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
      return isDroppableBLock(blockState.getBlock());
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
               Block block = (Block)BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId));
               if (!Blocks.AIR.equals(block)) {
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
         if (headStack != null && !headStack.isEmpty() && headStack.getItem() instanceof BasePartsItem.Head headParts && headParts.isFullCovered()) {
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
         targetPos = new Vec3(targetPos.x, targetPos.y - 3.0, targetPos.z);
      }

      Vec3 bulletPos = bullet.position();
      Vec3 bulletDir = targetPos.subtract(bulletPos).normalize();
      float yaw = (float)(Math.atan2(-bulletDir.x, bulletDir.z) * (180.0 / Math.PI));
      float pitch = (float)(Math.asin(-bulletDir.y) * (180.0 / Math.PI));
      return new float[]{pitch, yaw};
   }

   public static Vec3 getTargetPos(Entity target, boolean useDeviation, boolean aimFoot) {
      useDeviation = true;
      if (target.getVehicle() != null) {
         target = target.getVehicle();
      }

      Vec3 targetPos;
      if (aimFoot) {
         targetPos = target.position();
      } else {
         targetPos = target.getBoundingBox().getCenter();
      }

      if (useDeviation) {
         targetPos = applyDeviation(2, targetPos, target.getDeltaMovement());
      }

      return targetPos;
   }

   private static Vec3 applyDeviation(int num, Vec3 originalPos, Vec3 targetDelta) {
      for (int i = 0; i < num; i++) {
         originalPos = originalPos.add(targetDelta);
      }

      return originalPos;
   }

   public static float[] getShootingAngle2(Entity bullet, Entity target, boolean useDeviation, boolean aimFoot, float bulletSpeed, float inaccuracy) {
      target = getTarget2(target);
      Vec3 bulletPos = bullet.position();
      Vec3 targetPos = getTargetPos2(target, aimFoot);
      Vec3 targetVelocity = target.getDeltaMovement();
      return useDeviation ? getShootingAngle(bulletPos, bulletSpeed, targetPos, targetVelocity, RANDOM, inaccuracy) : aimDirect(bulletPos, targetPos);
   }

   public static Entity getTarget2(Entity target) {
      if (target.getVehicle() != null) {
         target = target.getVehicle();
      }

      return target;
   }

   public static Vec3 getTargetPos2(Entity target, boolean aimFoot) {
      Vec3 targetPos;
      if (target instanceof Pmvc01Entity pmvc01) {
         targetPos = pmvc01.getPosHistory(2);
      } else {
         targetPos = target.position();
      }

      if (!aimFoot) {
         targetPos = targetPos.add(0.0, target.getBoundingBox().getYsize() / 2.0, 0.0);
      }

      return targetPos;
   }

   public static float[] getShootingAngle(Vec3 bulletPos, float bulletSpeed, Vec3 targetPos, Vec3 targetVelocity, Random random, float inaccuracy) {
      Vec3 relativePos = targetPos.subtract(bulletPos);
      double a = targetVelocity.dot(targetVelocity) - bulletSpeed * bulletSpeed;
      double b = 2.0 * relativePos.dot(targetVelocity);
      double c = relativePos.dot(relativePos);
      double t = solveQuadratic(a, b, c);
      Vec3 predictedPos = t >= 0.0 ? targetPos.add(targetVelocity.scale(t)) : targetPos;
      if (inaccuracy > 0.0F) {
         double dist = relativePos.length();
         double offsetScale = dist * inaccuracy * 0.1;
         predictedPos = predictedPos.add(
            (random.nextDouble() - 0.5) * 2.0 * offsetScale, (random.nextDouble() - 0.5) * 2.0 * offsetScale, (random.nextDouble() - 0.5) * 2.0 * offsetScale
         );
      }

      Vec3 dir = predictedPos.subtract(bulletPos).normalize();
      float yaw = (float)(Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
      float pitch = (float)(Math.asin(-dir.y) * (180.0 / Math.PI));
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
      Vec3 dir = targetPos.subtract(bulletPos).normalize();
      float yaw = (float)(Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
      float pitch = (float)(Math.asin(-dir.y) * (180.0 / Math.PI));
      return new float[]{pitch, yaw};
   }

   public static boolean isObstructed(Level level, Entity from, Entity to) {
      Vec3 fromPos = from.getEyePosition(1.0F);
      Vec3 toPos = to.getEyePosition(1.0F);
      BlockHitResult result = level.clip(new ClipContext(fromPos, toPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, from));
      return result.getType() == Type.BLOCK;
   }

   private static void playSoundEffect(SoundEvent event, Entity src) {
      src.level().playLocalSound(src.getX(), src.getY(), src.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
   }

   private static void playSoundEffect(SoundEvent event, Entity src, float volume) {
      src.level().playLocalSound(src.getX(), src.getY(), src.getZ(), event, SoundSource.PLAYERS, volume, 1.0F, false);
   }

   public static boolean isWithinHorizontalDistance(Entity entityA, Entity entityB, double distance) {
      Vec3 posA = entityA.position();
      Vec3 posB = entityB.position();
      double dx = posA.x - posB.x;
      double dz = posA.z - posB.z;
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
         return projectile.getOwner() instanceof LivingEntity l ? l : null;
      }
   }

   public static boolean isSystemicDamage(DamageSource source) {
      return "genericKill".equals(source.getMsgId());
   }

   public static boolean isInRangeOnAxisXZ(Entity a, Entity b, float distance) {
      if (a != null && b != null) {
         double dx = a.getX() - b.getX();
         double dz = a.getZ() - b.getZ();
         double distanceXZSquared = dx * dx + dz * dz;
         return distanceXZSquared <= distance * distance;
      } else {
         return false;
      }
   }

   public static boolean isMapEditingMode(Level level) {
      MinecraftServer server = level.getServer();
      return server == null ? true : level.getGameRules().getBoolean(PomkotsMechs.RULE_MAP_EDIT);
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
      Item mainHandItem = mainHandStack.getItem();

      for (ItemStack stack : player.getInventory().items) {
         if (stack != mainHandStack && stack.getItem() == mainHandItem) {
            stacks.add(stack);
            int stackCount = stack.getCount();
            if (stackCount > remainingAmount) {
               return stacks;
            }

            remainingAmount -= stackCount;
         }
      }

      if (remainingAmount > 0) {
         stacks.add(mainHandStack);
         int mainHandCount = mainHandStack.getCount();
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
         if (stack.getCount() > num) {
            stack.setCount(stack.getCount() - num);
            return;
         }

         num -= stack.getCount();
         stack.setCount(0);
      }
   }

   public static void doCircleAoeDamageAndKnockback(Level level, Entity owner, Vec3 center, double radius, float damage, double knockbackStrength, double lift) {
      AABB box = new AABB(
         center.x - radius, center.y - 2.0, center.z - radius, center.x + radius, center.y + 2.0, center.z + radius
      );

      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != owner && !(e instanceof BossHitBoxEntity) && e.isAlive())) {
         double dx = target.getX() - center.x;
         double dz = target.getZ() - center.z;
         if (!(dx * dx + dz * dz > radius * radius)) {
            target.hurt(owner.damageSources().generic(), damage);
            Vec3 kb = computeRadialKnockback(center, target, radius, knockbackStrength, lift);
            target.setDeltaMovement(target.getDeltaMovement().add(kb));
            target.hasImpulse = true;
         }
      }
   }

   public static Vec3 computeRadialKnockback(Vec3 impactCenter, LivingEntity target, double radius, double strength, double lift) {
      Vec3 dir = target.position().subtract(impactCenter);
      Vec3 horizontal = new Vec3(dir.x, 0.0, dir.z);
      double dist = horizontal.length();
      if (dist < 1.0E-4) {
         return Vec3.ZERO;
      }

      Vec3 norm = horizontal.scale(1.0 / dist);
      double falloff = Mth.clamp(1.0 - dist / radius, 0.0, 1.0);
      return new Vec3(norm.x * strength * falloff, lift * falloff, norm.z * strength * falloff);
   }

   public static void spawnAoeDustParticles(Level level, Vec3 center, double radius, int countPerTick) {
      RandomSource rand = level.random;

      for (int i = 0; i < countPerTick; i++) {
         Vec3 pos = randomPointInCircle(rand, center, radius);
         level.addParticle(
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
            pos.x,
            pos.y + 0.2,
            pos.z,
            rand.nextGaussian() * 0.03,
            2.0,
            rand.nextGaussian() * 0.03
         );
      }
   }

   public static void spawnAoeDustParticles2(Level level, Vec3 center, double radius, int countPerTick) {
      RandomSource rand = level.random;

      for (int i = 0; i < countPerTick; i++) {
         Vec3 pos = randomPointInCircle(rand, center, radius);
         level.addAlwaysVisibleParticle(
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
            pos.x,
            pos.y + 0.2,
            pos.z,
            rand.nextGaussian() * 0.03,
            2.0,
            rand.nextGaussian() * 0.03
         );
      }
   }

   private static Vec3 randomPointInCircle(RandomSource rand, Vec3 center, double radius) {
      double r = radius * Math.sqrt(rand.nextDouble());
      double theta = rand.nextDouble() * Math.PI * 2.0;
      double x = center.x + r * Math.cos(theta);
      double z = center.z + r * Math.sin(theta);
      double y = center.y;
      return new Vec3(x, y, z);
   }

   public static Vec3 computeAoeCenter(Entity owner, Vec3 offsetLocal) {
      Vec3 offset = owner.position();
      offsetLocal = offsetLocal.yRot((float)Math.toRadians(-1.0 * owner.getYRot()));
      return offset.add(offsetLocal);
   }

   public static boolean isRidingPomkotsMechs(Player p) {
      return p == null ? false : p.getVehicle() instanceof PomkotsVehicle;
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
      explosion.explode();
      explosion.finalizeExplosion(true);
   }

   public static boolean completeAdvancement(ResourceLocation advancementId, Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         Advancement adv = player.getServer().getAdvancements().getAdvancement(advancementId);
         if (adv == null) {
            return false;
         }

         serverPlayer.getAdvancements().award(adv, "complete");
         return true;
      } else {
         return false;
      }
   }

   public static Component string2Component(String input) {
      if (input == null || input.isEmpty()) {
         return Component.empty();
      } else if (input.startsWith("{") && input.endsWith("}")) {
         String key = input.substring(1, input.length() - 1);
         return Component.translatable(key);
      } else {
         return Component.literal(input);
      }
   }

   public static Item getPartsClassFromBluePrint(ItemStack bluePrint) {
      Item bluePrintItem = bluePrint.getItem();
      if (!(bluePrintItem instanceof BluePrintItem)) {
         return null;
      }

      ResourceLocation id = BuiltInRegistries.ITEM.getKey(bluePrintItem);
      String partsName = id.getPath().replaceAll("blue_print_", "");
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

      Item item = (Item)BuiltInRegistries.ITEM.get(PomkotsMechs.id(partsName));
      return item.equals(Items.AIR) ? null : item;
   }

   public static MechAutoController createMechAutoController(LivingEntity pilot, Pmvc01Entity mech) {
      return new MechAutoController(pilot, mech);
   }

   public static void saveBlockPos(CompoundTag tag, String namePrefix, BlockPos blockPos) {
      tag.putInt(PomkotsMechs.nbtName(namePrefix + "X"), blockPos.getX());
      tag.putInt(PomkotsMechs.nbtName(namePrefix + "Y"), blockPos.getY());
      tag.putInt(PomkotsMechs.nbtName(namePrefix + "Z"), blockPos.getZ());
   }

   public static BlockPos loadBlockPos(String namePrefix, CompoundTag tag) {
      return new BlockPos(
         tag.getInt(PomkotsMechs.nbtName(namePrefix + "X")),
         tag.getInt(PomkotsMechs.nbtName(namePrefix + "Y")),
         tag.getInt(PomkotsMechs.nbtName(namePrefix + "Z"))
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
