package grcmcs.minecraft.mods.pomkotsmechs.misc;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ExplosionNoDrop extends Explosion {
   private final Level level;
   private final double x;
   private final double y;
   private final double z;
   private final BlockInteraction blockInteraction;

   public ExplosionNoDrop(Level level, @Nullable Entity entity, double x, double y, double z, float scale, boolean fire, BlockInteraction blockInteraction) {
      super(level, entity, x, y, z, scale, fire, blockInteraction);
      this.level = level;
      this.x = x;
      this.y = y;
      this.z = z;
      this.blockInteraction = blockInteraction;
   }

   public void m_46075_(boolean bl) {
      if (this.level.f_46443_) {
         this.level
            .m_7785_(
               this.x,
               this.y,
               this.z,
               SoundEvents.f_11913_,
               SoundSource.BLOCKS,
               4.0F,
               (1.0F + (this.level.f_46441_.m_188501_() - this.level.f_46441_.m_188501_()) * 0.2F) * 0.7F,
               false
            );
      }

      boolean bl2 = this.m_254884_();
      if (bl) {
         if (!(this.f_46017_ < 2.0F) && bl2) {
            this.level.m_7106_(ParticleTypes.f_123812_, this.x, this.y, this.z, 1.0, 0.0, 0.0);
         } else {
            this.level.m_7106_(ParticleTypes.f_123813_, this.x, this.y, this.z, 1.0, 0.0, 0.0);
         }
      }

      if (bl2) {
         ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList();
         ObjectArrayList<BlockPos> toBlow = (ObjectArrayList<BlockPos>)this.m_46081_();
         Util.m_214673_(toBlow, this.level.f_46441_);
         ObjectListIterator var5 = toBlow.iterator();

         while (var5.hasNext()) {
            BlockPos blockPos = (BlockPos)var5.next();
            BlockState blockState = this.level.m_8055_(blockPos);
            Block block = blockState.m_60734_();
            if (!blockState.m_60795_()) {
               this.level.m_46473_().m_6180_("explosion_blocks");
               this.level.m_7731_(blockPos, Blocks.f_50016_.m_49966_(), 3);
               block.m_7592_(this.level, blockPos, this);
               this.level.m_46473_().m_7238_();
            }
         }

         var5 = objectArrayList.iterator();

         while (var5.hasNext()) {
            Pair<ItemStack, BlockPos> pair = (Pair<ItemStack, BlockPos>)var5.next();
            Block.m_49840_(this.level, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
         }
      }
   }
}
