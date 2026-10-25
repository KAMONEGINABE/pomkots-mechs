package grcmcs.minecraft.mods.pomkotsmechs.entity.npc;

import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMenuData;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ArenaReceptionistEntity extends PathfinderMob implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private String arenaId = "";
   private float soundEffectVolume = -1.0F;

   public static @NotNull Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.FOLLOW_RANGE, 20.0).add(Attributes.MAX_HEALTH, 20.0);
   }

   public ArenaReceptionistEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
      super(entityType, level);
      this.setMaxUpStep(2.0F);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer && this.arenaId != null && !this.arenaId.isEmpty()) {
         ArenaMenuData.Entry extendedData = new ArenaMenuData.Entry(
            this.arenaId, 0, ItemStack.EMPTY, ArenaManager.buildRanking(serverPlayer.server, this.arenaId)
         );
         MenuRegistry.openExtendedMenu(
            serverPlayer,
            new SimpleMenuProvider((id, container, p) -> new ArenaReceptionistMenu(id, container, extendedData), Component.literal("Arena Reception")),
            buf -> ArenaMenuData.write(buf, extendedData)
         );
      }

      return InteractionResult.sidedSuccess(this.level().isClientSide());
   }

   public void tick() {
      if (this.getVehicle() instanceof Pmvc01Entity) {
         this.setNoAi(true);
      } else {
         this.setNoAi(false);
      }

      super.tick();
   }

   public void travel(@NotNull Vec3 pos) {
      super.travel(pos);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> {
                  if (this.isPassenger()) {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.mech_pilot.sit"));
                  } else if (event.isMoving()) {
                     return this.getDeltaMovement().horizontalDistanceSqr() > 0.04
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_pilot.run"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_pilot.walk"));
                  } else {
                     return PlayState.STOP;
                  }
               }
            )
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public String getArenaId() {
      return this.arenaId;
   }

   public void setArenaId(String arenaId) {
      this.arenaId = arenaId;
   }

   public void readAdditionalSaveData(@NotNull CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.arenaId = compound.getString(PomkotsMechs.nbtName("ArenaId"));
   }

   public void addAdditionalSaveData(@NotNull CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      if (this.arenaId != null) {
         compound.putString(PomkotsMechs.nbtName("ArenaId"), this.arenaId);
      }
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
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
