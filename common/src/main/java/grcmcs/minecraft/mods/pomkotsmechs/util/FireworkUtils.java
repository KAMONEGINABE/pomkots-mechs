package grcmcs.minecraft.mods.pomkotsmechs.util;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkUtils {
   public static void launchRandomFirework(ServerLevel level, Vec3 pos) {
      ItemStack fireworkStack = createRandomFirework(level);
      FireworkRocketEntity firework = new FireworkRocketEntity(level, pos.f_82479_, pos.f_82480_, pos.f_82481_, fireworkStack);
      level.m_7967_(firework);
   }

   public static ItemStack createRandomFirework(Level level) {
      ItemStack stack = new ItemStack(Items.f_42688_);
      CompoundTag tag = stack.m_41698_("Fireworks");
      int flightDuration = 1 + level.f_46441_.m_188503_(3);
      tag.m_128344_("Flight", (byte)flightDuration);
      int explosionCount = 1 + level.f_46441_.m_188503_(3);
      ListTag explosions = new ListTag();

      for (int i = 0; i < explosionCount; i++) {
         explosions.add(createRandomExplosion(level.f_46441_));
      }

      tag.m_128365_("Explosions", explosions);
      return stack;
   }

   private static CompoundTag createRandomExplosion(RandomSource random) {
      CompoundTag explosion = new CompoundTag();
      int[] shapes = new int[]{0, 1, 2, 3, 4};
      explosion.m_128344_("Type", (byte)shapes[random.m_188503_(shapes.length)]);
      int colorCount = 1 + random.m_188503_(5);
      int[] colors = new int[colorCount];

      for (int i = 0; i < colorCount; i++) {
         colors[i] = getRandomFireworkColor(random);
      }

      explosion.m_128385_("Colors", colors);
      if (random.m_188499_()) {
         int fadeColorCount = 1 + random.m_188503_(3);
         int[] fadeColors = new int[fadeColorCount];

         for (int i = 0; i < fadeColorCount; i++) {
            fadeColors[i] = getRandomFireworkColor(random);
         }

         explosion.m_128385_("FadeColors", fadeColors);
      }

      if (random.m_188501_() < 0.3F) {
         explosion.m_128379_("Trail", true);
      }

      if (random.m_188501_() < 0.3F) {
         explosion.m_128379_("Flicker", true);
      }

      return explosion;
   }

   private static int getRandomFireworkColor(RandomSource random) {
      int[] commonColors = new int[]{16711680, 65280, 255, 16776960, 16711935, 65535, 16777215, 16746496, 8913151, 16711816};
      return commonColors[random.m_188503_(commonColors.length)];
   }

   public static void launchFireworkShow(ServerLevel level, Vec3 centerPos, int count, int delayTicks) {
      for (int i = 0; i < count; i++) {
         int index = i;
         level.m_7654_().execute(() -> scheduleFirework(level, centerPos, index * delayTicks));
      }
   }

   private static void scheduleFirework(ServerLevel level, Vec3 centerPos, int delayTicks) {
      MinecraftServer server = level.m_7654_();
      server.m_6937_(new TickTask(server.m_129921_() + delayTicks, () -> {
         double offsetX = (level.f_46441_.m_188500_() - 0.5) * 10.0;
         double offsetZ = (level.f_46441_.m_188500_() - 0.5) * 10.0;
         Vec3 pos = centerPos.m_82520_(offsetX, 0.0, offsetZ);
         FireworkUtils.FireworkTheme theme = FireworkUtils.FireworkTheme.values()[level.f_46441_.m_188503_(FireworkUtils.FireworkTheme.values().length)];
         launchThemedFirework(level, pos, theme);
      }));
   }

   public static void launchCircleFireworks(ServerLevel level, Vec3 centerPos, int count, double radius) {
      for (int i = 0; i < count; i++) {
         double angle = (Math.PI * 2) * i / count;
         double x = centerPos.f_82479_ + Math.cos(angle) * radius;
         double z = centerPos.f_82481_ + Math.sin(angle) * radius;
         Vec3 pos = new Vec3(x, centerPos.f_82480_, z);
         int delay = i * 2;
         level.m_7654_().m_6937_(new TickTask(level.m_7654_().m_129921_() + delay, () -> launchRandomFirework(level, pos)));
      }
   }

   public static void launchRandomAreaFireworks(ServerLevel level, Vec3 centerPos, double range, int count, int totalDurationTicks) {
      for (int i = 0; i < count; i++) {
         int delay = level.f_46441_.m_188503_(totalDurationTicks);
         level.m_7654_().m_6937_(new TickTask(level.m_7654_().m_129921_() + delay, () -> {
            double x = centerPos.f_82479_ + (level.f_46441_.m_188500_() - 0.5) * range * 2.0;
            double z = centerPos.f_82481_ + (level.f_46441_.m_188500_() - 0.5) * range * 2.0;
            launchRandomFirework(level, new Vec3(x, centerPos.f_82480_, z));
         }));
      }
   }

   public static void launchThemedFirework(ServerLevel level, Vec3 pos, FireworkUtils.FireworkTheme theme) {
      ItemStack fireworkStack = createThemedFirework(level.f_46441_, theme);
      FireworkRocketEntity firework = new FireworkRocketEntity(level, pos.f_82479_, pos.f_82480_, pos.f_82481_, fireworkStack);
      level.m_7967_(firework);
   }

   public static ItemStack createThemedFirework(RandomSource random, FireworkUtils.FireworkTheme theme) {
      ItemStack stack = new ItemStack(Items.f_42688_);
      CompoundTag tag = stack.m_41698_("Fireworks");
      tag.m_128344_("Flight", (byte)theme.flightDuration);
      ListTag explosions = new ListTag();

      for (int i = 0; i < theme.explosionCount; i++) {
         explosions.add(createThemedExplosion(random, theme));
      }

      tag.m_128365_("Explosions", explosions);
      return stack;
   }

   private static CompoundTag createThemedExplosion(RandomSource random, FireworkUtils.FireworkTheme theme) {
      CompoundTag explosion = new CompoundTag();
      explosion.m_128344_("Type", (byte)theme.shapes[random.m_188503_(theme.shapes.length)]);
      int colorCount = Math.min(theme.colors.length, 1 + random.m_188503_(3));
      int[] colors = new int[colorCount];

      for (int i = 0; i < colorCount; i++) {
         colors[i] = theme.colors[random.m_188503_(theme.colors.length)];
      }

      explosion.m_128385_("Colors", colors);
      if (theme.useFadeColors && random.m_188499_()) {
         int[] fadeColors = new int[1 + random.m_188503_(2)];

         for (int i = 0; i < fadeColors.length; i++) {
            fadeColors[i] = theme.fadeColors[random.m_188503_(theme.fadeColors.length)];
         }

         explosion.m_128385_("FadeColors", fadeColors);
      }

      explosion.m_128379_("Trail", theme.trail);
      explosion.m_128379_("Flicker", theme.flicker);
      return explosion;
   }

   public enum FireworkTheme {
      RANDOM(
         new int[]{0, 1, 2, 3, 4},
         new int[]{16711680, 65280, 255, 16776960, 16711935, 65535, 16777215},
         new int[]{16777215, 16776960, 16746496},
         true,
         false,
         false,
         2,
         1 + new Random().nextInt(3)
      ),
      CELEBRATION(new int[]{1, 4}, new int[]{16776960, 16746496, 16711680}, new int[]{16777215, 16777096}, true, true, true, 3, 2 + new Random().nextInt(2)),
      COOL(new int[]{0, 2}, new int[]{255, 65535, 8913151}, new int[]{16777215, 8978431}, true, false, true, 2, 1 + new Random().nextInt(2)),
      CREEPER(new int[]{3}, new int[]{65280}, new int[]{16777215}, false, false, false, 2, 1),
      RAINBOW(new int[]{1, 4}, new int[]{16711680, 16746496, 16776960, 65280, 255, 8913151}, new int[]{16777215}, true, true, false, 2, 3);

      final int[] shapes;
      final int[] colors;
      final int[] fadeColors;
      final boolean useFadeColors;
      final boolean trail;
      final boolean flicker;
      final int flightDuration;
      final int explosionCount;

      FireworkTheme(int[] shapes, int[] colors, int[] fadeColors, boolean useFadeColors, boolean trail, boolean flicker, int flightDuration, int explosionCount) {
         this.shapes = shapes;
         this.colors = colors;
         this.fadeColors = fadeColors;
         this.useFadeColors = useFadeColors;
         this.trail = trail;
         this.flicker = flicker;
         this.flightDuration = flightDuration;
         this.explosionCount = explosionCount;
      }
   }
}
