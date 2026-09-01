package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.model.GeoModel;

public class Pmvc01EntityModel extends GeoModel<Pmvc01Entity> {
    public static float SHOULDER_AIM_PITCH_CALIBRATION_DEGREES = -15F;

    @Override
    public void setCustomAnimations(Pmvc01Entity animatable, long instanceId, AnimationState<Pmvc01Entity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        applyArmAimPitch(animatable, Pmvc01Entity.INV_WEAPON_RIGHT_HAND,
                "right_shoulder");
        applyArmAimPitch(animatable, Pmvc01Entity.INV_WEAPON_LEFT_HAND,
                "left_shoulder");
        applyShoulderAimPitch(animatable, Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER,
                "right_shoulder_hunger_rotate", "weapon_right_shoulder");
        applyShoulderAimPitch(animatable, Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER,
                "left_shoulder_hunger_rotate", "weapon_left_shoulder");
    }

    private void applyAimPitch(String boneName, float pitch, boolean active) {
        if (!active) return;
        CoreGeoBone bone = getAnimationProcessor().getBone(boneName);
        // This hook can run more than once per frame (for example Iris shadow
        // passes), so adding to the current rotation would accumulate forever.
        // Weapon animations use a forward-facing baseline while firing.
        if (bone != null) bone.setRotX(pitch);
    }

    private void applyArmAimPitch(Pmvc01Entity animatable, int inventorySlot,
                                  String boneName) {
        float pitch = -animatable.getVisualWeaponAimPitch(inventorySlot) * Mth.DEG_TO_RAD;
        applyAimPitch(boneName, pitch,
                animatable.isWeaponSlotInAction(inventorySlot)
                        && animatable.isWeaponSlotArmPitchAimingEnabled(inventorySlot));
    }

    private void applyShoulderAimPitch(Pmvc01Entity animatable, int inventorySlot,
                                       String hangerBoneName, String weaponBoneName) {
        if (!animatable.isWeaponSlotInAction(inventorySlot)) return;
        if (!animatable.isWeaponSlotShoulderPitchAimingEnabled(inventorySlot)) return;
        float pitch = -animatable.getVisualWeaponAimPitch(inventorySlot) * Mth.DEG_TO_RAD;

        // The grenade animation deliberately spins the hanger to 250 degrees.
        // Preserve that animation and apply aiming at its child instead.
        String targetBoneName = animatable.getWeaponSlotMotion(inventorySlot) == Motion.GRENADE
                ? weaponBoneName
                : hangerBoneName;
        CoreGeoBone bone = getAnimationProcessor().getBone(targetBoneName);
        if (bone != null) {
            // The 20-degree bind-pose rotation from the geo model is already
            // applied separately by GeckoLib. setRotX is the animation delta.
            float calibratedPitch = SHOULDER_AIM_PITCH_CALIBRATION_DEGREES * Mth.DEG_TO_RAD;
            bone.setRotX(calibratedPitch + pitch);
        }
    }

    @Override
    public ResourceLocation getAnimationResource(Pmvc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmvc01.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmvc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmvc01.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmvc01Entity animatable) {
        if (animatable.canWork(false) && !animatable.isBroken()) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmvc01.png");
        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmvc01stop.png");
        }
    }
}
