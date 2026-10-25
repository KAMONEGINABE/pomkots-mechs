package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureSpawnerBlockEntity extends BlockEntity {
   private boolean generated = false;
   private String structureId = "";

   public StructureSpawnerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.STRUCTURE_SPAWNER_BLOCK_ENTITY.get(), pos, state);
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      if (tag.m_128441_(PomkotsMechs.nbtName("StructureId"))) {
         this.structureId = tag.m_128461_(PomkotsMechs.nbtName("StructureId"));
      }
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128359_(PomkotsMechs.nbtName("StructureId"), this.structureId);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, StructureSpawnerBlockEntity be) {
      if (!level.f_46443_ && !be.generated && !"".equals(be.structureId) && !Utils.isMapEditingMode(level)) {
         ServerLevel serverLevel = (ServerLevel)level;
         StructureTemplateManager manager = serverLevel.m_215082_();
         Optional<StructureTemplate> templateOpt = manager.m_230407_(new ResourceLocation(be.structureId));
         if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings().m_74377_(Mirror.NONE).m_74379_(Rotation.NONE).m_74392_(false);
            template.m_230328_(serverLevel, pos, pos, settings, serverLevel.f_46441_, 2);
         }

         be.generated = true;
      }
   }
}
