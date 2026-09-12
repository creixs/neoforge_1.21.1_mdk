package com.creixs_generators;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class GeneratorConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue GENERATION_TICKS;
    public static final ModConfigSpec.IntValue OUTPUT_AMOUNT;
    public static final ModConfigSpec.BooleanValue ENABLE_AUTO_EXPORT;

    // Dynamische Listen für Tags und Items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_TAGS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_ITEMS;

    static {
        BUILDER.push("Generator Settings");

        GENERATION_TICKS = BUILDER
                .comment("Dauer in Ticks, bis 1 Item generiert wird (20 Ticks = 1 Sekunde). Standard: 100 Ticks (5 Sek.)")
                .defineInRange("generationTicks", 100, 1, 72000);

        OUTPUT_AMOUNT = BUILDER
                .comment("Anzahl der Items, die pro Durchlauf generiert werden.")
                .defineInRange("outputAmount", 1, 1, 64);

        ENABLE_AUTO_EXPORT = BUILDER
                .comment("Soll der Generator Items automatisch nach unten exportieren?")
                .define("enableAutoExport", true);

        BUILDER.pop();

        BUILDER.push("Whitelist Settings");

        ALLOWED_TAGS = BUILDER
                .comment("Liste von Item-Tags (Common/Conventional Tags), die erlaubt sind (Format: 'namespace:tagname').",
                        "Beispiele: 'c:ores', 'c:gems', 'c:dusts', 'c:raw_materials', 'c:crops'")
                .defineListAllowEmpty("allowedTags",
                        List.of("c:ores", "c:gems", "c:dusts", "c:raw_materials"),
                        obj -> obj instanceof String);

        ALLOWED_ITEMS = BUILDER
                .comment("Zusätzliche Einzel-Items, die erlaubt sind (Format: 'modid:itemid').",
                        "Beispiele: 'minecraft:cobblestone', 'minecraft:coal'")
                .defineListAllowEmpty("allowedItems",
                        List.of("minecraft:cobblestone", "minecraft:coal"),
                        obj -> obj instanceof String);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Prüft, ob ein Item in den Generator eingelegt werden darf.
     */
    public static boolean isItemAllowed(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 1. Tags prüfen
        List<? extends String> tagList = ALLOWED_TAGS.get();
        for (String tagString : tagList) {
            ResourceLocation location = ResourceLocation.tryParse(tagString);
            if (location != null) {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, location);
                if (stack.is(tagKey)) {
                    return true;
                }
            }
        }

        // 2. Einzelne Item-IDs prüfen
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        List<? extends String> itemList = ALLOWED_ITEMS.get();
        return itemList.contains(itemId);
    }
}