package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BasePartsItem extends Item implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private Supplier<Object> renderProvider;
   public static BiConsumer<Consumer<Object>, BasePartsItem> regForForge;
   private CachedBoneFinder parentBoneFinder = null;
   private Pmvc01Entity parentEntity = null;
   private BasePartsItem.AttachSide attachSide = BasePartsItem.AttachSide.RIGHT;

   public BasePartsItem(Properties properties) {
      super(properties);
      if (Platform.isFabric()) {
         this.renderProvider = GeoItem.makeRenderer(this);
      }
   }

   public Supplier<Object> getRenderProvider() {
      return this.renderProvider;
   }

   public void initializeClient(Consumer<Object> consumer) {
      regForForge.accept(consumer, this);
   }

   public void createRenderer(Consumer<Object> consumer) {
      consumer.accept(new RenderProvider() {
         private BlockEntityWithoutLevelRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = (BlockEntityWithoutLevelRenderer)BasePartsItem.this.newRenderer();
            }

            return this.renderer;
         }
      });
   }

   public abstract Object newRenderer();

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public void setCachedBoneFinder(CachedBoneFinder b) {
      this.parentBoneFinder = b;
   }

   public CachedBoneFinder getCachedBoneFinder() {
      return this.parentBoneFinder;
   }

   public void setParentEntity(Pmvc01Entity b) {
      this.parentEntity = b;
   }

   public Pmvc01Entity getParentEntity() {
      return this.parentEntity;
   }

   public BasePartsItem.AttachSide getSide() {
      return this.attachSide;
   }

   public void setSide(BasePartsItem.AttachSide s) {
      this.attachSide = s;
   }

   public abstract String getPartsSeriesName();

   public abstract BasePartsItem.PartsCategory getPartsCategory();

   public abstract BasePartsItem.WeaponCategory getWeaponCategory();

   public int getLevel(ItemStack stack) {
      if (PomkotsMechs.CONFIG.enablePartsLevelCompatibility) {
         if (stack.m_41782_() && stack.m_41783_().m_128441_("Level")) {
            return stack.m_41783_().m_128451_("Level");
         }

         this.setLevel(stack, this.getMaxLevel());
         return this.getMaxLevel();
      } else {
         if (stack.m_41782_() && stack.m_41783_().m_128441_(PomkotsMechs.nbtName("Level"))) {
            return stack.m_41783_().m_128451_(PomkotsMechs.nbtName("Level"));
         }

         this.setLevel(stack, this.getMaxLevel());
         return this.getMaxLevel();
      }
   }

   public void setLevel(ItemStack stack, int level) {
      if (PomkotsMechs.CONFIG.enablePartsLevelCompatibility) {
         CompoundTag tag = stack.m_41784_();
         tag.m_128405_(PomkotsMechs.nbtName("Level"), level);
      } else {
         CompoundTag tag = stack.m_41784_();
         tag.m_128405_(PomkotsMechs.nbtName("Level"), level);
         stack.m_41751_(tag);
      }
   }

   public void m_7836_(ItemStack stack, Level world, Player player) {
      this.setLevel(stack, 1);
   }

   public int getMaxLevel() {
      PomkotsDataPack.PartsData data = this.getPartsData();
      return data != null && data.levels != null && !data.levels.isEmpty() ? data.levels.size() : 1;
   }

   private PomkotsDataPack.PartsData getPartsData() {
      return !(this instanceof BasePartsItem.Head)
            && !(this instanceof BasePartsItem.Body)
            && !(this instanceof BasePartsItem.Arm)
            && !(this instanceof BasePartsItem.Legs)
         ? PomkotsDataPackManager.getInstance().getDataPack().getPartsData(this.getPartsSeriesName())
         : PomkotsDataPackManager.getInstance().getDataPack().getPartsData(this.getPartsSeriesName() + this.getPartsCategory().getString());
   }

   private PomkotsDataPack.LevelData getLevelData(ItemStack stack, PomkotsDataPack.PartsData data) {
      if (data != null && data.levels != null && !data.levels.isEmpty()) {
         int level = this.getLevel(stack);
         return data.levels.get(level - 1);
      } else {
         return new PomkotsDataPack.LevelData();
      }
   }

   public String getAdditionalDescription(ItemStack stack) {
      PomkotsDataPack.PartsData data = this.getPartsData();
      return data != null ? data.description : "";
   }

   public int getWeight(ItemStack stack) {
      PomkotsDataPack.PartsData data = this.getPartsData();
      return data != null ? data.weight : 0;
   }

   public int getDurability(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.durability;
   }

   public int getMaxWeight(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.maxWeight;
   }

   public float getSpeedModifier(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.speedModifier;
   }

   public float getJumpModifier(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.jumpModifier;
   }

   public float getDamage(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.damage;
   }

   public int getMissileMaxNum(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.missileMaxNum;
   }

   public float getMissileLockInterval(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.missileLockInterval;
   }

   public int getMaxEnergy(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.maxEnergy;
   }

   public float getEnergyChargePerTick(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.energyChargePerTick;
   }

   public int getWorkSecPerFuel(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.workSecPerFuel;
   }

   public int getEnergyConsumeEvasion(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.energyConsumeEvasion;
   }

   public int getEnergyConsumeVertical(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.energyConsumeVertical;
   }

   public float getSpeedModifierEvasion(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.speedModifierEvasion;
   }

   public float getSpeedModifierVertical(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.speedModifierVertical;
   }

   public int getBulletsPerMagazine(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.bulletsPerMagazine;
   }

   public int getEnergyConsumePerTick(ItemStack stack) {
      PomkotsDataPack.LevelData data = this.getLevelData(stack, this.getPartsData());
      return data.energyConsumePerTick;
   }

   public void m_7373_(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
      super.m_7373_(stack, world, tooltip, flag);
      tooltip.add(Component.m_237113_(this.getAdditionalDescription(stack)).m_130940_(ChatFormatting.BLUE));
      tooltip.add(
         Component.m_237113_(this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.level}") + ": " + this.getLevel(stack)).m_130940_(ChatFormatting.WHITE)
      );
      tooltip.add(
         Component.m_237113_(this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.weight}") + ": " + String.format("%d", this.getWeight(stack)))
            .m_130940_(ChatFormatting.WHITE)
      );
      if (this instanceof BasePartsItem.Weapon) {
         tooltip.add(
            Component.m_237113_(this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.attack}") + ": " + String.format("%.1f", this.getDamage(stack)))
               .m_130940_(ChatFormatting.WHITE)
         );
         if (BasePartsItem.WeaponCategory.MISSILE == this.getWeaponCategory()) {
            tooltip.add(
               Component.m_237113_(
                     this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.maxmissilenum}") + ": " + String.format("%d", this.getMissileMaxNum(stack))
                  )
                  .m_130940_(ChatFormatting.WHITE)
            );
            tooltip.add(
               Component.m_237113_(
                     this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.lockinterval}")
                        + ": "
                        + String.format("%.1f", this.getMissileLockInterval(stack))
                  )
                  .m_130940_(ChatFormatting.WHITE)
            );
         }
      } else if (this instanceof BasePartsItem.Generator) {
         tooltip.add(
            Component.m_237113_(this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.maxenergy}") + ": " + String.format("%d", this.getMaxEnergy(stack)))
               .m_130940_(ChatFormatting.WHITE)
         );
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.enchargepertick}")
                     + ": "
                     + String.format("%.1f", this.getEnergyChargePerTick(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.worksecperfuel}") + ": " + String.format("%d", this.getWorkSecPerFuel(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
      } else if (this instanceof BasePartsItem.Booster) {
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.speedmodevasion}")
                     + ": "
                     + String.format("%.1f", this.getSpeedModifierEvasion(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.speedmodvertical}")
                     + ": "
                     + String.format("%.1f", this.getSpeedModifierVertical(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.enconsumeevasion}")
                     + ": "
                     + String.format("%d", this.getEnergyConsumeEvasion(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.enconsumevertical}")
                     + ": "
                     + String.format("%d", this.getEnergyConsumeVertical(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
      } else if (this instanceof SBUnitProtoTypeItem) {
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.enconsumepertick}")
                     + ": "
                     + String.format("%d", this.getEnergyConsumePerTick(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
      } else if (this instanceof BasePartsItem.MechParts) {
         tooltip.add(
            Component.m_237113_(
                  this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.durability}") + ": " + String.format("%d", this.getDurability(stack))
               )
               .m_130940_(ChatFormatting.WHITE)
         );
         if (BasePartsItem.PartsCategory.LEGS == this.getPartsCategory()) {
            tooltip.add(
               Component.m_237113_(
                     this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.maxweight}") + ": " + String.format("%d", this.getMaxWeight(stack))
                  )
                  .m_130940_(ChatFormatting.WHITE)
            );
            tooltip.add(
               Component.m_237113_(
                     this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.speed}") + ": " + String.format("%.1f", this.getSpeedModifier(stack))
                  )
                  .m_130940_(ChatFormatting.WHITE)
            );
            tooltip.add(
               Component.m_237113_(
                     this.getLocalizedString("{text.pomkotsmechs.gui.partsintro.jumpspeed}") + ": " + String.format("%.1f", this.getJumpModifier(stack))
                  )
                  .m_130940_(ChatFormatting.WHITE)
            );
         }
      }
   }

   private String getLocalizedString(String id) {
      return Utils.string2Component(id).getString();
   }

   public String getDefaultColor() {
      return "gray";
   }

   public abstract static class Arm extends BasePartsItem.MechParts {
      public Arm(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.ARM;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public enum AttachSide {
      RIGHT("right"),
      LEFT("left");

      private final String text;

      AttachSide(final String text) {
         this.text = text;
      }

      public String getString() {
         return this.text;
      }
   }

   public abstract static class Body extends BasePartsItem.MechParts {
      public Body(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.BODY;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }

      public float getNeckPos() {
         return 0.0F;
      }
   }

   public abstract static class Booster extends BasePartsItem.MechParts {
      public Booster(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.BOOSTER;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public abstract static class Extension extends BasePartsItem {
      public Extension(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.EXTENSION;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public abstract static class Fuel extends BasePartsItem {
      public Fuel(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.FUEL;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public abstract static class Generator extends BasePartsItem.MechParts {
      public Generator(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.GENERATOR;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public abstract static class Head extends BasePartsItem.MechParts {
      public Head(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.HEAD;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }

      public boolean isFullCovered() {
         return false;
      }
   }

   public abstract static class Legs extends BasePartsItem.MechParts {
      public Legs(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.LEGS;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public abstract static class Magazine extends BasePartsItem {
      public Magazine(Properties properties) {
         super(properties);
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.MAGAZINE;
      }

      @Override
      public BasePartsItem.WeaponCategory getWeaponCategory() {
         return BasePartsItem.WeaponCategory.NONE;
      }
   }

   public abstract static class MechParts extends BasePartsItem {
      public MechParts(Properties properties) {
         super(properties);
      }
   }

   public enum PartsCategory {
      HEAD("head"),
      BODY("body"),
      ARM("arm"),
      LEGS("legs"),
      GENERATOR("generator"),
      BOOSTER("booster"),
      EXTENSION("extension"),
      FUEL("fuel"),
      MAGAZINE("magazine"),
      WEAPON("weapon");

      private final String text;

      PartsCategory(final String text) {
         this.text = text;
      }

      public String getString() {
         return this.text;
      }
   }

   public abstract static class Weapon extends BasePartsItem implements BasePartsItem.WeaponInterface {
      public Weapon(Properties properties) {
         super(properties);
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public int maxMultiLockNum() {
         return 0;
      }

      @Override
      public boolean isSoftLockEnabled() {
         return false;
      }

      @Override
      public void tickWeaponInAction(ActionWeapon.WeaponMechInterface context, int tick, boolean isOnFire) {
      }

      public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
         return PomkotsCustomThrowableProjectile.RangeCategory.OPTIMAL;
      }

      @Override
      public BasePartsItem.PartsCategory getPartsCategory() {
         return BasePartsItem.PartsCategory.WEAPON;
      }
   }

   public abstract static class WeaponArm extends BasePartsItem.Weapon {
      public WeaponArm(Properties properties) {
         super(properties);
      }
   }

   public enum WeaponCategory {
      RIFLE("rifle"),
      MACHINE_GUN("machine_gun"),
      SHOT_GUN("shot_gun"),
      GRENADE("grenade"),
      MELEE("melee"),
      MISSILE("missile"),
      MISC("misc"),
      NONE("none");

      private final String text;

      WeaponCategory(final String text) {
         this.text = text;
      }

      public String getString() {
         return this.text;
      }
   }

   public interface WeaponInterface {
      void tickWeaponInAction(ActionWeapon.WeaponMechInterface var1, int var2, boolean var3);

      BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint();

      Motion getMotion();

      int getCoolTime();

      int maxMultiLockNum();

      boolean isSoftLockEnabled();

      default void startUsing(ActionWeapon.WeaponMechInterface context) {
      }

      default void endUsing(ActionWeapon.WeaponMechInterface context) {
      }

      default boolean isMatchAmmo(BasePartsItem.Magazine mag) {
         return false;
      }

      default boolean isToggleOn(ActionWeapon.WeaponMechInterface context) {
         return false;
      }

      enum WeaponAttachPoint {
         ATTACH_POINT_HAND("hand"),
         ATTACH_POINT_ARM("arm"),
         ATTACH_POINT_SHOULDER("shoulder"),
         ATTACH_POINT_ALL("all");

         private final String text;

         WeaponAttachPoint(final String text) {
            this.text = text;
         }

         public String getString() {
            return this.text;
         }
      }
   }

   public abstract static class WeaponShoulder extends BasePartsItem.Weapon {
      public WeaponShoulder(Properties properties) {
         super(properties);
      }
   }
}
