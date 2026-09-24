package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

import java.util.Locale;

public enum MechFeatureType {

    /**
     * ターゲットアシスト。
     * 例: 近い敵へ照準補正、ロック補助など。
     */
    TARGET_ASSIST_SOFT,
    TARGET_ASSIST_HARD,

    /**
     * 壁蹴り / 壁ジャンプ / 壁張り付きなど。
     */
    WALL_KICK,

    /** Allows an arm weapon to be carried in each shoulder slot and swapped in combat. */
    ARM_WEAPON_RACK,

    /** Demo feature: prevents Super Boost from consuming energy. */
    SUPER_BOOST_ENERGY_FREE;

    public static MechFeatureType byName(
            String name,
            MechFeatureType fallback
    ) {
        if (name == null) {
            return fallback;
        }

        String normalized =
                name.toUpperCase(Locale.ROOT);

        for (MechFeatureType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        return fallback;
    }
}
