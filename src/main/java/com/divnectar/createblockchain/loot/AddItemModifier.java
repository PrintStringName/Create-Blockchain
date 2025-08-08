package com.divnectar.createblockchain.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class AddItemModifier extends LootModifier {
    // The modern codec uses ItemStack.CODEC to allow for counts and NBT in the JSON.
    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    ItemStack.CODEC.fieldOf("item").forGetter(m -> m.itemStack)
            ).apply(inst, AddItemModifier::new));

    private final ItemStack itemStack;

    /**
     * Constructs a LootModifier.
     * @param conditionsIn The conditions that need to be met for this modifier to apply.
     * @param itemStack The ItemStack to be added to the loot.
     */
    public AddItemModifier(LootItemCondition[] conditionsIn, ItemStack itemStack) {
        super(conditionsIn);
        this.itemStack = itemStack;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // This method is called when the conditions are met.
        // We add a copy of our itemStack to the generated loot.
        generatedLoot.add(this.itemStack.copy());
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

