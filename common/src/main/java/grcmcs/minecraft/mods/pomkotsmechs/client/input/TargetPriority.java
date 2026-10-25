package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TargetPriority {
   private final double distanceWeight;
   private final double angleWeight;
   private final double sizeWeight;

   public TargetPriority(double distanceWeight, double angleWeight, double sizeWeight) {
      this.distanceWeight = distanceWeight;
      this.angleWeight = angleWeight;
      this.sizeWeight = sizeWeight;
   }

   public double calculatePriority(Player player, Entity target) {
      Vec3 eyePos = player.m_146892_();
      Vec3 lookDirection = player.m_20154_();
      double distance = eyePos.m_82554_(target.m_20182_());
      double distanceScore = Math.max(0.0, 1.0 - distance / 150.0);
      Vec3 toTarget = target.m_20182_().m_82546_(eyePos).m_82541_();
      double dot = lookDirection.m_82526_(toTarget);
      double angleScore = Math.max(0.0, dot);
      AABB bounds = target.m_20191_();
      double size = bounds.m_82362_() * bounds.m_82376_() * bounds.m_82385_();
      double sizeScore = Math.min(1.0, size / 8.0);
      return distanceScore * this.distanceWeight + angleScore * this.angleWeight + sizeScore * this.sizeWeight;
   }
}
