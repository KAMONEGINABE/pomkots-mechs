package grcmcs.minecraft.mods.pomkotsmechs.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
   public static final TagKey<Block> SCAN_TARGETS = TagKey.create(Registries.BLOCK, new ResourceLocation("pomkotsmechs", "scan_targets"));

   private ModBlockTags() {
   }
}
