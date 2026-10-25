package grcmcs.minecraft.mods.pomkotsmechs.forge.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.Optional;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.gui.LostCitySetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.worldgen.LostCityFeature;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState.WorldTypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
   @Unique
   private String pomkots$lastPreset = "UNKNOWN";
   private static final String SEED_VALUE = "-7229487307260543510";
   private static final ResourceLocation CUSTOM_PRESET_LOC = new ResourceLocation("pomkotsmechs", "pomkots_world");
   private static final ResourceKey<WorldPreset> CUSTOM_PRESET_KEY = ResourceKey.m_135785_(Registries.f_256729_, CUSTOM_PRESET_LOC);
   private static final String SOURCE_PROFILE_NAME = "00_pomkots_world";
   private static final String CUSTOMIZED_PROFILE_NAME = "customized";

   @ModifyArg(
      method = "openFresh",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContext;Ljava/util/Optional;Ljava/util/OptionalLong;)V"
      ),
      index = 3
   )
   private static Optional<ResourceKey<WorldPreset>> modifyDefaultPreset(Optional<ResourceKey<WorldPreset>> original) {
      Optional<ResourceKey<WorldPreset>> result = original;
      if (ModList.get().isLoaded("lostcities") && PomkotsMechs.CONFIG.survivalModeEnabled) {
         result = Optional.of(CUSTOM_PRESET_KEY);
      }

      return result;
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void onInit(CallbackInfo ci) {
      if (ModList.get().isLoaded("lostcities") && PomkotsMechs.CONFIG.survivalModeEnabled) {
         CreateWorldScreen self = (CreateWorldScreen)this;
         WorldCreationUiState uiState = self.m_267748_();
         uiState.m_267755_(state -> {
            String currentPreset = "";
            if (state.m_267828_() != null && state.m_267828_().f_267398_() != null) {
               currentPreset = state.m_267828_().f_267398_().m_203543_().map(key -> key.m_135782_().toString()).orElse("");
            }

            if (!currentPreset.equals(this.pomkots$lastPreset)) {
               this.pomkots$lastPreset = currentPreset;
               if (CUSTOM_PRESET_KEY.m_135782_().toString().equals(currentPreset)) {
                  if (!"-7229487307260543510".equals(state.m_267707_())) {
                     state.m_267759_("-7229487307260543510");
                  }

                  BooleanValue keepInv = (BooleanValue)state.m_267721_().m_46170_(GameRules.f_46133_);
                  if (keepInv != null && !keepInv.m_46223_()) {
                     keepInv.m_46246_(true, null);
                  }

                  LostCityProfile sourceProfile = (LostCityProfile)ProfileSetup.STANDARD_PROFILES.get("00_pomkots_world");
                  if (sourceProfile != null) {
                     LostCityProfile customizedProfile = new LostCityProfile("customized", false);
                     ProfileSetup.STANDARD_PROFILES.put("customized", customizedProfile);
                     customizedProfile.copyFrom(sourceProfile);
                     LostCitySetup.CLIENT_SETUP.setProfile("customized");
                     GuiLCConfig.selectProfile("customized", customizedProfile);
                     LostCityFeature.globalDimensionInfoDirtyCounter++;
                     Config.resetProfileCache();
                  } else {
                     Config.profileFromClient = "00_pomkots_world";
                  }
               } else {
                  if ("-7229487307260543510".equals(state.m_267707_())) {
                     state.m_267759_("");
                  }

                  BooleanValue keepInv = (BooleanValue)state.m_267721_().m_46170_(GameRules.f_46133_);
                  if (keepInv != null && keepInv.m_46223_()) {
                     keepInv.m_46246_(false, null);
                  }

                  GuiLCConfig.selectProfile(null, null);
                  LostCitySetup.CLIENT_SETUP.setProfile(null);
                  Config.resetProfileCache();
               }
            }
         });
         Registry<WorldPreset> presetRegistry = uiState.m_267573_().m_246480_().m_175515_(Registries.f_256729_);
         presetRegistry.m_203636_(CUSTOM_PRESET_KEY).ifPresent(holder -> uiState.m_267576_(new WorldTypeEntry(holder)));
      }
   }

   static {
      PomkotsMechs.LOGGER.info("MIXIN CreateWorldScreen LOADED");
   }
}
