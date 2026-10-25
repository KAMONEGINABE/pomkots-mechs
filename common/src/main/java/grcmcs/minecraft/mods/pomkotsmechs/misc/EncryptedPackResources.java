package grcmcs.minecraft.mods.pomkotsmechs.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

public class EncryptedPackResources implements PackResources {
   private static final String TARGET_NAMESPACE = "pomkotsmechs";
   private static final String TARGET_PREFIX = "sounds/bgm/";
   private final PackResources delegate;

   public EncryptedPackResources(PackResources wrapped) {
      this.delegate = wrapped;
   }

   public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
      PomkotsMechs.LOGGER.info("listResources1 called: namespace=" + namespace + " path=" + path);
      this.delegate.listResources(type, namespace, path, (location, supplier) -> {
         if (location.getNamespace().startsWith("pomkotsmechs")) {
            System.out.println("list s called: " + location);
         }

         if (type == PackType.CLIENT_RESOURCES && location.getNamespace().equals("pomkotsmechs") && location.getPath().endsWith(".dat")) {
            System.out.println(this.delegate.getClass().getName());
            ResourceLocation fake = new ResourceLocation(location.getNamespace(), location.getPath().replace(".dat", ".ogg"));
            System.out.println("fake: " + fake.toString());
            output.accept(fake, (IoSupplier)() -> CryptoUtil.decrypt((InputStream)supplier.get()));
         } else {
            output.accept(location, supplier);
         }
      });
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... strings) {
      return this.delegate.getRootResource(strings);
   }

   public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
      return this.delegate.getResource(type, location);
   }

   public String packId() {
      return this.delegate.packId();
   }

   public boolean isBuiltin() {
      return this.delegate.isBuiltin();
   }

   public Set<String> getNamespaces(PackType type) {
      return this.delegate.getNamespaces(type);
   }

   public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
      return (T)this.delegate.getMetadataSection(serializer);
   }

   public void close() {
      this.delegate.close();
   }
}
