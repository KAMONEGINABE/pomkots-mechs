package grcmcs.minecraft.mods.pomkotsmechs.misc;

import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

public class EncryptedSoundPack implements PackResources {
   private static final String TARGET_NAMESPACE = "pomkotsmechs";
   private static final String TARGET_PREFIX = "sounds/bgm/";
   private final PackResources delegate;

   public EncryptedSoundPack(PackResources delegate) {
      this.delegate = delegate;
   }

   private boolean isTarget(PackType type, ResourceLocation loc) {
      return type == PackType.CLIENT_RESOURCES
         && loc.getNamespace().equals("pomkotsmechs")
         && loc.getPath().startsWith("sounds/bgm/")
         && loc.getPath().endsWith(".ogg");
   }

   public IoSupplier<InputStream> getResource2(PackType type, ResourceLocation location) {
      if (location.getNamespace().startsWith("pomkotsmechs")) {
         System.out.println("getResource called: " + location);
      }

      if (this.isTarget(type, location)) {
         ResourceLocation encrypted = new ResourceLocation(location.getNamespace(), location.getPath().replace(".ogg", ".dat"));
         IoSupplier<InputStream> raw = this.delegate.getResource(type, encrypted);
         if (raw != null) {
            return () -> {
               Path p = Path.of("debug.ogg");
               System.out.println(p.toAbsolutePath());
               InputStream in = CryptoUtil.decrypt((InputStream)raw.get());
               byte[] data = in.readAllBytes();
               Files.write(Path.of("debug.ogg"), data);
               return new ByteArrayInputStream(data);
            };
         }
      }

      return this.delegate.getResource(type, location);
   }

   public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
      this.delegate.listResources(type, namespace, path, (location, supplier) -> {
         if (location.getNamespace().startsWith("pomkotsmechs")) {
            System.out.println("list s called: " + location);
         }

         if (type == PackType.CLIENT_RESOURCES && location.getNamespace().equals("pomkotsmechs") && location.getPath().endsWith(".dat")) {
            ResourceLocation fake = new ResourceLocation(location.getNamespace(), location.getPath().replace(".dat", ".ogg"));
            if (location.getNamespace().startsWith("pomkotsmechs")) {
               System.out.println("fake: " + fake.toString());
            }

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
