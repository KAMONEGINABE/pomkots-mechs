package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechTraderMenu;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MechTraderEntity extends Mob implements GeoEntity, GeoAnimatable, MenuProvider {
   private static List<MechTraderEntity.TraderPoolEntry> POOL = null;
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private final TraderInventory inventory = new TraderInventory();
   private long despawnGameTime = -1L;
   private int leavingTicks = 0;
   MechTraderEntity.LookAtPlayerGoal2 g;
   private static final EntityDataAccessor<Long> DESPAWN_TIME = SynchedEntityData.m_135353_(GenericPomkotsMonster.class, EntityDataSerializers.f_244073_);
   private static final EntityDataAccessor<Boolean> IS_LEAVING = SynchedEntityData.m_135353_(GenericPomkotsMonster.class, EntityDataSerializers.f_135035_);
   private float soundEffectVolume = -1.0F;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_()
         .m_22266_(Attributes.f_22282_)
         .m_22268_(Attributes.f_22278_, 1.0)
         .m_22268_(Attributes.f_22277_, 20.0)
         .m_22268_(Attributes.f_22276_, 500.0);
   }

   public MechTraderEntity(EntityType<? extends Mob> entityType, Level level) {
      super(entityType, level);
      this.m_274367_(2.0F);
   }

   protected void m_8099_() {
      super.m_8099_();
      this.g = new MechTraderEntity.LookAtPlayerGoal2(this, Player.class, 20.0F);
      this.f_21345_.m_25352_(1, this.g);
   }

   protected BodyRotationControl m_7560_() {
      return new BodyRotationControl(this) {
         public void m_8121_() {
         }
      };
   }

   public SpawnGroupData m_6518_(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag
   ) {
      SpawnGroupData result = super.m_6518_(level, difficulty, reason, spawnData, tag);
      if (!this.m_9236_().f_46443_) {
         if (POOL == null) {
            this.generatePool(PomkotsDataPackManager.getInstance().getDataPack().getTraderPoolItems());
         }

         this.generateOffers(POOL);
      }

      this.despawnGameTime = level.m_6018_().m_46468_() + 12000L;
      this.setDespawnTime(this.despawnGameTime);
      return result;
   }

   public void m_8119_() {
      this.soundEffectVolume = -1.0F;
      super.m_8119_();
      if (this.g != null && !this.g.isUsing) {
         this.f_20885_ = this.m_146908_() - 90.0F;
      }

      if (!this.m_9236_().m_5776_()) {
         if (this.despawnGameTime >= 0L) {
            if (this.m_9236_().m_46468_() >= this.despawnGameTime && !this.isLeaving()) {
               this.setLeaving(true);
               this.triggerAnim("departure", "start_departure");
            }

            if (this.isLeaving()) {
               this.tickLeaving();
            }
         }
      }
   }

   private void tickLeaving() {
      this.leavingTicks++;
      if (this.leavingTicks > 120) {
         ((ServerLevel)this.m_9236_()).m_8767_(ParticleTypes.f_123796_, this.m_20185_(), this.m_20186_() + 1.0, this.m_20189_(), 20, 0.5, 0.5, 0.5, 0.1);
         this.m_146870_();
      } else if (this.leavingTicks > 40) {
         Vec3 motion = this.m_20184_();
         Vec3 direction = Vec3.m_82498_(0.0F, this.m_146908_());
         motion = motion.m_82549_(direction.m_82490_(0.5));
         double maxSpeed = 2.0;
         if (motion.m_165924_() > maxSpeed) {
            motion = motion.m_82541_().m_82490_(maxSpeed);
         }

         this.m_20256_(motion);
         this.f_19812_ = true;
      }
   }

   public void m_7023_(Vec3 pos) {
      super.m_7023_(pos);
   }

   public InteractionResult m_6071_(Player player, InteractionHand hand) {
      if (this.isServerSide()) {
         player.m_5893_(this);
      }

      return super.m_6071_(player, hand);
   }

   public AbstractContainerMenu m_7208_(int id, Inventory playerInv, Player player) {
      MechTraderMenu menu = new MechTraderMenu(id, playerInv, this);
      menu.setEntityId(this.m_20148_().hashCode());
      return menu;
   }

   public TraderInventory getInventory() {
      return this.inventory;
   }

   public void generatePool(List<PomkotsDataPack.TraderPoolItem> config) {
      POOL = new ArrayList<>();

      for (PomkotsDataPack.TraderPoolItem ent : config) {
         MechTraderEntity.TraderCategory cat = switch (ent.category) {
            case "BLUEPRINT" -> MechTraderEntity.TraderCategory.BLUEPRINT;
            case "MATERIAL" -> MechTraderEntity.TraderCategory.MATERIAL;
            case "CONSUMABLE" -> MechTraderEntity.TraderCategory.CONSUMABLE;
            default -> MechTraderEntity.TraderCategory.FOOD;
         };
         Item offer = (Item)BuiltInRegistries.f_257033_.m_7745_(new ResourceLocation(ent.offer_item));
         Item price = (Item)BuiltInRegistries.f_257033_.m_7745_(new ResourceLocation(ent.price_item));
         POOL.add(new MechTraderEntity.TraderPoolEntry(cat, offer, ent.offer_count, price, ent.price_count, ent.weight));
      }
   }

   public void generateOffers(List<MechTraderEntity.TraderPoolEntry> pool) {
      this.inventory.m_6211_();

      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         List<MechTraderEntity.TraderPoolEntry> categoryPool = pool.stream().filter(e -> e.category() == category).collect(Collectors.toList());
         this.generateCategoryOffers(category, categoryPool);
      }

      this.inventory.m_6596_();
   }

   private void generateCategoryOffers(MechTraderEntity.TraderCategory category, List<MechTraderEntity.TraderPoolEntry> pool) {
      int count = 8;
      switch (category) {
         case BLUEPRINT:
            count = this.f_19796_.m_188503_(4) + 1;
            break;
         case MATERIAL:
         case FOOD:
            count = this.f_19796_.m_188503_(8) + 1;
         case CONSUMABLE:
      }

      count = Math.min(count, pool.size());

      for (int i = 0; i < count; i++) {
         MechTraderEntity.TraderPoolEntry entry = this.pickWeighted(pool);
         if (entry == null) {
            break;
         }

         pool.remove(entry);
         ItemStack offer = new ItemStack(entry.offerItem(), entry.offerCount());
         if (offer.m_41720_() instanceof BasePartsItem) {
            CompoundTag tag = offer.m_41784_();
            tag.m_128405_(PomkotsMechs.nbtName("Level"), 1);
            offer.m_41751_(tag);
         }

         this.inventory.m_6836_(TraderInventory.getOfferSlot(category, i), offer);
         this.inventory.m_6836_(TraderInventory.getPriceSlot(category, i), new ItemStack(entry.priceItem(), entry.priceCount()));
      }
   }

   @Nullable
   private MechTraderEntity.TraderPoolEntry pickWeighted(List<MechTraderEntity.TraderPoolEntry> entries) {
      if (entries.isEmpty()) {
         return null;
      }

      int totalWeight = 0;

      for (MechTraderEntity.TraderPoolEntry entry : entries) {
         totalWeight += entry.weight();
      }

      int roll = this.f_19796_.m_188503_(totalWeight);

      for (MechTraderEntity.TraderPoolEntry entry : entries) {
         roll -= entry.weight();
         if (roll < 0) {
            return entry;
         }
      }

      return entries.get(0);
   }

   public boolean buy(ServerPlayer player, int slotIdx) {
      ItemStack offer = this.inventory.m_8020_(slotIdx);
      ItemStack price = this.inventory.m_8020_(slotIdx + 1);
      if (offer.m_41619_()) {
         return false;
      } else if (!this.canPay(player, price)) {
         this.notifyPlayer(player, (SoundEvent)PomkotsMechs.SE_BEEP.get(), "Not enough currency");
         return false;
      } else if (!player.m_150109_().m_36054_(offer.m_41777_())) {
         this.notifyPlayer(player, (SoundEvent)PomkotsMechs.SE_BEEP.get(), "Inventory full");
         return false;
      } else {
         this.consumePrice(player, price);
         this.inventory.m_6836_(slotIdx, new ItemStack((ItemLike)PomkotsMechs.SOLD_ITEM.get()));
         this.inventory.m_6596_();
         this.notifyPlayer(player, (SoundEvent)PomkotsMechs.SE_REGISTER.get(), "Purchased");
         return true;
      }
   }

   private void notifyPlayer(ServerPlayer player, SoundEvent se, String message) {
      player.m_5661_(Component.m_237113_(message), true);
      player.m_6330_(se, SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   private boolean canPay(Player player, ItemStack price) {
      int remain = price.m_41613_();

      for (ItemStack stack : player.m_150109_().f_35974_) {
         if (!stack.m_41619_() && ItemStack.m_150942_(stack, price)) {
            remain -= stack.m_41613_();
            if (remain <= 0) {
               return true;
            }
         }
      }

      return false;
   }

   private void consumePrice(Player player, ItemStack price) {
      int remain = price.m_41613_();

      for (ItemStack stack : player.m_150109_().f_35974_) {
         if (remain <= 0) {
            break;
         }

         if (!stack.m_41619_() && ItemStack.m_150942_(stack, price)) {
            int consume = Math.min(remain, stack.m_41613_());
            stack.m_41774_(consume);
            remain -= consume;
         }
      }

      player.m_150109_().m_6596_();
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> !this.isLeaving()
                  ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_trader.opened"))
                  : event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_trader.idle"))
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "departure", state -> PlayState.STOP)
               .triggerableAnim("start_departure", RawAnimation.begin().thenPlay("animation.mech_trader.departure").thenLoop("animation.mech_trader.run"))
               .setSoundKeyframeHandler(soundKeyframeEvent -> {
                  if ("se_close_door".equals(soundKeyframeEvent.getKeyframeData().getSound())) {
                     this.playSoundEffect((SoundEvent)PomkotsMechs.SE_CLOSE_DOOR.get());
                  } else if ("se_start_car".equals(soundKeyframeEvent.getKeyframeData().getSound())) {
                     this.playSoundEffect((SoundEvent)PomkotsMechs.SE_START_CAR.get());
                  }
               })
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
      if (compound.m_128441_("Items")) {
         this.inventory.m_6211_();
         ContainerHelper.m_18980_(compound, this.inventory.f_19147_);
      }

      this.despawnGameTime = compound.m_128454_(PomkotsMechs.nbtName("DespawnGameTime"));
      this.setDespawnTime(this.despawnGameTime);
   }

   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
      ContainerHelper.m_18973_(compound, this.inventory.f_19147_);
      compound.m_128356_(PomkotsMechs.nbtName("DespawnGameTime"), this.despawnGameTime);
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(DESPAWN_TIME, -1L);
      this.f_19804_.m_135372_(IS_LEAVING, false);
   }

   public void setDespawnTime(long l) {
      this.f_19804_.m_135381_(DESPAWN_TIME, l);
   }

   public long getDespawnTime() {
      return (Long)this.f_19804_.m_135370_(DESPAWN_TIME);
   }

   public void setLeaving(boolean b) {
      this.f_19804_.m_135381_(IS_LEAVING, b);
   }

   public boolean isLeaving() {
      return (Boolean)this.f_19804_.m_135370_(IS_LEAVING);
   }

   public boolean m_6129_() {
      return false;
   }

   public int m_7302_(int air) {
      return 0;
   }

   public boolean m_6785_(double distanceToClosestPlayer) {
      return false;
   }

   public boolean m_6094_() {
      return false;
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

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      if (this.soundEffectVolume < 0.0F) {
         this.soundEffectVolume = this.computeVolume(100.0);
      }

      if (this.soundEffectVolume > 0.0F) {
         this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   public void playSoundPublic(SoundEvent event) {
      this.playSoundEffect(event);
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

   public static class LookAtPlayerGoal2 extends LookAtPlayerGoal {
      public boolean isUsing = false;

      public LookAtPlayerGoal2(Mob mob, Class<? extends LivingEntity> class_, float f) {
         super(mob, class_, f, 0.5F);
      }

      public void m_8056_() {
         super.m_8056_();
         this.isUsing = true;
      }

      public void m_8041_() {
         super.m_8041_();
         this.isUsing = false;
      }
   }

   public enum TraderCategory {
      BLUEPRINT,
      MATERIAL,
      CONSUMABLE,
      FOOD;
   }

   public record TraderPoolEntry(MechTraderEntity.TraderCategory category, Item offerItem, int offerCount, Item priceItem, int priceCount, int weight) {
   }
}
