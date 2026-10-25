package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientTickEvent.Client;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.PlayerEvent.AttackEntity;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInteractionManager {
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   private final TargetLocker targetLocker = TargetLocker.getInstance();
   private short prevDriverInput = 0;

   public void registerClient() {
      PlayerEvent.ATTACK_ENTITY
         .register(
            (AttackEntity)(player, world, hand, entity, hitResult) -> Utils.isRidingPomkotsControllable(player)
               ? EventResult.interruptFalse()
               : EventResult.pass()
         );
      Map<UserInteractionManager.Keys, KeyMapping> kbMap = new HashMap<>();

      for (UserInteractionManager.Keys key : UserInteractionManager.Keys.values()) {
         if (key.getId() != 261) {
            KeyMapping kb = new KeyMapping(key.getConfName(), Type.KEYSYM, key.getId(), "Pomkots-Bots");
            kbMap.put(key, kb);
            KeyMappingRegistry.register(kb);
         }
      }

      ClientTickEvent.CLIENT_POST.register((Client)client -> {
         short keyPressStatus = 0;
         if (client.f_91074_ != null) {
            if (Utils.isRidingPomkotsControllable(client.f_91074_)) {
               for (Entry<UserInteractionManager.Keys, KeyMapping> entry : kbMap.entrySet()) {
                  if (entry.getKey() != UserInteractionManager.Keys.LOCK && entry.getKey() != UserInteractionManager.Keys.MODE) {
                     if (entry.getValue().m_90857_()) {
                        keyPressStatus |= entry.getKey().keyID;
                     }
                  } else if (entry.getValue().m_90859_()) {
                     keyPressStatus |= entry.getKey().keyID;
                  }
               }

               if (client.f_91074_.f_108618_.f_108568_) {
                  keyPressStatus |= UserInteractionManager.Keys.FORWARD.getKeyID();
               }

               if (client.f_91074_.f_108618_.f_108569_) {
                  keyPressStatus |= UserInteractionManager.Keys.BACK.getKeyID();
               }

               if (client.f_91074_.f_108618_.f_108571_) {
                  keyPressStatus |= UserInteractionManager.Keys.RIGHT.getKeyID();
               }

               if (client.f_91074_.f_108618_.f_108570_) {
                  keyPressStatus |= UserInteractionManager.Keys.LEFT.getKeyID();
               }

               if (client.f_91066_.f_92096_.m_90857_()) {
                  keyPressStatus |= UserInteractionManager.Keys.WEAPON_ARM_L.getKeyID();
               }

               if (client.f_91066_.f_92095_.m_90857_()) {
                  keyPressStatus |= UserInteractionManager.Keys.WEAPON_ARM_R.getKeyID();
               }

               if (client.f_91066_.f_92089_.m_90857_()) {
                  keyPressStatus |= UserInteractionManager.Keys.JUMP.getKeyID();
               }

               if (client.f_91066_.f_92091_.m_90857_()) {
                  keyPressStatus |= UserInteractionManager.Keys.EVASION.getKeyID();
               }

               if (this.prevDriverInput != keyPressStatus) {
                  this.sendDriverInput2Server(keyPressStatus);
                  this.prevDriverInput = keyPressStatus;
               }

               if (client.f_91074_.m_20202_() instanceof PomkotsVehicle bot) {
                  DriverInput driverInput = new DriverInput(keyPressStatus);
                  this.targetLocker.tick(driverInput, bot);
               }
            } else {
               this.targetLocker.clearLockTargets();
            }
         }
      });
   }

   private void sendDriverInput2Server(short keyPressStatus) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.writeShort(keyPressStatus);
      NetworkManager.sendToServer(PomkotsMechs.id("kpm"), buf);
   }

   public enum Keys {
      FORWARD((short)1, 261, "none"),
      BACK((short)2, 261, "none"),
      LEFT((short)4, 261, "none"),
      RIGHT((short)8, 261, "none"),
      EVASION((short)16, 76, "evasion"),
      JUMP((short)32, 66, "jump"),
      WEAPON_ARM_R((short)64, 74, "weapon_arm_r"),
      WEAPON_ARM_L((short)128, 75, "weapon_arm_l"),
      WEAPON_SHOULDER_R((short)256, 80, "weapon_shld_r"),
      WEAPON_SHOULDER_L((short)512, 79, "weapon_shld_l"),
      LOCK((short)1024, 85, "lock"),
      MODE((short)2048, 89, "mode"),
      EXT1((short)4096, 78, "ext1"),
      EXT2((short)8192, 77, "ext2"),
      RELOAD((short)16384, 72, "reload"),
      REPAIR((short)-32768, 44, "repair");

      private final short keyID;
      private final int id;
      private final String confName;

      Keys(final short keyID, final int id, final String confName) {
         this.keyID = keyID;
         this.id = id;
         this.confName = confName;
      }

      public short getKeyID() {
         return this.keyID;
      }

      public int getId() {
         return this.id;
      }

      public String getConfName() {
         return this.confName;
      }
   }
}
