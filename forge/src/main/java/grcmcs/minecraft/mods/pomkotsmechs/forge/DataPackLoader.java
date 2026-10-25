package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "pomkotsmechs", bus = Bus.FORGE)
public class DataPackLoader {
   @SubscribeEvent
   public static void onReload(AddReloadListenerEvent event) {
      event.addListener(new SimplePreparableReloadListener<Void>() {
         protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
            PomkotsMechs.loadDataPack(manager);
            return null;
         }

         protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
         }
      });
   }
}
