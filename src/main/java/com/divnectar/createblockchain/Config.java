package com.divnectar.createblockchain;

import java.util.Arrays;
import java.util.stream.Collectors;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    public static final ModConfigSpec.IntValue BASE_ENERGY_PER_COIN = BUILDER
            .comment("The base amount of Forge Energy (FE) required to mine one coin. Default value is around 1 coin per min with two max speed alternators.")
            .defineInRange("baseEnergyPerCoin", 830000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue DIFFICULTY_BONUS = BUILDER
            .comment("How much additional FE is added to the mining cost each time the difficulty increases.")
            .defineInRange("difficultyBonus", 10000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue DIFFICULTY_INTERVAL = BUILDER
            .comment("How many coins must be mined globally before the mining cost increases by the difficulty bonus.")
            .defineInRange("difficultyInterval", 110, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue MAX_ENERGY_CONSUMPTION = BUILDER
            .comment("The maximum amount of FE the miner can consume per tick.")
            .defineInRange("maxEnergyConsumption", 8092, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ENERGY_CAPACITY = BUILDER
            .comment("The total amount of FE the miner can store internally.")
            .defineInRange("energyCapacity", 8000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.ConfigValue<String> COIN_TO_GENERATE = BUILDER
            .comment("The type of coin to generate when mining. Can be SPUR, BEVEL, SPROCKET, COG, CROWN, or SUN.")
            .define("coinToGenerate", "SPUR");


    // --- Heat & Cooling ---
//    public static final ModConfigSpec.DoubleValue HEAT_SCALING_FACTOR = BUILDER
//            .comment("A multiplier to control how much heat is generated per FE consumed. Higher values mean faster heating.")
//            .defineInRange("heatScalingFactor", 0.0025, 0.0, 1.0);
//    public static final ModConfigSpec.DoubleValue HEAT_GENERATION_RATE = BUILDER
//            .comment("How many base heat units are generated per tick when the miner is working. This is multiplied by the energy consumed per tick.")
//            .defineInRange("heatGenerationRate", 0.05, 0, 100);
//    public static final ModConfigSpec.DoubleValue PASSIVE_COOLING_RATE = BUILDER
//            .comment("How many heat units are passively lost per tick.")
//            .defineInRange("passiveCoolingRate", 0.2, 0, 100);
//    public static final ModConfigSpec.DoubleValue ACTIVE_COOLING_RATE = BUILDER
//            .comment("How many additional heat units are lost per tick when using a fluid coolant.")
//            .defineInRange("activeCoolingRate", 1.2, 0, 100);
    public static final ModConfigSpec.IntValue OVERHEAT_THRESHOLD = BUILDER
            .comment("The heat percentage at which the miner will shut down to cool off.")
            .defineInRange("overheatThreshold", 95, 1, 100);
//    public static final ModConfigSpec.IntValue COOLDOWN_THRESHOLD = BUILDER
//            .comment("The heat percentage the miner must cool down to before it can restart.")
//            .defineInRange("cooldownThreshold", 70, 0, 94);

    // --- Heat & Cooling ---

    // NEW: A multiplier for heat generated per FE. Renamed from HEAT_SCALING_FACTOR for clarity.
    public static final ModConfigSpec.DoubleValue HEAT_PER_FE = BUILDER
            .comment("The amount of heat generated per FE consumed. A smaller value is better.")
            .defineInRange("heatPerFE", 0.001, 0.0, 1.0);

    // NEW: The percentage of current heat passively dissipated per tick.
    public static final ModConfigSpec.DoubleValue PASSIVE_COOLING_PERCENT = BUILDER
            .comment("The percentage of heat passively cooled each tick (e.g., 0.005 is 0.5%).")
            .defineInRange("passiveCoolingPercent", 0.005, 0.0, 1.0);

    // NEW: The additional percentage of cooling provided by Water.
    public static final ModConfigSpec.DoubleValue WATER_COOLING_PERCENT = BUILDER
            .comment("The additional percentage of heat cooled by Water each tick.")
            .defineInRange("waterCoolingPercent", 0.01, 0.0, 1.0);

    // NEW: The additional percentage of cooling provided by Cryotheum Coolant.
    public static final ModConfigSpec.DoubleValue CRYOTHEUM_COOLING_PERCENT = BUILDER
            .comment("The additional percentage of heat cooled by Cryotheum Coolant each tick.")
            .defineInRange("cryotheumCoolingPercent", 0.025, 0.0, 1.0);

    // --- Durability & Maintenance ---
    public static final ModConfigSpec.IntValue MAX_DURABILITY = BUILDER
            .comment("The maximum durability of a Mining Core.")
            .defineInRange("maxDurability", 1000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue DURABILITY_DAMAGE_STANDARD = BUILDER
            .comment("The amount of durability damage a core takes per coin mined when using Cryotheum Coolant.")
            .defineInRange("durabilityDamageStandard", 1, 0, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue DURABILITY_DAMAGE_WATER = BUILDER
            .comment("The amount of durability damage a core takes per coin mined when using Water as a coolant.")
            .defineInRange("durabilityDamageWater", 2, 0, Integer.MAX_VALUE);


    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}