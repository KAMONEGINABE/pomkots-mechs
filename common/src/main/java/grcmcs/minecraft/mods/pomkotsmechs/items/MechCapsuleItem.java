package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MechCapsuleProjectileEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class MechCapsuleItem extends Item {
   public MechCapsuleItem(Properties properties) {
      super(properties);
   }

   public Component m_7626_(ItemStack stack) {
      Component base = super.m_7626_(stack);
      CompoundTag root = stack.m_41783_();
      if (root != null && root.m_128441_(PomkotsMechs.nbtName("MechPreset"))) {
         CompoundTag preset = root.m_128469_(PomkotsMechs.nbtName("MechPreset"));
         if (!preset.m_128441_(PomkotsMechs.nbtName("ModelName"))) {
            return base;
         } else {
            String no = preset.m_128461_(PomkotsMechs.nbtName("ModelName"));
            if (no.startsWith("Pmb")) {
               return base.m_6881_().m_130946_(" (Boss: " + no + ")");
            } else {
               return no.startsWith("Pms") ? base.m_6881_().m_130946_(" (Mob: " + no + ")") : base.m_6881_().m_130946_(" (Model: " + no + ")");
            }
         }
      } else {
         return base;
      }
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.f_46443_) {
         MechCapsuleProjectileEntity projectile = new MechCapsuleProjectileEntity(level, player);
         projectile.m_37446_(stack);
         projectile.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 1.5F, 1.0F);
         level.m_7967_(projectile);
      }

      player.m_36246_(Stats.f_12982_.m_12902_(this));
      if (!player.m_150110_().f_35937_) {
         stack.m_41774_(1);
      }

      return InteractionResultHolder.m_19092_(stack, level.f_46443_);
   }

   public static LivingEntity generateEntity(Level level, ItemStack deployItem) {
      CompoundTag root = deployItem.m_41783_();
      if (root != null && root.m_128441_(PomkotsMechs.nbtName("MechPreset"))) {
         CompoundTag preset = root.m_128469_(PomkotsMechs.nbtName("MechPreset"));
         if (!preset.m_128441_(PomkotsMechs.nbtName("EntityType"))) {
            return null;
         }

         String entityTypeId = preset.m_128461_(PomkotsMechs.nbtName("EntityType"));
         EntityType<?> entType = getEntityType(new ResourceLocation(entityTypeId));
         if (entType.m_20615_(level) instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Pmvc01Entity mech) {
               applyPresetToCustomMech(preset, mech);
               return mech;
            } else {
               return livingEntity;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static EntityType<?> getEntityType(ResourceLocation id) {
      return (EntityType<?>)BuiltInRegistries.f_256780_.m_7745_(id);
   }

   public static void applyPresetToCustomMech(CompoundTag preset, Pmvc01Entity mech) {
      if (preset.m_128441_(PomkotsMechs.nbtName("Color"))) {
         mech.setTextureColor(preset.m_128451_(PomkotsMechs.nbtName("Color")));
      }

      if (preset.m_128441_(PomkotsMechs.nbtName("Parts"))) {
         for (Tag t : preset.m_128437_(PomkotsMechs.nbtName("Parts"), 10)) {
            CompoundTag p = (CompoundTag)t;
            int slot = p.m_128451_(PomkotsMechs.nbtName("Slot"));
            String itemId = p.m_128461_(PomkotsMechs.nbtName("Item"));
            ItemStack partStack = ItemStack.f_41583_;
            if (!PomkotsMechs.nbtName("empty").equals(itemId)) {
               Item item = (Item)BuiltInRegistries.f_257033_.m_7745_(new ResourceLocation(itemId));
               int count = p.m_128451_(PomkotsMechs.nbtName("Count"));
               partStack = new ItemStack(item, count);
               if (p.m_128441_(PomkotsMechs.nbtName("Tag"))) {
                  partStack.m_41751_(p.m_128469_(PomkotsMechs.nbtName("Tag")).m_6426_());
               }
            }

            mech.m_6836_(slot, partStack);
         }

         mech.m_6596_();
         mech.m_21153_(mech.m_21233_());
      }
   }

   public static CompoundTag createPresetTag(String modelName, ResourceLocation mechType, int color, List<MechCapsuleItem.PresetPart> parts) {
      CompoundTag root = new CompoundTag();
      CompoundTag preset = new CompoundTag();
      preset.m_128359_(PomkotsMechs.nbtName("ModelName"), modelName);
      preset.m_128359_(PomkotsMechs.nbtName("EntityType"), mechType.toString());
      if (mechType.m_135815_().equals("pmvc01")) {
         preset.m_128405_(PomkotsMechs.nbtName("Color"), color);
         if (parts != null) {
            ListTag list = new ListTag();

            for (MechCapsuleItem.PresetPart p : parts) {
               CompoundTag t = new CompoundTag();
               t.m_128405_(PomkotsMechs.nbtName("Slot"), p.slot());
               t.m_128359_(PomkotsMechs.nbtName("Item"), p.item().toString());
               t.m_128405_(PomkotsMechs.nbtName("Count"), p.count());
               if (p.tag() != null) {
                  t.m_128365_(PomkotsMechs.nbtName("Tag"), p.tag());
               }

               list.add(t);
            }

            preset.m_128365_(PomkotsMechs.nbtName("Parts"), list);
         }
      }

      root.m_128365_(PomkotsMechs.nbtName("MechPreset"), preset);
      return root;
   }

   private static CompoundTag levelTag(int lv) {
      CompoundTag t = new CompoundTag();
      t.m_128405_(PomkotsMechs.nbtName("Level"), lv);
      return t;
   }

   public static void buildPreset(String presetName, ItemStack stack) {
      CompoundTag preset = null;
      if ("Rusty".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            8,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("rustyhead"), 1, levelTag(2)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("rustybody"), 1, levelTag(2)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("rustyarm"), 1, levelTag(2)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("rustylegs"), 1, levelTag(2)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("shiga"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("narita"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("shakuji"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("tenpou"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("kawasemi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("kawasemi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinerifle"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("empty"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Deneb".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            6,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("denebhead"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("denebbody"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("denebarm"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("deneblegs"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("saga"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("kansai"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("shinobazu"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("senzoku"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("tsubame"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("tsubame"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinemachinegun"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("magazineshotgun"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Altair".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            3,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("altairhead"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("altairbody"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("altairarm"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("altairlegs"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("saga"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("kansai"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("shinobazu"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("tenpou"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("nosuri"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("biwa"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinemachinegun"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("empty"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Vega".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            7,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("vegahead"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("vegabody"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("vegaarm"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("vegalegs"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("shiga"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("narita"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("biwa"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("biwa"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Sirius".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            2,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("siriushead"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("siriusbody"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("siriusarm"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("siriuslegs"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("saga"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("kansai"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("shakuji"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("shakuji"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("empty"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("kawasemi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinerifle"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("magazinerifle"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("empty"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Aldebaran".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            4,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("aldebaranhead"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("aldebaranbody"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("aldebaranarm"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("aldebaranlegs"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("chiba"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("haneda"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("suwa"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("suwa"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Muknvali".equals(presetName)) {
         preset = createPresetTag(
            presetName,
            PomkotsMechs.id("pmvc01"),
            1,
            List.of(
               new MechCapsuleItem.PresetPart(0, PomkotsMechs.id("muknvalihead"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(1, PomkotsMechs.id("muknvalibody"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(2, PomkotsMechs.id("muknvaliarm"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(3, PomkotsMechs.id("muknvalilegs"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(4, PomkotsMechs.id("chiba"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(5, PomkotsMechs.id("haneda"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(6, PomkotsMechs.id("kasumi"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(7, PomkotsMechs.id("kagenobu"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(8, PomkotsMechs.id("mukudori"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(9, PomkotsMechs.id("biwa"), 1, levelTag(5)),
               new MechCapsuleItem.PresetPart(10, PomkotsMechs.id("protosbunit"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(11, PomkotsMechs.id("circuitsoftlock"), 1, levelTag(1)),
               new MechCapsuleItem.PresetPart(12, PomkotsMechs.id("magazinegatling"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(13, PomkotsMechs.id("empty"), 32, levelTag(1)),
               new MechCapsuleItem.PresetPart(14, PomkotsMechs.id("magazinemissile"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(15, PomkotsMechs.id("magazinegrenade"), 12, levelTag(1)),
               new MechCapsuleItem.PresetPart(16, PomkotsMechs.id("pellet"), 64, levelTag(1))
            )
         );
      } else if ("Pmb01Mk2".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb01mk2"), 0, null);
      } else if ("Pmb02".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb02"), 0, null);
      } else if ("Pmb03".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb03"), 0, null);
      } else if ("Pmb04".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb04"), 0, null);
      } else if ("Pmb05".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb05"), 0, null);
      } else if ("Pmb06".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb06"), 0, null);
      } else if ("Pmb07".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb07"), 0, null);
      } else if ("Pmb08".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb08"), 0, null);
      } else if ("Pmb99".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pmb99"), 0, null);
      } else if ("Pms01".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms01"), 0, null);
      } else if ("Pms02".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms02"), 0, null);
      } else if ("Pms03".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms03"), 0, null);
      } else if ("Pms04".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms04"), 0, null);
      } else if ("Pms05".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms05"), 0, null);
      } else if ("Pms06".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms06"), 0, null);
      } else if ("Pms07".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms07"), 0, null);
      } else if ("Pms08".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms08"), 0, null);
      } else if ("Pms09".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms09"), 0, null);
      } else if ("Pms10".equals(presetName)) {
         preset = createPresetTag(presetName, PomkotsMechs.id("pms10"), 0, null);
      }

      if (preset != null) {
         stack.m_41784_().m_128391_(preset);
      }
   }

   public record PresetPart(int slot, ResourceLocation item, int count, CompoundTag tag) {
   }
}
