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
      FireworkRocketEntity firework = new FireworkRocketEntity(level, pos.x, pos.y, pos.z, fireworkStack);
      level.addFreshEntity(firework);
   }

   public static ItemStack createRandomFirework(Level level) {
      ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
      CompoundTag tag = stack.getOrCreateTagElement("Fireworks");
      int flightDuration = 1 + level.random.nextInt(3);
      tag.putByte("Flight", (byte)flightDuration);
      int explosionCount = 1 + level.random.nextInt(3);
      ListTag explosions = new ListTag();

      for (int i = 0; i < explosionCount; i++) {
         explosions.add(createRandomExplosion(level.random));
      }

      tag.put("Explosions", explosions);
      return stack;
   }

   private static CompoundTag createRandomExplosion(RandomSource random) {
      CompoundTag explosion = new CompoundTag();
      int[] shapes = new int[]{0, 1, 2, 3, 4};
      explosion.putByte("Type", (byte)shapes[random.nextInt(shapes.length)]);
      int colorCount = 1 + random.nextInt(5);
      int[] colors = new int[colorCount];

      for (int i = 0; i < colorCount; i++) {
         colors[i] = getRandomFireworkColor(random);
      }

      explosion.putIntArray("Colors", colors);
      if (random.nextBoolean()) {
         int fadeColorCount = 1 + random.nextInt(3);
         int[] fadeColors = new int[fadeColorCount];

         for (int i = 0; i < fadeColorCount; i++) {
            fadeColors[i] = getRandomFireworkColor(random);
         }

         explosion.putIntArray("FadeColors", fadeColors);
      }

      if (random.nextFloat() < 0.3F) {
         explosion.putBoolean("Trail", true);
      }

      if (random.nextFloat() < 0.3F) {
         explosion.putBoolean("Flicker", true);
      }

      return explosion;
   }

   private static int getRandomFireworkColor(RandomSource random) {
      int[] commonColors = new int[]{16711680, 65280, 255, 16776960, 16711935, 65535, 16777215, 16746496, 8913151, 16711816};
      return commonColors[random.nextInt(commonColors.length)];
   }

   public static void launchFireworkShow(ServerLevel level, Vec3 centerPos, int count, int delayTicks) {
      for (int i = 0; i < count; i++) {
         int index = i;
         level.getServer().execute(() -> scheduleFirework(level, centerPos, index * delayTicks));
      }
   }

   private static void scheduleFirework(ServerLevel level, Vec3 centerPos, int delayTicks) {
      MinecraftServer server = level.getServer();
      server.tell(new TickTask(server.getTickCount() + delayTicks, () -> {
         double offsetX = (level.random.nextDouble() - 0.5) * 10.0;
         double offsetZ = (level.random.nextDouble() - 0.5) * 10.0;
         Vec3 pos = centerPos.add(offsetX, 0.0, offsetZ);
         FireworkUtils.FireworkTheme theme = FireworkUtils.FireworkTheme.values()[level.random.nextInt(FireworkUtils.FireworkTheme.values().length)];
         launchThemedFirework(level, pos, theme);
      }));
   }

   public static void launchCircleFireworks(ServerLevel level, Vec3 centerPos, int count, double radius) {
      for (int i = 0; i < count; i++) {
         double angle = (Math.PI * 2) * i / count;
         double x = centerPos.x + Math.cos(angle) * radius;
         double z = centerPos.z + Math.sin(angle) * radius;
         Vec3 pos = new Vec3(x, centerPos.y, z);
         int delay = i * 2;
         level.getServer().tell(new TickTask(level.getServer().getTickCount() + delay, () -> launchRandomFirework(level, pos)));
      }
   }

   public static void launchRandomAreaFireworks(ServerLevel level, Vec3 centerPos, double range, int count, int totalDurationTicks) {
      for (int i = 0; i < count; i++) {
         int delay = level.random.nextInt(totalDurationTicks);
         level.getServer().tell(new TickTask(level.getServer().getTickCount() + delay, () -> {
            double x = centerPos.x + (level.random.nextDouble() - 0.5) * range * 2.0;
            double z = centerPos.z + (level.random.nextDouble() - 0.5) * range * 2.0;
            launchRandomFirework(level, new Vec3(x, centerPos.y, z));
         }));
      }
   }

   public static void launchThemedFirework(ServerLevel level, Vec3 pos, FireworkUtils.FireworkTheme theme) {
      ItemStack fireworkStack = createThemedFirework(level.random, theme);
      FireworkRocketEntity firework = new FireworkRocketEntity(level, pos.x, pos.y, pos.z, fireworkStack);
      level.addFreshEntity(firework);
   }

   public static ItemStack createThemedFirework(RandomSource random, FireworkUtils.FireworkTheme theme) {
      ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
      CompoundTag tag = stack.getOrCreateTagElement("Fireworks");
      tag.putByte("Flight", (byte)theme.flightDuration);
      ListTag explosions = new ListTag();

      for (int i = 0; i < theme.explosionCount; i++) {
         explosions.add(createThemedExplosion(random, theme));
      }

      tag.put("Explosions", explosions);
      return stack;
   }

   private static CompoundTag createThemedExplosion(RandomSource random, FireworkUtils.FireworkTheme theme) {
      CompoundTag explosion = new CompoundTag();
      explosion.putByte("Type", (byte)theme.shapes[random.nextInt(theme.shapes.length)]);
      int colorCount = Math.min(theme.colors.length, 1 + random.nextInt(3));
      int[] colors = new int[colorCount];

      for (int i = 0; i < colorCount; i++) {
         colors[i] = theme.colors[random.nextInt(theme.colors.length)];
      }

      explosion.putIntArray("Colors", colors);
      if (theme.useFadeColors && random.nextBoolean()) {
         int[] fadeColors = new int[1 + random.nextInt(2)];

         for (int i = 0; i < fadeColors.length; i++) {
            fadeColors[i] = theme.fadeColors[random.nextInt(theme.fadeColors.length)];
         }

         explosion.putIntArray("FadeColors", fadeColors);
      }

      explosion.putBoolean("Trail", theme.trail);
      explosion.putBoolean("Flicker", theme.flicker);
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
