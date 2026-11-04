package grcmcs.minecraft.mods.pomkotsmechs.forge.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * 既存ルートテーブルを上書きせず、指定した追加ルートテーブルのアイテムを安全に合成する GLM。
 * 他 Mod（SuperbWarfare 等）によるチェストスロット圧迫（27枠上限）によるアイテムロストを緩和するため、
 * 生成された同種スタック可能アイテムは既存リスト内でマージした上で追加する。
 */
public class AddLootTableModifier extends LootModifier {
    public static final Codec<AddLootTableModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(
            ResourceLocation.CODEC.fieldOf("lootTable").forGetter(m -> m.lootTable)
    ).apply(inst, AddLootTableModifier::new));

    private final ResourceLocation lootTable;

    public AddLootTableModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable) {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!PomkotsMechs.CONFIG.enableChestLootInjection) {
            return generatedLoot;
        }

        LootTable extraTable = context.getResolver().getLootTable(this.lootTable);
        extraTable.getRandomItemsRaw(context, itemStack -> {
            for (ItemStack existing : generatedLoot) {
                if (ItemStack.isSameItemSameTags(existing, itemStack) && existing.getCount() < existing.getMaxStackSize()) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    int toAdd = Math.min(space, itemStack.getCount());
                    existing.grow(toAdd);
                    itemStack.shrink(toAdd);
                    if (itemStack.isEmpty()) {
                        return;
                    }
                }
            }
            if (!itemStack.isEmpty()) {
                generatedLoot.add(itemStack);
            }
        });

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
