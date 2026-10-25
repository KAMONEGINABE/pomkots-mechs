package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;

public interface IArenaMatchStateProcessor {
   void onEnter(ArenaMatchContext var1);

   void tick(ArenaMatchContext var1);

   void onExit(ArenaMatchContext var1);

   void onCancel(ArenaMatchContext var1);
}
