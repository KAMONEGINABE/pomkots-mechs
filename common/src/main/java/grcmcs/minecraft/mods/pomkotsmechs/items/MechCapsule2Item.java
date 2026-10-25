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

   public InteractionResult m_6880_(ItemStack ignoredStack, Player player, LivingEntity target, InteractionHand hand) {
      if (target instanceof Pmvc01Entity mech) {
         ItemStack handStack = player.m_21120_(hand);
         if (hasStoredMech(handStack)) {
            return InteractionResult.PASS;
         }

         if (!player.m_9236_().f_46443_) {
            CompoundTag storedMech = MechCapsuleData.capture(mech);
            ItemStack savedCloner = handStack.m_41777_();
            savedCloner.m_41764_(1);
            savedCloner.m_41784_().m_128365_("StoredMech", storedMech.m_6426_());
            if (handStack.m_41613_() > 1) {
               ItemStack remainingEmptyCloners = handStack.m_41777_();
               remainingEmptyCloners.m_41774_(1);
               player.m_21008_(hand, remainingEmptyCloners);
               if (!player.m_150109_().m_36054_(savedCloner)) {
                  player.m_36176_(savedCloner, false);
               }
            } else {
               player.m_21008_(hand, savedCloner);
            }

            target.m_146870_();
            player.m_150109_().m_6596_();
            if (player instanceof ServerPlayer serverPlayer) {
               serverPlayer.f_36096_.m_38946_();
            }
         }

         return InteractionResult.m_19078_(player.m_9236_().f_46443_);
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!hasStoredMech(stack)) {
         return InteractionResultHolder.m_19098_(stack);
      }

      if (player.m_19907_(20.0, 0.0F, false) instanceof BlockHitResult blockHitResult) {
         if (level.f_46443_) {
            return InteractionResultHolder.m_19092_(stack, true);
         }

         if (level instanceof ServerLevel serverLevel) {
            Vec3 hitPos = blockHitResult.m_82450_();
            Vec3 spawnPos = hitPos.m_82549_(Vec3.m_82528_(blockHitResult.m_82434_().m_122436_()).m_82490_(0.05));
            double dx = player.m_20185_() - spawnPos.f_82479_;
            double dz = player.m_20189_() - spawnPos.f_82481_;
            float spawnYaw = (float)(Mth.m_14136_(dz, dx) * 180.0F / (float)Math.PI) - 90.0F;
            CompoundTag storedMech = getStoredMech(stack);
            Optional<Entity> spawned = MechCapsuleData.spawn(serverLevel, storedMech, spawnPos, spawnYaw);
            if (spawned.isEmpty()) {
               return InteractionResultHolder.m_19100_(stack);
            }

            stack.m_41774_(1);
            player.m_36335_().m_41524_(this, 10);
            return InteractionResultHolder.m_19090_(stack);
         } else {
            return InteractionResultHolder.m_19100_(stack);
         }
      } else {
         return InteractionResultHolder.m_19098_(stack);
      }
   }

   public Component m_7626_(ItemStack stack) {
      Component baseName = super.m_7626_(stack);
      return (Component)(!hasStoredMech(stack)
         ? baseName
         : Component.m_237119_().m_7220_(baseName).m_7220_(Component.m_237113_(" (Saved)").m_130940_(ChatFormatting.GREEN)));
   }

   private void spawnStoredMech(ServerLevel level, ItemStack capsuleStack, Vec3 spawnPosition) {
      if (hasStoredMech(capsuleStack)) {
         CompoundTag storedMech = getStoredMech(capsuleStack);
         MechCapsuleData.spawn(level, storedMech, spawnPosition, 0.0F).ifPresent(spawned -> capsuleStack.m_41774_(1));
      }
   }

   public static boolean hasStoredMech(ItemStack stack) {
      CompoundTag root = stack.m_41783_();
      return root != null && root.m_128425_("StoredMech", 10);
   }

   public static CompoundTag getStoredMech(ItemStack stack) {
      CompoundTag root = stack.m_41783_();
      return root == null ? new CompoundTag() : root.m_128469_("StoredMech");
   }

   public static void clearStoredMech(ItemStack stack) {
      CompoundTag root = stack.m_41783_();
      if (root != null) {
         root.m_128473_("StoredMech");
         if (root.m_128456_()) {
            stack.m_41751_(null);
         }
      }
   }

   public static void saveMechToCard(Pmvc01Entity mech, ItemStack cardItem) {
      if (mech != null && !cardItem.m_41619_()) {
         CompoundTag cardTag = cardItem.m_41784_();
         ListTag itemList = new ListTag();

         for (int i = 0; i < mech.m_6643_(); i++) {
            ItemStack stack = mech.m_8020_(i);
            if (!stack.m_41619_()) {
               CompoundTag stackTag = new CompoundTag();
               stack.m_41739_(stackTag);
               stackTag.m_128344_("Slot", (byte)i);
               itemList.add(stackTag);
            }
         }

         cardTag.m_128365_("Items", itemList);
         cardTag.m_128359_("StoredMechName", mech.m_5446_().getString());
         cardTag.m_128362_("StoredMechUUID", mech.m_20148_());
         cardItem.m_41751_(cardTag);
         cardItem.m_41739_(cardTag);
      }
   }

   public static void loadMechFromCard(ItemStack cardItem, Player player, Level level) {
      Pmvc01Entity mech = summonRobot(player, level);
      if (!cardItem.m_41619_() && mech != null) {
         CompoundTag cardTag = cardItem.m_41783_();
         if (cardTag != null && cardTag.m_128425_("Items", 9)) {
            ListTag itemList = cardTag.m_128437_("Items", 10);

            for (int i = 0; i < mech.m_6643_(); i++) {
               mech.m_6836_(i, ItemStack.f_41583_);
            }

            for (Tag base : itemList) {
               CompoundTag stackTag = (CompoundTag)base;
               int slot = stackTag.m_128445_("Slot") & 255;
               ItemStack stack = ItemStack.m_41712_(stackTag);
               if (!stack.m_41619_() && slot < mech.m_6643_()) {
                  mech.m_6836_(slot, stack);
               }
            }

            mech.m_6596_();
         }
      }
   }

   private static Pmvc01Entity summonRobot(Player player, Level world) {
      Pmvc01Entity robot = new Pmvc01Entity((EntityType<? extends LivingEntity>)PomkotsMechs.PMVC01.get(), world);
      robot.m_7678_(player.m_20185_(), player.m_20186_(), player.m_20189_(), player.m_146908_(), player.m_146909_());
      world.m_7967_(robot);
      return robot;
   }
}
