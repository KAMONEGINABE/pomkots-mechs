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
   private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.m_135353_(Pms10Entity.class, EntityDataSerializers.f_135028_);
   private String raidName = "";

   @Override
   public String getMechName() {
      return "pms10";
   }

   public Pms10Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
   }

   @Override
   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      if (this.isServerSide() && this.getMode() != MODE_BLUE) {
         if (this.alertTick > 0) {
            this.alertTick++;
            if (this.alertTick == 50) {
               if (this.m_5448_() != null) {
                  Level level = this.m_9236_();
                  if (this.getMode() != MODE_RED && (this.raidName == null || this.raidName.isEmpty())) {
                     this.forceTarget(level);
                  } else {
                     this.startRaid(this.raidName, level);
                  }

                  this.m_6074_();
               } else {
                  this.alertTick = 0;
               }
            }
         } else if (this.m_5448_() != null) {
            Level level = this.m_9236_();
            AlertEntity e;
            if (this.getMode() == MODE_RED) {
               this.sendTitle("Detected!", "Mech Dropship Summoned", this.m_5448_());
               e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERTRED.get(), level);
            } else {
               this.sendTitle("Detected!", "Nearby Enemies Converging", this.m_5448_());
               e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERT.get(), level);
            }

            e.m_146884_(this.m_20182_());
            level.m_7967_(e);
            this.alertTick = 1;
         }
      }
   }

   private void sendTitle(String title, String subTitle, LivingEntity target) {
      if (target instanceof ServerPlayer player) {
         ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
         ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Component.m_237113_(title));
         ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Component.m_237113_(subTitle));
         player.f_8906_.m_9829_(timesPacket);
         player.f_8906_.m_9829_(titlePacket);
         player.f_8906_.m_9829_(subtitlePacket);
      }
   }

   private void startRaid(String raidName, Level level) {
      RaidDefinition raidDef = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(raidName);
      if (raidDef != null) {
         RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).m_20615_(level);
         if (rce != null) {
            rce.m_146884_(this.m_5448_().m_20182_());
            CompoundTag tag = RaidControllerEntity.buildCompoundTag(raidName, this.m_5448_().m_20148_(), "say mission complete", "say mission failed");
            rce.m_7378_(tag);
            level.m_7967_(rce);
         }
      }
   }

   private void forceTarget(Level level) {
      for (BaseSmallMonsterEntity mob : level.m_6443_(
         BaseSmallMonsterEntity.class, this.m_20191_().m_82377_(200.0, 40.0, 200.0), e -> !(e instanceof Pms10Entity)
      )) {
         mob.m_6710_(this.m_5448_());
      }
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.f_21346_.m_262460_(goal -> true);
      this.f_21346_.m_25352_(1, new HurtByTargetGoal(this, new Class[0]));
      this.f_21346_.m_25352_(2, new NearestAttackableTargetGoal(this, Player.class, true, this::predicate));
      this.f_21345_.m_25352_(1, new SearchDroneGoal(this, this.getMechData().speed * 2.0F, 100.0F, 80.0F, 20.0F, 40));
   }

   @Override
   public boolean predicate(Object target) {
      if (target instanceof Player p) {
         boolean ridingMech = p.m_20202_() instanceof PomkotsVehicleBase;
         if (ridingMech) {
            return true;
         } else if (p.m_20270_(this) <= this.m_21051_(Attributes.f_22277_).m_22115_() * 0.5) {
            return p.m_21205_().m_150930_((Item)PomkotsMechs.CARTON.get()) ? p.m_20142_() : true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void doAttack() {
      if (this.isServerSide() && this.m_5448_() != null) {
         this.rotateToTarget(this.m_5448_());
         BulletMiddleEntity be = new BulletMiddleEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLETMIDDLE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
         );
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, 5.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         be.m_146922_(this.m_146908_());
         be.m_146926_(this.m_146909_());
         be.f_19859_ = this.m_146908_();
         be.f_19860_ = this.m_146909_();
         float[] angle = Utils.getShootingAngle(be, this.m_5448_(), true);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
         this.m_9236_().m_7967_(be);
      }
   }

   @Override
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(MODE, MODE_YELLOW);
   }

   private void setMode(int id) {
      this.f_19804_.m_135381_(MODE, id);
   }

   public int getMode() {
      return (Integer)this.f_19804_.m_135370_(MODE);
   }

   @Override
   public void m_7378_(CompoundTag tag) {
      super.m_7378_(tag);
      if (tag.m_128441_(PomkotsMechs.nbtName("DroneSearchMode"))) {
         this.setMode(tag.m_128451_(PomkotsMechs.nbtName("DroneSearchMode")));
      } else {
         this.setMode(MODE_YELLOW);
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("DroneStartRaidName"))) {
         this.raidName = tag.m_128461_(PomkotsMechs.nbtName("DroneStartRaidName"));
      }
   }

   @Override
   public void m_7380_(CompoundTag tag) {
      super.m_7380_(tag);
      tag.m_128405_(PomkotsMechs.nbtName("DroneSearchMode"), this.getMode());
      if (this.raidName != null) {
         tag.m_128359_(PomkotsMechs.nbtName("DroneStartRaidName"), this.raidName);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("DroneStartRaidName"));
      }
   }

   @Override
   public int getMaxAttackCooltime() {
      return 30;
   }

   protected float m_274460_() {
      return this.getMechData().speed;
   }

   public boolean m_20096_() {
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
