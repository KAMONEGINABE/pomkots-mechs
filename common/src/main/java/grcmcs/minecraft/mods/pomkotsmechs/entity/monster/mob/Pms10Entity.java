package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.SearchDroneGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pms10Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 0.5F;
   private LivingEntity prevTarget = null;
   private int alertTick = 0;
   public static int MODE_BLUE = 0;
   public static int MODE_YELLOW = 1;
   public static int MODE_RED = 2;
   private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(Pms10Entity.class, EntityDataSerializers.INT);
   private String raidName = "";

   @Override
   public String getMechName() {
      return "pms10";
   }

   public Pms10Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.setNoGravity(true);
   }

   @Override
   public void tick() {
      this.setNoGravity(true);
      super.tick();
      if (this.isServerSide() && this.getMode() != MODE_BLUE) {
         if (this.alertTick > 0) {
            this.alertTick++;
            if (this.alertTick == 50) {
               if (this.getTarget() != null) {
                  Level level = this.level();
                  if (this.getMode() != MODE_RED && (this.raidName == null || this.raidName.isEmpty())) {
                     this.forceTarget(level);
                  } else {
                     this.startRaid(this.raidName, level);
                  }

                  this.kill();
               } else {
                  this.alertTick = 0;
               }
            }
         } else if (this.getTarget() != null) {
            Level level = this.level();
            AlertEntity e;
            if (this.getMode() == MODE_RED) {
               this.sendTitle("Detected!", "Mech Dropship Summoned", this.getTarget());
               e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERTRED.get(), level);
            } else {
               this.sendTitle("Detected!", "Nearby Enemies Converging", this.getTarget());
               e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERT.get(), level);
            }

            e.setPos(this.position());
            level.addFreshEntity(e);
            this.alertTick = 1;
         }
      }
   }

   private void sendTitle(String title, String subTitle, LivingEntity target) {
      if (target instanceof ServerPlayer player) {
         ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
         ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Component.literal(title));
         ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Component.literal(subTitle));
         player.connection.send(timesPacket);
         player.connection.send(titlePacket);
         player.connection.send(subtitlePacket);
      }
   }

   private void startRaid(String raidName, Level level) {
      RaidDefinition raidDef = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(raidName);
      if (raidDef != null) {
         RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).create(level);
         if (rce != null) {
            rce.setPos(this.getTarget().position());
            CompoundTag tag = RaidControllerEntity.buildCompoundTag(raidName, this.getTarget().getUUID(), "say mission complete", "say mission failed");
            rce.readAdditionalSaveData(tag);
            level.addFreshEntity(rce);
         }
      }
   }

   private void forceTarget(Level level) {
      for (BaseSmallMonsterEntity mob : level.getEntitiesOfClass(
         BaseSmallMonsterEntity.class, this.getBoundingBox().inflate(200.0, 40.0, 200.0), e -> !(e instanceof Pms10Entity)
      )) {
         mob.setTarget(this.getTarget());
      }
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.removeAllGoals(goal -> true);
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true, this::predicate));
      this.goalSelector.addGoal(1, new SearchDroneGoal(this, this.getMechData().speed * 2.0F, 100.0F, 80.0F, 20.0F, 40));
   }

   @Override
   public boolean predicate(Object target) {
      if (target instanceof Player p) {
         boolean ridingMech = p.getVehicle() instanceof PomkotsVehicleBase;
         if (ridingMech) {
            return true;
         } else if (p.distanceTo(this) <= this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() * 0.5) {
            return p.getMainHandItem().is((Item)PomkotsMechs.CARTON.get()) ? p.isSprinting() : true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void doAttack() {
      if (this.isServerSide() && this.getTarget() != null) {
         this.rotateToTarget(this.getTarget());
         BulletMiddleEntity be = new BulletMiddleEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLETMIDDLE.get(), this.level(), this, this.getMechData().bulletDamage
         );
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, 5.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         be.setYRot(this.getYRot());
         be.setXRot(this.getXRot());
         be.yRotO = this.getYRot();
         be.xRotO = this.getXRot();
         float[] angle = Utils.getShootingAngle(be, this.getTarget(), true);
         be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().bulletSpeed, 0.0F);
         this.level().addFreshEntity(be);
      }
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(MODE, MODE_YELLOW);
   }

   private void setMode(int id) {
      this.entityData.set(MODE, id);
   }

   public int getMode() {
      return (Integer)this.entityData.get(MODE);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains(PomkotsMechs.nbtName("DroneSearchMode"))) {
         this.setMode(tag.getInt(PomkotsMechs.nbtName("DroneSearchMode")));
      } else {
         this.setMode(MODE_YELLOW);
      }

      if (tag.contains(PomkotsMechs.nbtName("DroneStartRaidName"))) {
         this.raidName = tag.getString(PomkotsMechs.nbtName("DroneStartRaidName"));
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt(PomkotsMechs.nbtName("DroneSearchMode"), this.getMode());
      if (this.raidName != null) {
         tag.putString(PomkotsMechs.nbtName("DroneStartRaidName"), this.raidName);
      } else {
         tag.remove(PomkotsMechs.nbtName("DroneStartRaidName"));
      }
   }

   @Override
   public int getMaxAttackCooltime() {
      return 30;
   }

   protected float getFlyingSpeed() {
      return this.getMechData().speed;
   }

   public boolean onGround() {
      return false;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> {
                  if (this.isClosed()) {
                     return PlayState.STOP;
                  } else {
                     return event.isMoving()
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));
                  }
               }
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms.idle"))
               .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
               .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
         }
      );
   }

   @Override
   protected void fireOpenAnimation() {
      this.triggerAnim("shoot_controller", "open");
   }

   @Override
   protected void fireCloseAnimation() {
      this.triggerAnim("shoot_controller", "close");
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public static boolean canSpawn(EntityType<Pms10Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }
}
