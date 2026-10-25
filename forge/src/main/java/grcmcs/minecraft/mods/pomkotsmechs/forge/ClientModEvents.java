package grcmcs.minecraft.mods.pomkotsmechs.forge;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechTraderScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.PartsWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaBattleResultScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadKeyCardScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PersonalPomkotsCubeRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubePurpleRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeRedRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeYellowRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsLeverRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.MechCapsule2Item;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemProperties;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "pomkotsmechs", bus = Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(
         () -> {
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.MECH_WORKBENCH_GUI.get(), MechWorkbenchScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.PARTS_WORKBENCH_GUI.get(), PartsWorkbenchScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.MECH_SALVAGER_GUI.get(), MechSalvagerScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.POMKOTS_RADAR_GUI.get(), RadarTargetSelectScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.MECH_TRADER_GUI.get(), MechTraderScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.PILOT_CONFIG_GUI.get(), PilotScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.ARENA_RECEPTIONIST.get(), ArenaReceptionistScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.ARENA_RESULT_MENU.get(), ArenaBattleResultScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.POMKOTS_DATAPAD_GUI.get(), DataPadScreen::new);
            MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.POMKOTS_DATAPAD_KEYCARD_GUI.get(), DataPadKeyCardScreen::new);
            BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), context -> new PomkotsCubeRenderer(context));
            BlockEntityRendererRegistry.register(
               (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW.get(), context -> new PomkotsCubeYellowRenderer(context)
            );
            BlockEntityRendererRegistry.register(
               (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), context -> new PomkotsCubeRedRenderer(context)
            );
            BlockEntityRendererRegistry.register(
               (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_PURPLE.get(), context -> new PomkotsCubePurpleRenderer(context)
            );
            BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(), PersonalPomkotsCubeRenderer::new);
            BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), context -> new PomkotsLeverRenderer(context));
            ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
               CompoundTag tag = stack.getTag();
               if (tag == null) {
                  return -1;
               } else if (tintIndex == 0 && tag.contains(PomkotsMechs.nbtName("PrimaryColor"))) {
                  return tag.getInt(PomkotsMechs.nbtName("PrimaryColor"));
               } else {
                  return tintIndex == 1 && tag.contains(PomkotsMechs.nbtName("SecondaryColor")) ? tag.getInt(PomkotsMechs.nbtName("SecondaryColor")) : -1;
               }
            }, new ItemLike[]{(ItemLike)PomkotsMechs.MECH_CAPSULE_ITEM.get()});
            ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
               if (!(stack.getItem() instanceof MechCapsule2Item)) {
                  return -1;
               } else if (tintIndex == 0) {
                  return -5395027;
               } else if (tintIndex == 1) {
                  return MechCapsule2Item.hasStoredMech(stack) ? -8519824 : -1;
               } else {
                  return -1;
               }
            }, new ItemLike[]{(ItemLike)PomkotsMechs.MECH_CAPSULE2_ITEM.get()});
            ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
               if (tintIndex != 0) {
                  return 16777215;
               }

               CircuitRarity rarity = CircuitItemStackHelper.getRarityOrDefault(stack);
               return rarity.tintColor();
            }, new ItemLike[]{(ItemLike)PomkotsMechs.CIRCUIT_BASE.get()});
            CircuitItemProperties.register();
         }
      );
   }

   @SubscribeEvent
   public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("hide_mech_hud", (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
         Minecraft mc = Minecraft.getInstance();
         Player player = mc.player;
         if (player != null && player.getVehicle() instanceof Pmvc01Entity) {
            gui.leftHeight = 0;
            gui.rightHeight = 0;
         }
      });
   }
}
