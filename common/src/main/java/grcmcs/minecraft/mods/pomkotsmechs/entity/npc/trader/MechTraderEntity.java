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
   private static final EntityDataAccessor<Long> DESPAWN_TIME = SynchedEntityData.defineId(GenericPomkotsMonster.class, EntityDataSerializers.LONG);
   private static final EntityDataAccessor<Boolean> IS_LEAVING = SynchedEntityData.defineId(GenericPomkotsMonster.class, EntityDataSerializers.BOOLEAN);
   private float soundEffectVolume = -1.0F;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.ATTACK_KNOCKBACK)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.FOLLOW_RANGE, 20.0)
         .add(Attributes.MAX_HEALTH, 500.0);
   }

   public MechTraderEntity(EntityType<? extends Mob> entityType, Level level) {
      super(entityType, level);
      this.setMaxUpStep(2.0F);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.g = new MechTraderEntity.LookAtPlayerGoal2(this, Player.class, 20.0F);
      this.goalSelector.addGoal(1, this.g);
   }

   protected BodyRotationControl createBodyControl() {
      return new BodyRotationControl(this) {
         public void clientTick() {
         }
      };
   }

   public SpawnGroupData finalizeSpawn(
      ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag
   ) {
      SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
      if (!this.level().isClientSide) {
         if (POOL == null) {
            this.generatePool(PomkotsDataPackManager.getInstance().getDataPack().getTraderPoolItems());
         }

         this.generateOffers(POOL);
      }

      this.despawnGameTime = level.getLevel().getDayTime() + 12000L;
      this.setDespawnTime(this.despawnGameTime);
      return result;
   }

   public void tick() {
      this.soundEffectVolume = -1.0F;
      super.tick();
      if (this.g != null && !this.g.isUsing) {
         this.yHeadRot = this.getYRot() - 90.0F;
      }

      if (!this.level().isClientSide()) {
         if (this.despawnGameTime >= 0L) {
            if (this.level().getDayTime() >= this.despawnGameTime && !this.isLeaving()) {
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
         ((ServerLevel)this.level()).sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
         this.discard();
      } else if (this.leavingTicks > 40) {
         Vec3 motion = this.getDeltaMovement();
         Vec3 direction = Vec3.directionFromRotation(0.0F, this.getYRot());
         motion = motion.add(direction.scale(0.5));
         double maxSpeed = 2.0;
         if (motion.horizontalDistance() > maxSpeed) {
            motion = motion.normalize().scale(maxSpeed);
         }

         this.setDeltaMovement(motion);
         this.hasImpulse = true;
      }
   }

   public void travel(Vec3 pos) {
      super.travel(pos);
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (this.isServerSide()) {
         player.openMenu(this);
      }

      return super.mobInteract(player, hand);
   }

   public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
      MechTraderMenu menu = new MechTraderMenu(id, playerInv, this);
      menu.setEntityId(this.getUUID().hashCode());
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
         Item offer = (Item)BuiltInRegistries.ITEM.get(new ResourceLocation(ent.offer_item));
         Item price = (Item)BuiltInRegistries.ITEM.get(new ResourceLocation(ent.price_item));
         POOL.add(new MechTraderEntity.TraderPoolEntry(cat, offer, ent.offer_count, price, ent.price_count, ent.weight));
      }
   }

   public void generateOffers(List<MechTraderEntity.TraderPoolEntry> pool) {
      this.inventory.clearContent();

      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         List<MechTraderEntity.TraderPoolEntry> categoryPool = pool.stream().filter(e -> e.category() == category).collect(Collectors.toList());
         this.generateCategoryOffers(category, categoryPool);
      }

      this.inventory.setChanged();
   }

   private void generateCategoryOffers(MechTraderEntity.TraderCategory category, List<MechTraderEntity.TraderPoolEntry> pool) {
      int count = 8;
      switch (category) {
         case BLUEPRINT:
            count = this.random.nextInt(4) + 1;
            break;
         case MATERIAL:
         case FOOD:
            count = this.random.nextInt(8) + 1;
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
         if (offer.getItem() instanceof BasePartsItem) {
            CompoundTag tag = offer.getOrCreateTag();
            tag.putInt(PomkotsMechs.nbtName("Level"), 1);
            offer.setTag(tag);
         }

         this.inventory.setItem(TraderInventory.getOfferSlot(category, i), offer);
         this.inventory.setItem(TraderInventory.getPriceSlot(category, i), new ItemStack(entry.priceItem(), entry.priceCount()));
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

      int roll = this.random.nextInt(totalWeight);

      for (MechTraderEntity.TraderPoolEntry entry : entries) {
         roll -= entry.weight();
         if (roll < 0) {
            return entry;
         }
      }

      return entries.get(0);
   }

   public boolean buy(ServerPlayer player, int slotIdx) {
      ItemStack offer = this.inventory.getItem(slotIdx);
      ItemStack price = this.inventory.getItem(slotIdx + 1);
      if (offer.isEmpty()) {
         return false;
      } else if (!this.canPay(player, price)) {
         this.notifyPlayer(player, (SoundEvent)PomkotsMechs.SE_BEEP.get(), "Not enough currency");
         return false;
      } else if (!player.getInventory().add(offer.copy())) {
         this.notifyPlayer(player, (SoundEvent)PomkotsMechs.SE_BEEP.get(), "Inventory full");
         return false;
      } else {
         this.consumePrice(player, price);
         this.inventory.setItem(slotIdx, new ItemStack((ItemLike)PomkotsMechs.SOLD_ITEM.get()));
         this.inventory.setChanged();
         this.notifyPlayer(player, (SoundEvent)PomkotsMechs.SE_REGISTER.get(), "Purchased");
         return true;
      }
   }

   private void notifyPlayer(ServerPlayer player, SoundEvent se, String message) {
      player.displayClientMessage(Component.literal(message), true);
      player.playNotifySound(se, SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   private boolean canPay(Player player, ItemStack price) {
      int remain = price.getCount();

      for (ItemStack stack : player.getInventory().items) {
         if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, price)) {
            remain -= stack.getCount();
            if (remain <= 0) {
               return true;
            }
         }
      }

      return false;
   }

   private void consumePrice(Player player, ItemStack price) {
      int remain = price.getCount();

      for (ItemStack stack : player.getInventory().items) {
         if (remain <= 0) {
            break;
         }

         if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, price)) {
            int consume = Math.min(remain, stack.getCount());
            stack.shrink(consume);
            remain -= consume;
         }
      }

      player.getInventory().setChanged();
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

   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Items")) {
         this.inventory.clearContent();
         ContainerHelper.loadAllItems(compound, this.inventory.items);
      }

      this.despawnGameTime = compound.getLong(PomkotsMechs.nbtName("DespawnGameTime"));
      this.setDespawnTime(this.despawnGameTime);
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      ContainerHelper.saveAllItems(compound, this.inventory.items);
      compound.putLong(PomkotsMechs.nbtName("DespawnGameTime"), this.despawnGameTime);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DESPAWN_TIME, -1L);
      this.entityData.define(IS_LEAVING, false);
   }

   public void setDespawnTime(long l) {
      this.entityData.set(DESPAWN_TIME, l);
   }

   public long getDespawnTime() {
      return (Long)this.entityData.get(DESPAWN_TIME);
   }

   public void setLeaving(boolean b) {
      this.entityData.set(IS_LEAVING, b);
   }

   public boolean isLeaving() {
      return (Boolean)this.entityData.get(IS_LEAVING);
   }

   public boolean isAffectedByFluids() {
      return false;
   }

   public int decreaseAirSupply(int air) {
      return 0;
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   public boolean isPushable() {
      return false;
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

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      if (this.soundEffectVolume < 0.0F) {
         this.soundEffectVolume = this.computeVolume(100.0);
      }

      if (this.soundEffectVolume > 0.0F) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   public void playSoundPublic(SoundEvent event) {
      this.playSoundEffect(event);
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

   public static class LookAtPlayerGoal2 extends LookAtPlayerGoal {
      public boolean isUsing = false;

      public LookAtPlayerGoal2(Mob mob, Class<? extends LivingEntity> class_, float f) {
         super(mob, class_, f, 0.5F);
      }

      public void start() {
         super.start();
         this.isUsing = true;
      }

      public void stop() {
         super.stop();
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
