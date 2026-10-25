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
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22277_, 20.0).m_22268_(Attributes.f_22276_, 20.0);
   }

   public ArenaReceptionistEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
      super(entityType, level);
      this.m_274367_(2.0F);
   }

   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.f_21345_.m_25352_(4, new RandomLookAroundGoal(this));
   }

   public InteractionResult m_6071_(Player player, InteractionHand hand) {
      if (!this.m_9236_().m_5776_() && player instanceof ServerPlayer serverPlayer && this.arenaId != null && !this.arenaId.isEmpty()) {
         ArenaMenuData.Entry extendedData = new ArenaMenuData.Entry(
            this.arenaId, 0, ItemStack.f_41583_, ArenaManager.buildRanking(serverPlayer.f_8924_, this.arenaId)
         );
         MenuRegistry.openExtendedMenu(
            serverPlayer,
            new SimpleMenuProvider((id, container, p) -> new ArenaReceptionistMenu(id, container, extendedData), Component.m_237113_("Arena Reception")),
            buf -> ArenaMenuData.write(buf, extendedData)
         );
      }

      return InteractionResult.m_19078_(this.m_9236_().m_5776_());
   }

   public void m_8119_() {
      if (this.m_20202_() instanceof Pmvc01Entity) {
         this.m_21557_(true);
      } else {
         this.m_21557_(false);
      }

      super.m_8119_();
   }

   public void m_7023_(@NotNull Vec3 pos) {
      super.m_7023_(pos);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> {
                  if (this.m_20159_()) {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.mech_pilot.sit"));
                  } else if (event.isMoving()) {
                     return this.m_20184_().m_165925_() > 0.04
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

   public void m_7378_(@NotNull CompoundTag compound) {
      super.m_7378_(compound);
      this.arenaId = compound.m_128461_(PomkotsMechs.nbtName("ArenaId"));
   }

   public void m_7380_(@NotNull CompoundTag compound) {
      super.m_7380_(compound);
      if (this.arenaId != null) {
         compound.m_128359_(PomkotsMechs.nbtName("ArenaId"), this.arenaId);
      }
   }

   protected void m_8097_() {
      super.m_8097_();
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
