package grcmcs.minecraft.mods.pomkotsmechs;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientTickEvent.Client;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.NetworkManager.Side;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechTraderScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.PartsWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ScreenFadeManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaBattleResultScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaCameraClient;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMenuData;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadKeyCardScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.narration.IntroNarrationScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.hud.PomkotsHud;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.MechCameraHandler;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.UserInteractionManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientHudShake;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.DustHeavyParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.DustParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ExplosionCore;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.FireParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.MagazineParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.MissileSmokeParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.MuzzleFlashParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.SparkParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.SpiralAttractParticle;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.AlertEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.ArenaCameraRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.ArenaReceptionistEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.BlockMassEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.BlockProjectileEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.BossBoxEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.BossHitBoxEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.BulletEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.BulletMiddleEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.EarthbreakEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.EarthraiseEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.ElevatorEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.ExplosionEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.GrenadeEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.GrenadeLargeEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.HitBoxEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.KujiraEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.MechPilotEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.MechTraderEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.MineEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.MissileBaseEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.NeedleEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PersonalPomkotsCubeRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PlateEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PlayerDummyEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb01mk2EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb04EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb05EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb06EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb07EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb08EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmb99EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmc01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmc02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms04EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms05EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms06EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms07EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms08EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms09EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pms10EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmss01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmss02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmss03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmv01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmv01bEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmv02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmv03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmv03pEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmvc01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmvt01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubePurpleRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeRedRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeYellowRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsLeverRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PresentBoxEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RaidControllerEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RaidObjectiveEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RockLargeEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RockSmallEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.SlashEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.WaveHorizontalEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc.BlockPlacementPreviewRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc.PlacementPreviewRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.BulletBeamEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.BulletGrenadeEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.BulletGrenadeLargeEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.BulletMachineEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.BulletRifleEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.MissileGenericEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile.MissilePodEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan.ScanPulseEntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt01EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt02EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret.Pmt04EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsBGMManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsSoundManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.MechCapsule2Item;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemProperties;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ClientScanManager;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import java.io.ByteArrayInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PomkotsMechsClient {
   private static final UserInteractionManager keyPressManager = new UserInteractionManager();

   public static void initialize() {
      EntityRendererRegistry.register(PomkotsMechs.PMV01, context -> new Pmv01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMV01B, context -> new Pmv01bEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMV02, context -> new Pmv02EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMV03P, context -> new Pmv03pEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMV03, context -> new Pmv03EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMVC01, context -> new Pmvc01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMVT01, context -> new Pmvt01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB01, context -> new Pmb01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB01MK2, context -> new Pmb01mk2EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB02, context -> new Pmb02EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB03, context -> new Pmb03EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB04, context -> new Pmb04EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB05, context -> new Pmb05EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB06, context -> new Pmb06EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB07, context -> new Pmb07EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB08, context -> new Pmb08EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMB99, context -> new Pmb99EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMSS01, context -> new Pmss01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMSS02, context -> new Pmss02EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMSS03, context -> new Pmss03EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS01, context -> new Pms01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS02, context -> new Pms02EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS03, context -> new Pms03EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS04, context -> new Pms04EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS05, context -> new Pms05EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS06, context -> new Pms06EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS07, context -> new Pms07EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS08, context -> new Pms08EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS09, context -> new Pms09EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMS10, context -> new Pms10EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMC01, context -> new Pmc01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMC02, context -> new Pmc02EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMT01, context -> new Pmt01EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMT02, context -> new Pmt02EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMT03, context -> new Pmt03EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PMT04, context -> new Pmt04EntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MECH_TRADER, context -> new MechTraderEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MECH_PILOT, context -> new MechPilotEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ARENA_RECEP, context -> new ArenaReceptionistEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.EARTHBREAK2, context -> new EarthbreakEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PLAYERDUMMY, context -> new PlayerDummyEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BULLET, context -> new BulletEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BULLETMIDDLE, context -> new BulletMiddleEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.GRENADE, context -> new GrenadeEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.GRENADELARGE, context -> new GrenadeLargeEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_GENERIC, context -> new MissileGenericEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_GENERIC_LARGE, context -> new MissileGenericEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_VERTICAL, context -> new MissileBaseEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_HORIZONTAL, context -> new MissileBaseEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_ENEMY, context -> new MissileBaseEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_ENEMY_LARGE, context -> new MissileBaseEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MISSILE_POD, context -> new MissilePodEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BLOCK_PROJECTILE, context -> new BlockProjectileEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BLOCK_MASS, context -> new BlockMassEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PRESENT_BOX, context -> new PresentBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BULLET_RIFLE, context -> new BulletRifleEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BULLET_MACHINE, context -> new BulletMachineEntityRenderer(context, 0.4F));
      EntityRendererRegistry.register(PomkotsMechs.BULLET_MACHINE_LARGE, context -> new BulletMachineEntityRenderer(context, 0.7F));
      EntityRendererRegistry.register(PomkotsMechs.BULLET_GRENADE, context -> new BulletGrenadeEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BULLET_GRENADE_LARGE, context -> new BulletGrenadeLargeEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BULLET_BEAM, context -> new BulletBeamEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ROCK_SMALL, context -> new RockSmallEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ROCK_LARGE, context -> new RockLargeEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.NEEDLE, context -> new NeedleEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.MINE, context -> new MineEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.EXPLOSION, context -> new ExplosionEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.EARTHBREAK, context -> new EarthbreakEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.EARTHRAISE, context -> new EarthraiseEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.EXPLOADSLASH, context -> new SlashEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ARENA_CAMERA, context -> new ArenaCameraRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.WAVE_HOR, context -> new WaveHorizontalEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.KUJIRA, context -> new KujiraEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PLATE, context -> new PlateEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ALERT, context -> new AlertEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ALERTRED, context -> new AlertEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.ELEVATOR, context -> new ElevatorEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BOSSBOX, context -> new BossBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.HITBOX1, context -> new HitBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.HITBOX2, context -> new HitBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB02, context -> new BossHitBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB03, context -> new BossHitBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB06, context -> new BossHitBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.HITBOX_PMB08, context -> new BossHitBoxEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.RAID_CONTROLLER, context -> new RaidControllerEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.RAID_OBJECTIVE, context -> new RaidObjectiveEntityRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.PLACEMENT_PREVIEW, context -> new PlacementPreviewRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.BLOCK_PLACEMENT_PREVIEW, context -> new BlockPlacementPreviewRenderer(context));
      EntityRendererRegistry.register(PomkotsMechs.SCAN_PULSE, ScanPulseEntityRenderer::new);
      EntityRendererRegistry.register(PomkotsMechs.MECH_CAPSULE_PROJECTILE, ThrownItemRenderer::new);
      if (Platform.isFabric()) {
         BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), context -> new PomkotsCubeRenderer(context));
         BlockEntityRendererRegistry.register(
            (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW.get(), context -> new PomkotsCubeYellowRenderer(context)
         );
         BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), context -> new PomkotsCubeRedRenderer(context));
         BlockEntityRendererRegistry.register(
            (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_PURPLE.get(), context -> new PomkotsCubePurpleRenderer(context)
         );
         BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(), PersonalPomkotsCubeRenderer::new);
         BlockEntityRendererRegistry.register((BlockEntityType)PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), context -> new PomkotsLeverRenderer(context));
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.MECH_WORKBENCH_GUI.get(), MechWorkbenchScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.PARTS_WORKBENCH_GUI.get(), PartsWorkbenchScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.MECH_SALVAGER_GUI.get(), MechSalvagerScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.POMKOTS_RADAR_GUI.get(), RadarTargetSelectScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.MECH_TRADER_GUI.get(), MechTraderScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.PILOT_CONFIG_GUI.get(), PilotScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.ARENA_RECEPTIONIST.get(), ArenaReceptionistScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.ARENA_RESULT_MENU.get(), ArenaBattleResultScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.POMKOTS_DATAPAD_GUI.get(), DataPadScreen::new);
         MenuRegistry.registerScreenFactory((MenuType)PomkotsMechs.POMKOTS_DATAPAD_KEYCARD_GUI.get(), DataPadKeyCardScreen::new);
         ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            CompoundTag tag = stack.m_41783_();
            if (tag == null) {
               return -1;
            } else if (tintIndex == 0 && tag.m_128441_(PomkotsMechs.nbtName("PrimaryColor"))) {
               return tag.m_128451_(PomkotsMechs.nbtName("PrimaryColor"));
            } else {
               return tintIndex == 1 && tag.m_128441_(PomkotsMechs.nbtName("SecondaryColor")) ? tag.m_128451_(PomkotsMechs.nbtName("SecondaryColor")) : -1;
            }
         }, new ItemLike[]{(ItemLike)PomkotsMechs.MECH_CAPSULE_ITEM.get()});
         ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if (!(stack.m_41720_() instanceof MechCapsule2Item)) {
               return -1;
            } else if (tintIndex == 0) {
               return -5395027;
            } else if (tintIndex == 1) {
               return MechCapsule2Item.hasStoredMech(stack) ? -8519824 : -1;
            } else {
               return -1;
            }
         }, new ItemLike[]{(ItemLike)PomkotsMechs.MECH_CAPSULE2_ITEM.get()});
         ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if (tintIndex != 0) {
               return 16777215;
            }

            CircuitRarity rarity = CircuitItemStackHelper.getRarityOrDefault(stack);
            return rarity.tintColor();
         }, new ItemLike[]{(ItemLike)PomkotsMechs.CIRCUIT_BASE.get()});
         CircuitItemProperties.register();
      }

      keyPressManager.registerClient();
      ParticleProviderRegistry.register(PomkotsMechs.FIRE, FireParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.MISSILE_SMOKE, MissileSmokeParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.EXPLOSION_CORE, ExplosionCore.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.SPARK, SparkParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.SPARK_ORANGE, SparkParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.SPARK_RED, SparkParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.SPIRAL, SpiralAttractParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.MAGAZINE, MagazineParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.MAGAZINE_RIGHT, MagazineParticle.ProviderR::new);
      ParticleProviderRegistry.register(PomkotsMechs.MAGAZINE_LEFT, MagazineParticle.ProviderL::new);
      ParticleProviderRegistry.register(PomkotsMechs.MECH_DUST, DustParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.MECH_DUST_HEAVY, DustHeavyParticle.Provider::new);
      ParticleProviderRegistry.register(PomkotsMechs.MUZZLE_FLASH, MuzzleFlashParticle.Provider::new);
      ClientGuiEvent.RENDER_HUD.register(new PomkotsHud());
      ClientTickEvent.CLIENT_POST.register(PomkotsSoundManager::cleanup);
      ClientTickEvent.CLIENT_POST.register(MechCameraHandler::tick);
      ClientTickEvent.CLIENT_POST.register(ClientHudShake::tick);
      ClientTickEvent.CLIENT_POST.register((Client)minecraft -> ClientScanManager.clientTick());
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("gsf"), (buf, context) -> {
         int in = buf.readInt();
         int keep = buf.readInt();
         int out = buf.readInt();
         ScreenFadeManager.start(in, keep, out);
      });
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("aros"), (buf, context) -> ArenaCameraClient.startOpeningCamera());
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("aroe"), (buf, context) -> ArenaCameraClient.endOpeningCamera());
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("udp"), (buf, context) -> {
         try {
            ByteArrayInputStream bais = new ByteArrayInputStream(buf.m_130052_());
            PomkotsDataPackManager.getInstance().deSerialize(bais);
            bais.close();
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to recieve datapack to client", e);
         }
      });
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("so"), (buf, context) -> {
         if (Platform.isForge()) {
            Minecraft mc = Minecraft.m_91087_();
            mc.m_6937_(() -> mc.m_91152_(new IntroNarrationScreen()));
         } else {
            context.queue(() -> {
               Minecraft mcx = Minecraft.m_91087_();
               mcx.execute(() -> mcx.m_91152_(new IntroNarrationScreen()));
            });
         }
      });
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("bs"), (buf, context) -> {
         BGMState newState = (BGMState)buf.m_130066_(BGMState.class);
         PomkotsBGMManager.setState(newState);
      });
      NetworkManager.registerReceiver(Side.S2C, PomkotsMechs.id("arr"), (buf, context) -> {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null && mc.f_91074_.f_36096_ instanceof ArenaReceptionistMenu menu) {
            ArenaMenuData.Entry data = ArenaMenuData.read(buf);
            if (menu.getArenaId() != null && menu.getArenaId().equals(data.getArenaId())) {
               menu.setRankings(data.getRankings());
               if (mc.f_91080_ instanceof ArenaReceptionistScreen screen) {
                  screen.refleshData();
               }
            }
         }
      });
   }
}
