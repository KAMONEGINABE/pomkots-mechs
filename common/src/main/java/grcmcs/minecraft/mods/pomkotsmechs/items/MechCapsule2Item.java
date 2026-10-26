package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MechCapsule2Item extends Item {
   public MechCapsule2Item(Properties properties) {
      super(properties);
   }

   public InteractionResult interactLivingEntity(ItemStack ignoredStack, Player player, LivingEntity target, InteractionHand hand) {
      if (target instanceof Pmvc01Entity mech) {
         ItemStack handStack = player.getItemInHand(hand);
         if (hasStoredMech(handStack)) {
            return InteractionResult.PASS;
         }

         if (!player.level().isClientSide) {
            CompoundTag storedMech = MechCapsuleData.capture(mech);
            ItemStack savedCloner = handStack.copy();
            savedCloner.setCount(1);
            savedCloner.getOrCreateTag().put("StoredMech", storedMech.copy());
            if (handStack.getCount() > 1) {
               ItemStack remainingEmptyCloners = handStack.copy();
               remainingEmptyCloners.shrink(1);
               player.setItemInHand(hand, remainingEmptyCloners);
               if (!player.getInventory().add(savedCloner)) {
                  player.drop(savedCloner, false);
               }
            } else {
               player.setItemInHand(hand, savedCloner);
            }

            target.discard();
            player.getInventory().setChanged();
            if (player instanceof ServerPlayer serverPlayer) {
               serverPlayer.containerMenu.broadcastChanges();
            }
         }

         return InteractionResult.sidedSuccess(player.level().isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!hasStoredMech(stack)) {
         return InteractionResultHolder.pass(stack);
      }

      if (player.pick(20.0, 0.0F, false) instanceof BlockHitResult blockHitResult) {
         if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
         }

         if (level instanceof ServerLevel serverLevel) {
            Vec3 hitPos = blockHitResult.getLocation();
            Vec3 spawnPos = hitPos.add(Vec3.atLowerCornerOf(blockHitResult.getDirection().getNormal()).scale(0.05));
            double dx = player.getX() - spawnPos.x;
            double dz = player.getZ() - spawnPos.z;
            float spawnYaw = (float)(Mth.atan2(dz, dx) * 180.0F / (float)Math.PI) - 90.0F;
            CompoundTag storedMech = getStoredMech(stack);
            Optional<Entity> spawned = MechCapsuleData.spawn(serverLevel, storedMech, spawnPos, spawnYaw);
            if (spawned.isEmpty()) {
               return InteractionResultHolder.fail(stack);
            }

            stack.shrink(1);
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.success(stack);
         } else {
            return InteractionResultHolder.fail(stack);
         }
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public Component getName(ItemStack stack) {
      Component baseName = super.getName(stack);
      if (!hasStoredMech(stack)) {
         return baseName;
      }
      CompoundTag stored = getStoredMech(stack);
      if (stored.contains("IsFrame") && stored.getBoolean("IsFrame")) {
         return Component.literal("Mech Capsule (Frame)");
      }
      return Component.empty().append(baseName).append(Component.literal(" (Saved)").withStyle(ChatFormatting.GREEN));
   }


   private void spawnStoredMech(ServerLevel level, ItemStack capsuleStack, Vec3 spawnPosition) {
      if (hasStoredMech(capsuleStack)) {
         CompoundTag storedMech = getStoredMech(capsuleStack);
         MechCapsuleData.spawn(level, storedMech, spawnPosition, 0.0F).ifPresent(spawned -> capsuleStack.shrink(1));
      }
   }

   public static boolean hasStoredMech(ItemStack stack) {
      CompoundTag root = stack.getTag();
      return root != null && root.contains("StoredMech", 10);
   }

   public static CompoundTag getStoredMech(ItemStack stack) {
      CompoundTag root = stack.getTag();
      return root == null ? new CompoundTag() : root.getCompound("StoredMech");
   }

   public static void clearStoredMech(ItemStack stack) {
      CompoundTag root = stack.getTag();
      if (root != null) {
         root.remove("StoredMech");
         if (root.isEmpty()) {
            stack.setTag(null);
         }
      }
   }

   public static void saveMechToCard(Pmvc01Entity mech, ItemStack cardItem) {
      if (mech != null && !cardItem.isEmpty()) {
         CompoundTag cardTag = cardItem.getOrCreateTag();
         ListTag itemList = new ListTag();

         for (int i = 0; i < mech.getContainerSize(); i++) {
            ItemStack stack = mech.getItem(i);
            if (!stack.isEmpty()) {
               CompoundTag stackTag = new CompoundTag();
               stack.save(stackTag);
               stackTag.putByte("Slot", (byte)i);
               itemList.add(stackTag);
            }
         }

         cardTag.put("Items", itemList);
         cardTag.putString("StoredMechName", mech.getDisplayName().getString());
         cardTag.putUUID("StoredMechUUID", mech.getUUID());
         cardItem.setTag(cardTag);
         cardItem.save(cardTag);
      }
   }

   public static void loadMechFromCard(ItemStack cardItem, Player player, Level level) {
      Pmvc01Entity mech = summonRobot(player, level);
      if (!cardItem.isEmpty() && mech != null) {
         CompoundTag cardTag = cardItem.getTag();
         if (cardTag != null && cardTag.contains("Items", 9)) {
            ListTag itemList = cardTag.getList("Items", 10);

            for (int i = 0; i < mech.getContainerSize(); i++) {
               mech.setItem(i, ItemStack.EMPTY);
            }

            for (Tag base : itemList) {
               CompoundTag stackTag = (CompoundTag)base;
               int slot = stackTag.getByte("Slot") & 255;
               ItemStack stack = ItemStack.of(stackTag);
               if (!stack.isEmpty() && slot < mech.getContainerSize()) {
                  mech.setItem(slot, stack);
               }
            }

            mech.setChanged();
         }
      }
   }

   private static Pmvc01Entity summonRobot(Player player, Level world) {
      Pmvc01Entity robot = new Pmvc01Entity((EntityType<? extends LivingEntity>)PomkotsMechs.PMVC01.get(), world);
      robot.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
      world.addFreshEntity(robot);
      return robot;
   }
}
