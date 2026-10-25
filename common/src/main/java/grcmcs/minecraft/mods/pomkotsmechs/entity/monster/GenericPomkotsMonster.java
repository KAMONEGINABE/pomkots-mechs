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
   private static final EntityDataAccessor<Boolean> IS_IN_EVENT = SynchedEntityData.m_135353_(GenericPomkotsMonster.class, EntityDataSerializers.f_135035_);
   private int breakCooldown = 0;
   private boolean breakingMode = false;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22278_, 1.0).m_22268_(Attributes.f_22277_, 200.0);
   }

   public PomkotsDataPack.EnemyData getMechData() {
      return this.mechData;
   }

   public abstract String getMechName();

   protected GenericPomkotsMonster(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.mechData = this.loadMechData();
      this.m_21051_(Attributes.f_22278_).m_22100_(this.mechData.knockBackResistance);
      this.m_21051_(Attributes.f_22277_).m_22100_(this.mechData.followRange);
      this.m_21051_(Attributes.f_22276_).m_22100_(this.mechData.health);
      this.m_21153_(this.mechData.health);
      this.m_21051_(Attributes.f_22284_).m_22100_(this.mechData.armor);
      this.m_21051_(Attributes.f_22285_).m_22100_(this.mechData.armorToughness);
   }

   protected void m_8099_() {
      super.m_8099_();
      if (this.mechData == null) {
         this.mechData = this.loadMechData();
         this.m_21051_(Attributes.f_22277_).m_22100_(this.mechData.followRange);
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

   public LivingEntity m_6688_() {
      List<Entity> passes = this.m_20197_();
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

   public void m_8119_() {
      this.soundEffectVolume = -1.0F;
      if (!this.m_9236_().f_46443_ && !this.attributeInitialized) {
         this.initializeHealthAttribute();
         this.attributeInitialized = true;
      }

      if (this.m_6688_() instanceof Player player) {
         if (this.rideCoolTick > 0) {
            this.rideCoolTick--;
         } else if (player.f_20911_) {
            this.tryAttack();
         }
      } else {
         this.m_21557_(false);
      }

      super.m_8119_();
      if (this.attackCooltime > 0) {
         this.attackCooltime--;
      }

      LivingEntity target = this.m_5448_();
      if (!this.m_9236_().f_46443_ && target != null && this.alwaysLookAtTarget) {
         this.m_21563_().m_24960_(target, 30.0F, 30.0F);
      }

      if (this.isServerSide() && this.isInRaid()) {
         this.handleObstacleBlocking();
      }
   }

   public void m_7023_(Vec3 pos) {
      if (this.m_6084_() && this.m_20160_()) {
         LivingEntity pilot = this.m_6688_();
         if (pilot != null) {
            this.m_146922_(pilot.m_146908_());
            this.f_19859_ = this.m_146908_();
            this.m_146926_(pilot.m_146909_() * 0.5F);
            this.m_19915_(this.m_146908_(), this.m_146909_());
            this.m_5618_(this.m_146908_());
            this.m_5616_(this.m_146908_());
            float f = pilot.f_20900_ * 0.5F;
            float f1 = pilot.f_20902_ * 0.5F;
            super.m_7023_(new Vec3(f, pos.f_82480_, f1));
         }
      } else {
         super.m_7023_(pos);
      }
   }

   public InteractionResult m_6071_(Player player, InteractionHand hand) {
      if (!this.m_9236_().f_46443_ && player.m_21205_().m_41720_() instanceof CoreStonePMB01Item) {
         player.m_146922_(this.m_146908_());
         player.m_146926_(this.m_146909_());
         player.m_20329_(this);
         this.m_21557_(true);
      }

      this.rideCoolTick = 3;
      return InteractionResult.m_19078_(this.m_9236_().f_46443_);
   }

   public boolean m_6094_() {
      return false;
   }

   public void rotateToTarget(LivingEntity target) {
      if (!this.m_9236_().f_46443_ && target != null) {
         Vec3 tgtPos = target.m_20182_();
         Vec3 slfPos = this.m_20182_();
         Vec3 v = slfPos.m_82505_(tgtPos);
         this.m_146922_((float)Math.toDegrees(Mth.m_14136_(v.f_82481_, v.f_82479_)) - 90.0F);
         this.m_5618_(this.m_146908_());
         this.m_5616_(this.m_146908_());
         this.f_19859_ = this.m_146908_();
         this.f_20884_ = this.m_146908_();
         this.f_20886_ = this.m_146908_();
      }
   }

   public boolean m_6783_(double d) {
      return true;
   }

   protected boolean isServerSide() {
      return !this.isClientSide();
   }

   protected boolean isClientSide() {
      return this.m_9236_().m_5776_();
   }

   protected void m_6153_() {
      this.f_20919_++;
      if (this.f_20919_ == 10) {
         this.m_142687_(RemovalReason.KILLED);
         this.generateDestroyedExplosionEffect();
      }
   }

   protected void generateDestroyedExplosionEffect() {
      if (this.isServerSide()) {
         Level level = this.m_9236_();
         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level);
         e.m_146884_(this.m_20182_());
         level.m_7967_(e);
      }
   }

   public boolean m_6469_(DamageSource ds, float dmg) {
      ParticleUtil.addParticles(ds, this);
      return super.m_6469_(ds, dmg);
   }

   public void m_6677_(DamageSource ds) {
      if (this.isClientSide()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
      }
   }

   public boolean m_6051_() {
      return false;
   }

   protected abstract void doAttack();

   protected abstract int getMaxAttackCooltime();

   public static boolean canSpawnCommon(
      EntityType<? extends GenericPomkotsMonster> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random
   ) {
      return true;
   }

   public boolean m_5545_(LevelAccessor level, MobSpawnType spawnType) {
      return true;
   }

   public SpawnGroupData m_6518_(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnData, CompoundTag tag) {
      return super.m_6518_(level, difficulty, spawnType, spawnData, tag);
   }

   public void setModifiers(float healthModifier, float attackModifier) {
      this.healthModifier = healthModifier;
      this.attackModifier = attackModifier;
   }

   protected void initializeHealthAttribute() {
      AttributeInstance healthAttribute = this.m_21051_(Attributes.f_22276_);
      if (healthAttribute != null && this.healthModifier != 1.0F) {
         double newHealthValue = healthAttribute.m_22115_() * this.healthModifier;
         healthAttribute.m_22100_(newHealthValue);
         if (this.m_21223_() > newHealthValue) {
            this.m_21153_((float)newHealthValue);
         }
      }
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(IS_IN_EVENT, false);
   }

   public void setInEvent(boolean value) {
      this.f_19804_.m_135381_(IS_IN_EVENT, value);
   }

   public boolean isInEvent() {
      return (Boolean)this.f_19804_.m_135370_(IS_IN_EVENT);
   }

   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
      this.healthModifier = compound.m_128457_(PomkotsMechs.nbtName("MonsterHealthModifier"));
      this.attackModifier = compound.m_128457_(PomkotsMechs.nbtName("MonsterAttackModifier"));
      if (this.healthModifier == 0.0F) {
         this.healthModifier = 1.0F;
      }

      if (this.attackModifier == 0.0F) {
         this.attackModifier = 1.0F;
      }

      this.isInRaid = compound.m_128471_(PomkotsMechs.nbtName("IsInRaid"));
      if (compound.m_128441_(PomkotsMechs.nbtName("IsInEvent"))) {
         this.setInEvent(compound.m_128471_(PomkotsMechs.nbtName("IsInEvent")));
      }

      this.attributeInitialized = compound.m_128471_(PomkotsMechs.nbtName("AttributeInitialized"));
   }

   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
      compound.m_128350_(PomkotsMechs.nbtName("MonsterHealthModifier"), this.healthModifier);
      compound.m_128350_(PomkotsMechs.nbtName("MonsterAttackModifier"), this.attackModifier);
      compound.m_128379_(PomkotsMechs.nbtName("IsInRaid"), this.isInRaid);
      compound.m_128379_(PomkotsMechs.nbtName("IsInEvent"), this.isInEvent());
      compound.m_128379_(PomkotsMechs.nbtName("AttributeInitialized"), this.attributeInitialized);
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
      Vec3 offset = new Vec3(target.m_20182_().f_82479_, target.m_20191_().m_82399_().f_82480_, target.m_20182_().f_82481_);

      for (int i = 0; i < 40; i++) {
         double velocityX = this.f_19796_.m_188500_() * 4.3 - 1.0;
         double velocityY = this.f_19796_.m_188500_() * 4.3 - 1.0;
         double velocityZ = this.f_19796_.m_188500_() * 4.3 - 1.0;
         this.m_9236_()
            .m_6485_((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.m_7096_(), offset.m_7098_(), offset.m_7094_(), velocityX, velocityY, velocityZ);
      }
   }

   public boolean m_6129_() {
      return false;
   }

   public int m_7302_(int air) {
      return 0;
   }

   private void handleObstacleBlocking() {
      if (this.breakCooldown > 0) {
         this.breakCooldown--;
      } else {
         Vec3 forward = this.m_20154_().m_82541_();
         double reach = 2.5;
         double width = this.m_20205_() * 0.8;
         double height = this.m_20206_();
         AABB forwardBox = this.m_20191_().m_82400_(0.1).m_82369_(forward.m_82490_(reach)).m_82377_(width * 0.2, height * 0.1, width * 0.2);
         double filledCount = 0.0;
         double totalCount = 0.0;
         double maxStep = this.m_274421_();
         double highestSolid = Double.NEGATIVE_INFINITY;

         for (BlockPos pos : BlockPos.m_121940_(
            BlockPos.m_274561_(forwardBox.f_82288_, forwardBox.f_82289_, forwardBox.f_82290_),
            BlockPos.m_274561_(forwardBox.f_82291_, forwardBox.f_82292_, forwardBox.f_82293_)
         )) {
            BlockState state = this.m_9236_().m_8055_(pos);
            if (!state.m_60795_()) {
               VoxelShape shape = state.m_60812_(this.m_9236_(), pos);
               if (!shape.m_83281_()) {
                  totalCount++;
                  filledCount++;
                  AABB blockBox = shape.m_83215_();
                  double topY = pos.m_123342_() + blockBox.f_82292_;
                  if (topY > highestSolid) {
                     highestSolid = topY;
                  }
               }
            }
         }

         double fillRatio = totalCount == 0.0 ? 0.0 : filledCount / totalCount;
         double feetY = this.m_20186_();
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
      Vec3 center = area.m_82399_();
      float strength = (this.m_20205_() + this.m_20206_()) * 0.9F;
      Utils.explode(this, center.f_82479_, center.f_82480_, center.f_82481_, strength, ExplosionInteraction.BLOCK, this.m_9236_());
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
         this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.m_91087_();
      LocalPlayer player = mc.f_91074_;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.m_20270_(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.m_14036_(volume, 0.0F, 1.0F);
   }
}
