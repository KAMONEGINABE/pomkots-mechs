package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class BaseBossGoal extends Goal {
    protected BaseBossEntity mob;
    protected GoalDice goalDice;
    private boolean movementGoal;

    public BaseBossGoal(BaseBossEntity mob) {
        this.mob = mob;
        this.goalDice = mob.goalDice;
    }

    public void bindGoalDice(GoalDice goalDice, boolean movementGoal) {
        this.goalDice = goalDice;
        this.movementGoal = movementGoal;
    }

    @Override
    public boolean canUse() {
        var target = mob.getTarget();
        return target != null
                && target.isAlive()
                && (movementGoal
                    ? !mob.getActionController().isMovementControlledActionInProgress()
                    : !mob.getActionController().isInActionAll())
                && goalDice.canUse(this);
    }

    abstract boolean canUseInternal();
}
