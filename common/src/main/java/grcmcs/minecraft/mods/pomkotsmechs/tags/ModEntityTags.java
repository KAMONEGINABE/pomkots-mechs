package grcmcs.minecraft.mods.pomkotsmechs.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTags {
   public static final TagKey<EntityType<?>> SCAN_TARGETS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("pomkotsmechs", "scan_targets"));

   private ModEntityTags() {
   }
}
