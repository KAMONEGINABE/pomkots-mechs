package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.JinbaItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class JinbaItem extends BasePartsItem.WeaponArm {
   public JinbaItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         Entity driver = mechInterface.getDrivingPassenger();
         Vec3 pilePos1 = new Vec3(6.5 * mechInterface.isRight(), 4.0, 18.0)
            .yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .add(mechInterface.position());
         Vec3 pilePos2 = new Vec3(-4 * mechInterface.isRight(), -4.0, -4.0)
            .yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .add(mechInterface.position());
         Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
         float damage = mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory());

         for (Entity ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
            if (!mechInterface.isSelf(ent) && ent instanceof LivingEntity le) {
               if (!world.isClientSide()) {
                  le.knockback(2.0, kbVel.x, kbVel.z);
                  DamageSource ds;
                  if (driver instanceof Player p) {
                     ds = mechInterface.damageSources().playerAttack(p);
                  } else {
                     ds = mechInterface.damageSources().generic();
                  }

                  if (le instanceof BossHitBoxEntity hit) {
                     hit.addStunPoint(10);
                  }

                  le.invulnerableTime = 0;
                  le.hurt(ds, damage);
               } else {
                  mechInterface.addHitParticles(le);
               }
            }
         }

         if (world.isClientSide) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
         } else if (Utils.isBlockDestructionAllowed(mechInterface.getMechEntity())) {
            Vec3 offset = mechInterface.getOffsetByLookingDirection(7);
            if (offset != null) {
               mechInterface.breakBlockSphere(5, offset);
            }
         }
      }
   }

   @Override
   public void startUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "use");
      }
   }

   @Override
   public void endUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "stop");
      }
   }

   public void inventoryTick(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
      super.inventoryTick(stack, level, ent, slotIndex, bl);
      if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
         GeoItem.getOrAssignId(stack, serverLevel);
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "Activation", 0, state -> PlayState.STOP)
               .triggerableAnim("use", RawAnimation.begin().thenPlay("animation.weapon.use"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.weapon.idle"))
         }
      );
   }

   public JinbaItemRenderer newRenderer() {
      return new JinbaItemRenderer();
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MELEE;
   }

   @Override
   public Motion getMotion() {
      return Motion.DRILL;
   }

   @Override
   public int getCoolTime() {
      return 20;
   }

   @Override
   public String getPartsSeriesName() {
      return "jinba";
   }
}
