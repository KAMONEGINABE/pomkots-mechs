package grcmcs.minecraft.mods.pomkotsmechs.entity.monster;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.CoreStonePMB01Item;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GenericPomkotsMonster extends Monster {
   protected short rideCoolTick = 0;
   protected boolean alwaysLookAtTarget = true;
   protected boolean isInRaid = false;
   protected PomkotsDataPack.EnemyData mechData;
   private float soundEffectVolume = -1.0F;
   protected int attackCooltime = 0;
   private boolean attributeInitialized = false;
   protected float healthModifier = 1.0F;
   protected float attackModifier = 1.0F;
   private static final EntityDataAccessor<Boolean> IS_IN_EVENT = SynchedEntityData.defineId(GenericPomkotsMonster.class, EntityDataSerializers.BOOLEAN);
   private int breakCooldown = 0;
   private boolean breakingMode = false;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 200.0);
   }

   public PomkotsDataPack.EnemyData getMechData() {
      return this.mechData;
   }

   public abstract String getMechName();

   protected GenericPomkotsMonster(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.mechData = this.loadMechData();
      this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(this.mechData.knockBackResistance);
      this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(this.mechData.followRange);
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.mechData.health);
      this.setHealth(this.mechData.health);
      this.getAttribute(Attributes.ARMOR).setBaseValue(this.mechData.armor);
      this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(this.mechData.armorToughness);
   }

   protected void registerGoals() {
      super.registerGoals();
      if (this.mechData == null) {
         this.mechData = this.loadMechData();
         this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(this.mechData.followRange);
      }
   }

   protected PomkotsDataPack.EnemyData loadMechData() {
      return PomkotsDataPackManager.getInstance().getDataPack().getEnemyData(this.getMechName());
   }

   public boolean tryAttack() {
      if (this.attackCooltime == 0) {
         this.doAttack();
         this.attackCooltime = this.getMaxAttackCooltime();
         return true;
      } else {
         return false;
      }
   }

   public boolean isInAttackCooltime() {
      return this.attackCooltime > 0;
   }

   public LivingEntity getControllingPassenger() {
      List<Entity> passes = this.getPassengers();
      if (passes.isEmpty()) {
         return null;
      }

      for (Entity ent : passes) {
         if (ent instanceof LivingEntity le) {
            return le;
         }
      }

      return null;
   }

   public void tick() {
      this.soundEffectVolume = -1.0F;
      if (!this.level().isClientSide && !this.attributeInitialized) {
         this.initializeHealthAttribute();
         this.attributeInitialized = true;
      }

      if (this.getControllingPassenger() instanceof Player player) {
         if (this.rideCoolTick > 0) {
            this.rideCoolTick--;
         } else if (player.swinging) {
            this.tryAttack();
         }
      } else {
         this.setNoAi(false);
      }

      super.tick();
      if (this.attackCooltime > 0) {
         this.attackCooltime--;
      }

      LivingEntity target = this.getTarget();
      if (!this.level().isClientSide && target != null && this.alwaysLookAtTarget) {
         this.getLookControl().setLookAt(target, 30.0F, 30.0F);
      }

      if (this.isServerSide() && this.isInRaid()) {
         this.handleObstacleBlocking();
      }
   }

   public void travel(Vec3 pos) {
      if (this.isAlive() && this.isVehicle()) {
         LivingEntity pilot = this.getControllingPassenger();
         if (pilot != null) {
            this.setYRot(pilot.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(pilot.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.setYBodyRot(this.getYRot());
            this.setYHeadRot(this.getYRot());
            float f = pilot.xxa * 0.5F;
            float f1 = pilot.zza * 0.5F;
            super.travel(new Vec3(f, pos.y, f1));
         }
      } else {
         super.travel(pos);
      }
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (!this.level().isClientSide && player.getMainHandItem().getItem() instanceof CoreStonePMB01Item) {
         player.setYRot(this.getYRot());
         player.setXRot(this.getXRot());
         player.startRiding(this);
         this.setNoAi(true);
      }

      this.rideCoolTick = 3;
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   public boolean isPushable() {
      return false;
   }

   public void rotateToTarget(LivingEntity target) {
      if (!this.level().isClientSide && target != null) {
         Vec3 tgtPos = target.position();
         Vec3 slfPos = this.position();
         Vec3 v = slfPos.vectorTo(tgtPos);
         this.setYRot((float)Math.toDegrees(Mth.atan2(v.z, v.x)) - 90.0F);
         this.setYBodyRot(this.getYRot());
         this.setYHeadRot(this.getYRot());
         this.yRotO = this.getYRot();
         this.yBodyRotO = this.getYRot();
         this.yHeadRotO = this.getYRot();
      }
   }

   public boolean shouldRenderAtSqrDistance(double d) {
      return true;
   }

   protected boolean isServerSide() {
      return !this.isClientSide();
   }

   protected boolean isClientSide() {
      return this.level().isClientSide();
   }

   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 10) {
         this.remove(RemovalReason.KILLED);
         this.generateDestroyedExplosionEffect();
      }
   }

   protected void generateDestroyedExplosionEffect() {
      if (this.isServerSide()) {
         Level level = this.level();
         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level);
         e.setPos(this.position());
         level.addFreshEntity(e);
      }
   }

   public boolean hurt(DamageSource ds, float dmg) {
      ParticleUtil.addParticles(ds, this);
      return super.hurt(ds, dmg);
   }

   public void playHurtSound(DamageSource ds) {
      if (this.isClientSide()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
      }
   }

   public boolean displayFireAnimation() {
      return false;
   }

   protected abstract void doAttack();

   protected abstract int getMaxAttackCooltime();

   public static boolean canSpawnCommon(
      EntityType<? extends GenericPomkotsMonster> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random
   ) {
      return true;
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
      return true;
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnData, CompoundTag tag) {
      return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
   }

   public void setModifiers(float healthModifier, float attackModifier) {
      this.healthModifier = healthModifier;
      this.attackModifier = attackModifier;
   }

   protected void initializeHealthAttribute() {
      AttributeInstance healthAttribute = this.getAttribute(Attributes.MAX_HEALTH);
      if (healthAttribute != null && this.healthModifier != 1.0F) {
         double newHealthValue = healthAttribute.getBaseValue() * this.healthModifier;
         healthAttribute.setBaseValue(newHealthValue);
         if (this.getHealth() > newHealthValue) {
            this.setHealth((float)newHealthValue);
         }
      }
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_IN_EVENT, false);
   }

   public void setInEvent(boolean value) {
      this.entityData.set(IS_IN_EVENT, value);
   }

   public boolean isInEvent() {
      return (Boolean)this.entityData.get(IS_IN_EVENT);
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.healthModifier = compound.getFloat(PomkotsMechs.nbtName("MonsterHealthModifier"));
      this.attackModifier = compound.getFloat(PomkotsMechs.nbtName("MonsterAttackModifier"));
      if (this.healthModifier == 0.0F) {
         this.healthModifier = 1.0F;
      }

      if (this.attackModifier == 0.0F) {
         this.attackModifier = 1.0F;
      }

      this.isInRaid = compound.getBoolean(PomkotsMechs.nbtName("IsInRaid"));
      if (compound.contains(PomkotsMechs.nbtName("IsInEvent"))) {
         this.setInEvent(compound.getBoolean(PomkotsMechs.nbtName("IsInEvent")));
      }

      this.attributeInitialized = compound.getBoolean(PomkotsMechs.nbtName("AttributeInitialized"));
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putFloat(PomkotsMechs.nbtName("MonsterHealthModifier"), this.healthModifier);
      compound.putFloat(PomkotsMechs.nbtName("MonsterAttackModifier"), this.attackModifier);
      compound.putBoolean(PomkotsMechs.nbtName("IsInRaid"), this.isInRaid);
      compound.putBoolean(PomkotsMechs.nbtName("IsInEvent"), this.isInEvent());
      compound.putBoolean(PomkotsMechs.nbtName("AttributeInitialized"), this.attributeInitialized);
   }

   public boolean isInRaid() {
      return this.isInRaid;
   }

   public void setInRaid(boolean flag) {
      this.isInRaid = flag;
   }

   public float getModifiedDamage(float base) {
      return base * this.attackModifier;
   }

   protected void addHitParticles(Entity target) {
      Vec3 offset = new Vec3(target.position().x, target.getBoundingBox().getCenter().y, target.position().z);

      for (int i = 0; i < 40; i++) {
         double velocityX = this.random.nextDouble() * 4.3 - 1.0;
         double velocityY = this.random.nextDouble() * 4.3 - 1.0;
         double velocityZ = this.random.nextDouble() * 4.3 - 1.0;
         this.level()
            .addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.x(), offset.y(), offset.z(), velocityX, velocityY, velocityZ);
      }
   }

   public boolean isAffectedByFluids() {
      return false;
   }

   public int decreaseAirSupply(int air) {
      return 0;
   }

   private void handleObstacleBlocking() {
      if (this.breakCooldown > 0) {
         this.breakCooldown--;
      } else {
         Vec3 forward = this.getLookAngle().normalize();
         double reach = 2.5;
         double width = this.getBbWidth() * 0.8;
         double height = this.getBbHeight();
         AABB forwardBox = this.getBoundingBox().inflate(0.1).expandTowards(forward.scale(reach)).inflate(width * 0.2, height * 0.1, width * 0.2);
         double filledCount = 0.0;
         double totalCount = 0.0;
         double maxStep = this.maxUpStep();
         double highestSolid = Double.NEGATIVE_INFINITY;

         for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(forwardBox.minX, forwardBox.minY, forwardBox.minZ),
            BlockPos.containing(forwardBox.maxX, forwardBox.maxY, forwardBox.maxZ)
         )) {
            BlockState state = this.level().getBlockState(pos);
            if (!state.isAir()) {
               VoxelShape shape = state.getCollisionShape(this.level(), pos);
               if (!shape.isEmpty()) {
                  totalCount++;
                  filledCount++;
                  AABB blockBox = shape.bounds();
                  double topY = pos.getY() + blockBox.maxY;
                  if (topY > highestSolid) {
                     highestSolid = topY;
                  }
               }
            }
         }

         double fillRatio = totalCount == 0.0 ? 0.0 : filledCount / totalCount;
         double feetY = this.getY();
         double stepHeight = highestSolid - feetY;
         boolean tooHigh = stepHeight > maxStep + 0.2;
         boolean tooDense = fillRatio > 0.7;
         if (!tooHigh || !tooDense) {
            this.breakingMode = false;
         } else if (!this.breakingMode) {
            this.breakingMode = true;
            this.triggerExplosion(forwardBox);
            this.breakCooldown = 100;
         }
      }
   }

   private void triggerExplosion(AABB area) {
      Vec3 center = area.getCenter();
      float strength = (this.getBbWidth() + this.getBbHeight()) * 0.9F;
      Utils.explode(this, center.x, center.y, center.z, strength, ExplosionInteraction.BLOCK, this.level());
   }

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      this.playSoundEffect(event, volume, 100.0);
   }

   protected void playSoundEffect(SoundEvent event, float volume, double maxDistance) {
      if (this.soundEffectVolume < 0.0F) {
         this.soundEffectVolume = this.computeVolume(maxDistance);
      }

      if (this.soundEffectVolume > 0.0F) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.distanceTo(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.clamp(volume, 0.0F, 1.0F);
   }
}
