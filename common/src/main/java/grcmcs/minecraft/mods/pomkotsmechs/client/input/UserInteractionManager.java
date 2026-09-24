package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class UserInteractionManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public enum Keys {
        // キーバインドに登録したくないやつは仮にDELETEキーにマップしとく（もっとかっこいい方法考える）
        FORWARD            ((short)0b0000000000000001, GLFW.GLFW_KEY_DELETE, "none"),
        BACK               ((short)0b0000000000000010, GLFW.GLFW_KEY_DELETE, "none"),
        LEFT               ((short)0b0000000000000100, GLFW.GLFW_KEY_DELETE, "none"),
        RIGHT              ((short)0b0000000000001000, GLFW.GLFW_KEY_DELETE, "none"),
        EVASION            ((short)0b0000000000010000, InputConstants.KEY_L, "evasion"),
        JUMP               ((short)0b0000000000100000, InputConstants.KEY_B, "jump"),
        WEAPON_ARM_R       ((short)0b0000000001000000, InputConstants.KEY_J, "weapon_arm_r"),
        WEAPON_ARM_L       ((short)0b0000000010000000, InputConstants.KEY_K, "weapon_arm_l"),
//        WEAPON_ARM_R       ((short)0b0000000001000000, InputConstants.MOUSE_BUTTON_RIGHT, "weapon_arm_r"),
//        WEAPON_ARM_L       ((short)0b0000000010000000, InputConstants.MOUSE_BUTTON_LEFT, "weapon_arm_l"),
        WEAPON_SHOULDER_R  ((short)0b0000000100000000, InputConstants.KEY_P, "weapon_shld_r"),
        WEAPON_SHOULDER_L  ((short)0b0000001000000000, InputConstants.KEY_O, "weapon_shld_l"),
        LOCK               ((short)0b0000010000000000, InputConstants.KEY_U, "lock"),
        MODE               ((short)0b0000100000000000, InputConstants.KEY_Y, "mode"),
        EXT1               ((short)0b0001000000000000, InputConstants.KEY_N, "ext1"),
        EXT2               ((short)0b0010000000000000, InputConstants.KEY_M, "ext2"),
        RELOAD             ((short)0b0100000000000000, InputConstants.KEY_H, "reload"),
        REPAIR             ((short)0b1000000000000000, InputConstants.KEY_COMMA, "repair");

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

    private final TargetLocker targetLocker = TargetLocker.getInstance();
    private short prevDriverInput = 0;
    private byte prevFlightEvasionDirection = 0;
    private final float[] lastSentAimPitch = {
            Float.NaN, Float.NaN, Float.NaN, Float.NaN
    };
    private long lastAimPitchSendTick = Long.MIN_VALUE;
    private static final int AIM_PITCH_SYNC_INTERVAL_TICKS = 3;
    private static final float AIM_PITCH_SYNC_THRESHOLD_DEGREES = 0.5F;

    public UserInteractionManager() {
    }

    public void registerClient() {
        // 左クリックでの攻撃抑制
        PlayerEvent.ATTACK_ENTITY.register((player, world, hand, entity, hitResult) -> {
            if (Utils.isRidingPomkotsControllable(player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        Map<Keys, KeyMapping> kbMap = new HashMap<>();

        for (Keys key: Keys.values()) {
            if (key.getId() != GLFW.GLFW_KEY_DELETE) {
                KeyMapping kb = new KeyMapping(
                    key.getConfName(),
                    InputConstants.Type.KEYSYM,
                    key.getId(),
                    "Pomkots-Bots");
                kbMap.put(key, kb);
                KeyMappingRegistry.register(kb);
            }
        }

        // クライアントのTickの最終段に、キー押下を検出してクラサバ両方にメッセージを送る処理を追加
        ClientTickEvent.CLIENT_POST.register(client -> {
            short keyPressStatus = 0;
            if (client.player != null) {
                if (Utils.isRidingPomkotsControllable(client.player)) {
                    for (Map.Entry<Keys, KeyMapping> entry : kbMap.entrySet()) {
                        if (entry.getKey() == Keys.LOCK || entry.getKey() == Keys.MODE) {
                            if (entry.getValue().consumeClick()) {
                                keyPressStatus |= entry.getKey().keyID;
                            }
                        } else {
                            if (entry.getValue().isDown()) {
                                keyPressStatus |= entry.getKey().keyID;
                            }
                        }
                    }

                    if (client.player.input.up) {
                        keyPressStatus |= Keys.FORWARD.getKeyID();
                    }
                    if (client.player.input.down) {
                        keyPressStatus |= Keys.BACK.getKeyID();
                    }
                    if (client.player.input.right) {
                        keyPressStatus |= Keys.RIGHT.getKeyID();
                    }
                    if (client.player.input.left) {
                        keyPressStatus |= Keys.LEFT.getKeyID();
                    }
                    if (client.options.keyAttack.isDown()) {
                        keyPressStatus |= Keys.WEAPON_ARM_L.getKeyID();
                    }
                    if (client.options.keyUse.isDown()) {
                        keyPressStatus |= Keys.WEAPON_ARM_R.getKeyID();
                    }
                    if (client.options.keyJump.isDown()) {
                        keyPressStatus |= Keys.JUMP.getKeyID();
                    }
                    if (client.options.keySprint.isDown()) {
                        keyPressStatus |= Keys.EVASION.getKeyID();
                    }

                    byte flightEvasionDirection = 0;
                    if (client.player.getVehicle() instanceof Pmvc01Entity mech
                            && mech.isFlightMode()
                            && (keyPressStatus & Keys.EVASION.getKeyID()) != 0) {
                        flightEvasionDirection = selectFlightEvasionDirection(
                                keyPressStatus, client.player.input.forwardImpulse,
                                client.player.input.leftImpulse, prevFlightEvasionDirection);
                    }
                    if (this.prevDriverInput != keyPressStatus
                            || this.prevFlightEvasionDirection != flightEvasionDirection) {
                        sendDriverInput2Server(keyPressStatus, flightEvasionDirection);
                        this.prevDriverInput = keyPressStatus;
                        this.prevFlightEvasionDirection = flightEvasionDirection;
                    }

                    if (client.player.getVehicle() instanceof PomkotsVehicle bot) {
                        var driverInput = new DriverInput(keyPressStatus);
                        targetLocker.tick(driverInput, bot);
                    }
                    if (client.player.getVehicle() instanceof Pmvc01Entity mech) {
                        sendAimPitchIfNeeded(mech);
                    }
                } else {
                    targetLocker.clearLockTargets();
                    prevDriverInput = 0;
                    prevFlightEvasionDirection = 0;
                    Arrays.fill(lastSentAimPitch, Float.NaN);
                }
            }
        });
    }

    private static byte selectFlightEvasionDirection(short keys, float forward, float left, byte previous) {
        int vertical = (keys & Keys.FORWARD.getKeyID()) != 0 ? 2
                : (keys & Keys.BACK.getKeyID()) != 0 ? -2 : 0;
        int horizontal = (keys & Keys.LEFT.getKeyID()) != 0 ? -1
                : (keys & Keys.RIGHT.getKeyID()) != 0 ? 1 : 0;
        if (vertical == 0) return (byte)(horizontal == 0 ? 3 : horizontal);
        if (horizontal == 0) return (byte)vertical;

        // Analog sticks often report both directions. Require a meaningful
        // difference before switching axes so small stick noise cannot flip a dodge.
        float difference = Math.abs(forward) - Math.abs(left);
        if (difference > 0.12F) return (byte)vertical;
        if (difference < -0.12F) return (byte)horizontal;
        if (previous == vertical || previous == horizontal) return previous;
        return (byte)vertical;
    }

    private void sendDriverInput2Server(short keyPressStatus, byte flightEvasionDirection) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeShort(keyPressStatus);
        buf.writeByte(flightEvasionDirection);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_DRIVER_INPUT), buf);

    }

    private void sendAimPitchIfNeeded(Pmvc01Entity mech) {
        long gameTime = mech.level().getGameTime();
        if (gameTime - lastAimPitchSendTick < AIM_PITCH_SYNC_INTERVAL_TICKS) return;

        float[] pitches = new float[4];
        boolean changed = false;
        for (int index = 0; index < pitches.length; index++) {
            int slot = Pmvc01Entity.INV_WEAPON_RIGHT_HAND + index;
            pitches[index] = mech.getWeaponAimPitch(slot);
            if (!Float.isFinite(lastSentAimPitch[index])
                    || Math.abs(pitches[index] - lastSentAimPitch[index]) >= AIM_PITCH_SYNC_THRESHOLD_DEGREES) {
                changed = true;
            }
        }
        if (!changed) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        for (float pitch : pitches) buf.writeFloat(pitch);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_MECH_AIM_PITCH), buf);
        System.arraycopy(pitches, 0, lastSentAimPitch, 0, pitches.length);
        lastAimPitchSendTick = gameTime;
    }
}
