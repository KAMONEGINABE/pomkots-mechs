package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;

/** Runs a weapon-rack animation and exchanges the stacks at its midpoint. */
public final class ArmWeaponSwapAction extends Action {
    public static final int SWAP_TICK = 10;
    public static final int RETURN_TICKS = 10;

    private final Runnable swapAtMidpoint;

    public ArmWeaponSwapAction(Runnable swapAtMidpoint) {
        super(0, SWAP_TICK, RETURN_TICKS);
        this.swapAtMidpoint = swapAtMidpoint;
    }

    @Override
    public void fireAction() {
        super.fireAction();
        swapAtMidpoint.run();
    }
}
