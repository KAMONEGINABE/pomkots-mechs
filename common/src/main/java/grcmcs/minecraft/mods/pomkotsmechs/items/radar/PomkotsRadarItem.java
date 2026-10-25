package grcmcs.minecraft.mods.pomkotsmechs.items.radar;

import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class PomkotsRadarItem extends Item {
   private static final String NBT_TARGETS = "RadarTargets";
   private static final String NBT_SELECTED_IDX = "RadarSelectedIndex";

   public PomkotsRadarItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
      if (!level.m_5776_()) {
         player.m_5893_(new SimpleMenuProvider(RadarTargetSelectMenu::new, Component.m_237113_("Pomkots Radar")));
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   public static void addTarget(ItemStack stack, RadarTarget target) {
      List<RadarTarget> targets = getTargets(stack);

      for (int i = 0; i < targets.size(); i++) {
         RadarTarget t = targets.get(i);
         if (t.label().equals(target.label())) {
            setSelectedIndex(stack, i);
            return;
         }
      }

      targets.add(target);
      setTargets(stack, targets);
      setSelectedIndex(stack, targets.size() - 1);
   }

   public static List<RadarTarget> getTargets(ItemStack stack) {
      ListTag list = stack.m_41784_().m_128437_("RadarTargets", 10);
      List<RadarTarget> result = new ArrayList<>();

      for (Tag tag : list) {
         result.add(RadarTarget.fromNbt((CompoundTag)tag));
      }

      return result;
   }

   public static void setTargets(ItemStack stack, List<RadarTarget> targets) {
      ListTag list = new ListTag();

      for (RadarTarget t : targets) {
         list.add(RadarTarget.toNbt(t));
      }

      stack.m_41784_().m_128365_("RadarTargets", list);
   }

   public static int getSelectedIndex(ItemStack stack) {
      return stack.m_41784_().m_128451_("RadarSelectedIndex");
   }

   public static void setSelectedIndex(ItemStack stack, int index) {
      stack.m_41784_().m_128405_("RadarSelectedIndex", index);
   }

   public static Optional<RadarTarget> getActiveTarget(ItemStack stack) {
      List<RadarTarget> targets = getTargets(stack);
      if (targets.isEmpty()) {
         return Optional.empty();
      }

      int idx = Mth.m_14045_(getSelectedIndex(stack), 0, targets.size() - 1);
      return Optional.of(targets.get(idx));
   }

   public static void onServerTick(ServerPlayer player) {
      for (InteractionHand hand : InteractionHand.values()) {
         ItemStack stack = player.m_21120_(hand);
         if (stack.m_41720_() instanceof PomkotsRadarItem) {
            RadarTarget active = getActiveTarget(stack).orElse(null);
            if (active instanceof RadarTarget.EntityTarget et && et.uuid() == null && player.m_9236_().m_46467_() % 400L == 0L) {
               ResourceLocation playerDim = player.m_9236_().m_46472_().m_135782_();
               if (playerDim.equals(et.dimension())) {
                  resolveSelector(player, stack, et);
               }
            }
         }
      }
   }

   private static void resolveSelector(ServerPlayer player, ItemStack stack, RadarTarget.EntityTarget et) {
   }
}
