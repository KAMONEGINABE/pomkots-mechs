package grcmcs.minecraft.mods.pomkotsmechs.util;

import java.io.InputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
   private static final byte[] KEY = buildKey();

   private static byte[] buildKey() {
      byte[] k = new byte[]{72, 110, 12, 34, -64, -26, -124, -86, 75, 120, 105, 30, 15, 60, 45, -46};

      for (int i = 0; i < k.length; i++) {
         k[i] = (byte)(k[i] ^ 90);
      }

      return k;
   }

   public static InputStream decrypt(InputStream input) {
      try {
         byte[] iv = input.readNBytes(16);
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
         IvParameterSpec ivSpec = new IvParameterSpec(iv);
         cipher.init(2, keySpec, ivSpec);
         return new CipherInputStream(input, cipher);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
}
