package grcmcs.minecraft.mods.pomkotsmechs.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTags {
   public static final TagKey<EntityType<?>> SCAN_TARGETS = TagKey.m_203882_(Registries.f_256939_, new ResourceLocation("pomkotsmechs", "scan_targets"));

   private ModEntityTags() {
   }
}
