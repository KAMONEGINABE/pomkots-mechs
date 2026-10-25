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

   public void m_8031_(PackType type, String namespace, String path, ResourceOutput output) {
      PomkotsMechs.LOGGER.info("listResources1 called: namespace=" + namespace + " path=" + path);
      this.delegate.m_8031_(type, namespace, path, (location, supplier) -> {
         if (location.m_135827_().startsWith("pomkotsmechs")) {
            System.out.println("list s called: " + location);
         }

         if (type == PackType.CLIENT_RESOURCES && location.m_135827_().equals("pomkotsmechs") && location.m_135815_().endsWith(".dat")) {
            System.out.println(this.delegate.getClass().getName());
            ResourceLocation fake = new ResourceLocation(location.m_135827_(), location.m_135815_().replace(".dat", ".ogg"));
            System.out.println("fake: " + fake.toString());
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
