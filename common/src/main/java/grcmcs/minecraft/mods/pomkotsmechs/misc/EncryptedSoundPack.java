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
         && loc.m_135827_().equals("pomkotsmechs")
         && loc.m_135815_().startsWith("sounds/bgm/")
         && loc.m_135815_().endsWith(".ogg");
   }

   public IoSupplier<InputStream> getResource2(PackType type, ResourceLocation location) {
      if (location.m_135827_().startsWith("pomkotsmechs")) {
         System.out.println("getResource called: " + location);
      }

      if (this.isTarget(type, location)) {
         ResourceLocation encrypted = new ResourceLocation(location.m_135827_(), location.m_135815_().replace(".ogg", ".dat"));
         IoSupplier<InputStream> raw = this.delegate.m_214146_(type, encrypted);
         if (raw != null) {
            return () -> {
               Path p = Path.of("debug.ogg");
               System.out.println(p.toAbsolutePath());
               InputStream in = CryptoUtil.decrypt((InputStream)raw.m_247737_());
               byte[] data = in.readAllBytes();
               Files.write(Path.of("debug.ogg"), data);
               return new ByteArrayInputStream(data);
            };
         }
      }

      return this.delegate.m_214146_(type, location);
   }

   public void m_8031_(PackType type, String namespace, String path, ResourceOutput output) {
      this.delegate.m_8031_(type, namespace, path, (location, supplier) -> {
         if (location.m_135827_().startsWith("pomkotsmechs")) {
            System.out.println("list s called: " + location);
         }

         if (type == PackType.CLIENT_RESOURCES && location.m_135827_().equals("pomkotsmechs") && location.m_135815_().endsWith(".dat")) {
            ResourceLocation fake = new ResourceLocation(location.m_135827_(), location.m_135815_().replace(".dat", ".ogg"));
            if (location.m_135827_().startsWith("pomkotsmechs")) {
               System.out.println("fake: " + fake.toString());
            }

            output.accept(fake, (IoSupplier)() -> CryptoUtil.decrypt((InputStream)supplier.m_247737_()));
         } else {
            output.accept(location, supplier);
         }
      });
   }

   @Nullable
   public IoSupplier<InputStream> m_8017_(String... strings) {
      return this.delegate.m_8017_(strings);
   }

   public IoSupplier<InputStream> m_214146_(PackType type, ResourceLocation location) {
      return this.delegate.m_214146_(type, location);
   }

   public String m_5542_() {
      return this.delegate.m_5542_();
   }

   public boolean m_246538_() {
      return this.delegate.m_246538_();
   }

   public Set<String> m_5698_(PackType type) {
      return this.delegate.m_5698_(type);
   }

   public <T> T m_5550_(MetadataSectionSerializer<T> serializer) throws IOException {
      return (T)this.delegate.m_5550_(serializer);
   }

   public void close() {
      this.delegate.close();
   }
}
