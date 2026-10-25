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

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains(PomkotsMechs.nbtName("StructureId"))) {
         this.structureId = tag.getString(PomkotsMechs.nbtName("StructureId"));
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString(PomkotsMechs.nbtName("StructureId"), this.structureId);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, StructureSpawnerBlockEntity be) {
      if (!level.isClientSide && !be.generated && !"".equals(be.structureId) && !Utils.isMapEditingMode(level)) {
         ServerLevel serverLevel = (ServerLevel)level;
         StructureTemplateManager manager = serverLevel.getStructureManager();
         Optional<StructureTemplate> templateOpt = manager.get(new ResourceLocation(be.structureId));
         if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(false);
            template.placeInWorld(serverLevel, pos, pos, settings, serverLevel.random, 2);
         }

         be.generated = true;
      }
   }
}
