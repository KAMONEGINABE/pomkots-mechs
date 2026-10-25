package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PersonalPomkotsCubeBlockEntity extends ChestBlockEntity implements GeoBlockEntity {
   public static final int CONTAINER_SIZE = 54;
   private static final String OWNER_TAG = PomkotsMechs.nbtName("PersonalPomkotsCubeOwner");
   private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
   private NonNullList<ItemStack> items = NonNullList.m_122780_(54, ItemStack.f_41583_);
   private UUID ownerUuid;
   private float previousOpenness;

   public PersonalPomkotsCubeBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(), pos, state);
   }

   protected PersonalPomkotsCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public int m_6643_() {
      return 54;
   }

   protected NonNullList<ItemStack> m_7086_() {
      return this.items;
   }

   protected void m_6520_(NonNullList<ItemStack> items) {
      this.items = items;
   }

   public boolean isOwner(Player player) {
      return this.ownerUuid != null && this.ownerUuid.equals(player.m_20148_());
   }

   public void setOwnerIfAbsent(UUID ownerUuid) {
      if (this.ownerUuid == null) {
         this.ownerUuid = ownerUuid;
         this.m_6596_();
      }
   }

   protected Component m_6820_() {
      return Component.m_237115_("block.pomkotsmechs.personal_pomkots_cube");
   }

   @Nullable
   public AbstractContainerMenu m_7208_(int id, Inventory inventory, Player player) {
      if (this.isOwner(player) && this.m_7525_(player)) {
         this.m_59640_(player);
         return ChestMenu.m_39246_(id, inventory, this);
      } else {
         return null;
      }
   }

   protected void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      ContainerHelper.m_18973_(tag, this.items);
      if (this.ownerUuid != null) {
         tag.m_128362_(OWNER_TAG, this.ownerUuid);
      }
   }

   public void m_142466_(@NotNull CompoundTag tag) {
      this.items = NonNullList.m_122780_(54, ItemStack.f_41583_);
      super.m_142466_(tag);
      ContainerHelper.m_18980_(tag, this.items);
      this.ownerUuid = tag.m_128403_(OWNER_TAG) ? tag.m_128342_(OWNER_TAG) : null;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               event -> {
                  float openness = ((PersonalPomkotsCubeBlockEntity)event.getAnimatable()).m_6683_(event.getPartialTick());
                  float oldOpenness = this.previousOpenness;
                  this.previousOpenness = openness;
                  if (oldOpenness == 0.0F && openness > 0.0F) {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.open"));
                  } else {
                     return oldOpenness == 1.0F && openness < 1.0F
                        ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.close"))
                        : PlayState.CONTINUE;
                  }
               }
            )
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animationCache;
   }
}
