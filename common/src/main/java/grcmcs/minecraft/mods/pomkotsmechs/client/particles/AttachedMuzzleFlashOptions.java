package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.Locale;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public record AttachedMuzzleFlashOptions(int parentEntityId, int weaponPoint, float size, float localX, float localY, float localZ) implements ParticleOptions {
   public static final int WEAPON_POINT_RIGHT_ARM = 0;
   public static final int WEAPON_POINT_LEFT_ARM = 1;
   public static final int WEAPON_POINT_RIGHT_SHOULDER = 2;
   public static final int WEAPON_POINT_LEFT_SHOULDER = 3;
   public static final Codec<AttachedMuzzleFlashOptions> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.INT.fieldOf("parent_entity_id").forGetter(AttachedMuzzleFlashOptions::parentEntityId),
            Codec.INT.fieldOf("weapon_point").forGetter(AttachedMuzzleFlashOptions::weaponPoint),
            Codec.FLOAT.fieldOf("size").forGetter(AttachedMuzzleFlashOptions::size),
            Codec.FLOAT.fieldOf("local_x").forGetter(AttachedMuzzleFlashOptions::localX),
            Codec.FLOAT.fieldOf("local_y").forGetter(AttachedMuzzleFlashOptions::localY),
            Codec.FLOAT.fieldOf("local_z").forGetter(AttachedMuzzleFlashOptions::localZ)
         )
         .apply(instance, AttachedMuzzleFlashOptions::new)
   );
   public static final Deserializer<AttachedMuzzleFlashOptions> DESERIALIZER = new Deserializer<AttachedMuzzleFlashOptions>() {
      public AttachedMuzzleFlashOptions fromCommand(ParticleType<AttachedMuzzleFlashOptions> type, StringReader reader) throws CommandSyntaxException {
         reader.expect(' ');
         int parentEntityId = reader.readInt();
         reader.expect(' ');
         int weaponPoint = reader.readInt();
         reader.expect(' ');
         float size = reader.readFloat();
         reader.expect(' ');
         float localX = reader.readFloat();
         reader.expect(' ');
         float localY = reader.readFloat();
         reader.expect(' ');
         float localZ = reader.readFloat();
         return new AttachedMuzzleFlashOptions(parentEntityId, weaponPoint, size, localX, localY, localZ);
      }

      public AttachedMuzzleFlashOptions fromNetwork(ParticleType<AttachedMuzzleFlashOptions> type, FriendlyByteBuf buf) {
         return new AttachedMuzzleFlashOptions(buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
      }
   };

   public ParticleType<?> getType() {
      return (ParticleType<?>)PomkotsMechs.MUZZLE_FLASH.get();
   }

   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeVarInt(this.parentEntityId);
      buf.writeVarInt(this.weaponPoint);
      buf.writeFloat(this.size);
      buf.writeFloat(this.localX);
      buf.writeFloat(this.localY);
      buf.writeFloat(this.localZ);
   }

   public String writeToString() {
      return String.format(
         Locale.ROOT,
         "%s %d %d %.5f %.5f %.5f %.5f",
         BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()),
         this.parentEntityId,
         this.weaponPoint,
         this.size,
         this.localX,
         this.localY,
         this.localZ
      );
   }
}
