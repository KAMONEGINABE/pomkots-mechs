package grcmcs.minecraft.mods.pomkotsmechs.items.radar;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

public sealed interface RadarTarget permits RadarTarget.EntityTarget, RadarTarget.CoordTarget {
   static RadarTarget fromNbt(CompoundTag tag) {
      String type = tag.m_128461_("type");
      String selector = tag.m_128461_("selector");
      String label = tag.m_128461_("label");
      String level = tag.m_128461_("level");

      return switch (type) {
         case "entity" -> new RadarTarget.EntityTarget(tag.m_128342_("uuid"), selector, label, new ResourceLocation(level));
         case "coord" -> new RadarTarget.CoordTarget(NbtUtils.m_129239_(tag.m_128469_("pos")), label, new ResourceLocation(level));
         default -> throw new IllegalArgumentException("Unknown target type: " + type);
      };
   }

   static CompoundTag toNbt(RadarTarget target) {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("label", target.label());
      tag.m_128359_("level", target.dimension().toString());
      if (target instanceof RadarTarget.EntityTarget e) {
         tag.m_128359_("type", "entity");
         tag.m_128362_("uuid", e.uuid());
         tag.m_128359_("selector", e.selector);
      } else if (target instanceof RadarTarget.CoordTarget c) {
         tag.m_128359_("type", "coord");
         tag.m_128365_("pos", NbtUtils.m_129224_(c.pos()));
      }

      return tag;
   }

   String label();

   ResourceLocation dimension();

   record CoordTarget(BlockPos pos, String label, ResourceLocation dimension) implements RadarTarget {
   }

   record EntityTarget(UUID uuid, String selector, String label, ResourceLocation dimension) implements RadarTarget {
   }
}
