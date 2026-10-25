package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.LockTargets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface PomkotsVehicle extends PomkotsControllable {
   LockTargets getLockTargets();

   Vec3 getClientSeatPos(Entity var1);

   boolean shouldRenderDefaultHud(String var1);

   boolean shouldLockMulti(DriverInput var1);

   boolean shouldLockWeak(DriverInput var1);

   boolean shouldLockStrong(DriverInput var1);
}
