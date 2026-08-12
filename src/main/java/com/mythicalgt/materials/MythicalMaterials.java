package com.mythicalgt.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.mythicalgt.MythicalGT;

import java.util.function.Consumer;

/**
 * Wires every Mythic Metals CE Port metal into GTCEu's tool-generation system.
 * <p>
 * {@link #init()} registers a brand-new GT {@link Material} (under this addon's own material
 * registry, created in {@link MythicalGT#addMaterialRegistries}) for every metal that GTCEu doesn't
 * already ship a built-in material for. {@link #modify()} instead attaches a {@link ToolProperty}
 * to the handful of metals GTCEu already has (Bronze, Manganese, Osmium, Palladium, Platinum,
 * Silver, Steel, Tin), rather than creating a duplicate/conflicting Material for those.
 * <p>
 * Neither this class nor the addon's tag files (see {@code data/forge/tags/items/ingots|gems/*})
 * ever register a new ingot/gem/ore item: GT's {@code ChemicalHelper} resolves each material's item
 * forms tag-first (checking {@code forge:ingots/<name>}/{@code forge:gems/<name>} before ever
 * falling back to auto-generating its own item), so tagging Mythic Metals' existing items into
 * those tags is what makes GT's tool-recipe generation use the real items instead of duplicates.
 * <p>
 * Tool/durability/damage numbers for the metals that already had a {@code MythicToolMaterials}
 * entry in the base mod are copied from there, so a metal's GT tool tier roughly tracks its
 * existing vanilla-style tool tier. The handful of metals with no existing tool line in the base
 * mod (Midas Gold, Morkite, Starrite, Unobtainium, and the 4 reused materials that never had their
 * own {@code ToolSet}: Manganese, Platinum, Silver, Tin) get reasonable invented numbers instead.
 */
public final class MythicalMaterials {

    private MythicalMaterials() {}

    private record Stats(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                          int enchantability, int color, boolean gem) {
        private static Stats ingot(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                                    int enchantability, int color) {
            return new Stats(harvestSpeed, attackDamage, durability, harvestLevel, enchantability, color, false);
        }

        private static Stats gem(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                                  int enchantability, int color) {
            return new Stats(harvestSpeed, attackDamage, durability, harvestLevel, enchantability, color, true);
        }
    }

    // Metals new to GT: registered as brand-new Materials in this addon's own registry.
    private static final java.util.Map<String, Stats> NEW_MATERIALS = java.util.Map.ofEntries(
            java.util.Map.entry("adamantite", Stats.ingot(7.0F, 5.0F, 1024, 4, 16, 0x3A4A5C)),
            java.util.Map.entry("aquarium", Stats.ingot(6.5F, 2.0F, 455, 2, 12, 0x40C4C4)),
            java.util.Map.entry("banglum", Stats.ingot(11.0F, 2.0F, 260, 2, 1, 0xB33A1A)),
            java.util.Map.entry("carmot", Stats.ingot(11.5F, 3.0F, 1130, 3, 42, 0x8B1A1A)),
            java.util.Map.entry("celestium", Stats.ingot(25.0F, 6.0F, 2470, 5, 26, 0xD4AF37)),
            java.util.Map.entry("durasteel", Stats.ingot(7.1F, 3.5F, 820, 3, 12, 0x707070)),
            java.util.Map.entry("hallowed", Stats.ingot(12.0F, 5.0F, 1984, 4, 20, 0xF0E68C)),
            java.util.Map.entry("kyber", Stats.ingot(7.0F, 2.5F, 889, 3, 20, 0x66CCFF)),
            java.util.Map.entry("metallurgium", Stats.ingot(15.0F, 8.0F, 3000, 5, 30, 0x4B0082)),
            java.util.Map.entry("midas_gold", Stats.ingot(8.0F, 3.0F, 600, 2, 22, 0xFFD700)),
            java.util.Map.entry("morkite", Stats.ingot(6.0F, 2.5F, 400, 2, 10, 0x2E8B8B)),
            java.util.Map.entry("mythril", Stats.ingot(14.3F, 3.0F, 1564, 4, 22, 0x6699CC)),
            java.util.Map.entry("orichalcum", Stats.ingot(6.0F, 4.0F, 2048, 4, 16, 0xCC7722)),
            java.util.Map.entry("prometheum", Stats.ingot(6.0F, 4.0F, 1472, 3, 15, 0xFF4500)),
            java.util.Map.entry("quadrillum", Stats.ingot(6.0F, 2.7F, 321, 2, 8, 0xADD8E6)),
            java.util.Map.entry("runite", Stats.ingot(8.9F, 3.3F, 1337, 3, 17, 0x50C878)),
            java.util.Map.entry("star_platinum", Stats.ingot(9.0F, 4.0F, 1300, 4, 18, 0xE5E4E2)),
            java.util.Map.entry("starrite", Stats.gem(8.0F, 3.5F, 700, 3, 20, 0xF8F8FF)),
            java.util.Map.entry("stormyx", Stats.ingot(8.5F, 3.5F, 1305, 3, 20, 0x4A5A6A)),
            java.util.Map.entry("unobtainium", Stats.gem(20.0F, 6.0F, 3500, 5, 30, 0x1A0033)));

    // Metals GTCEu already ships as built-in Materials: only get a ToolProperty added, no new Material.
    private static final java.util.Map<String, Stats> REUSED_MATERIAL_STATS = java.util.Map.of(
            "bronze", Stats.ingot(5.5F, 2.5F, 354, 2, 14, 0),
            "manganese", Stats.ingot(6.5F, 2.5F, 400, 2, 10, 0),
            "osmium", Stats.ingot(7.0F, 2.5F, 664, 3, 13, 0),
            "palladium", Stats.ingot(8.0F, 3.5F, 1234, 4, 16, 0),
            "platinum", Stats.ingot(7.0F, 3.0F, 600, 3, 15, 0),
            "silver", Stats.ingot(6.0F, 2.0F, 300, 2, 12, 0),
            "steel", Stats.ingot(6.5F, 2.5F, 700, 3, 11, 0),
            "tin", Stats.ingot(5.0F, 1.5F, 200, 1, 8, 0));

    private static final java.util.Map<String, java.util.function.Supplier<Material>> REUSED_MATERIALS = java.util.Map
            .of(
                    "bronze", () -> GTMaterials.Bronze,
                    "manganese", () -> GTMaterials.Manganese,
                    "osmium", () -> GTMaterials.Osmium,
                    "palladium", () -> GTMaterials.Palladium,
                    "platinum", () -> GTMaterials.Platinum,
                    "silver", () -> GTMaterials.Silver,
                    "steel", () -> GTMaterials.Steel,
                    "tin", () -> GTMaterials.Tin);

    /** Called from {@link MythicalGT}'s {@code MaterialEvent} listener. Registers the 20 new-to-GT materials. */
    public static void init() {
        NEW_MATERIALS.forEach(MythicalMaterials::registerNewMaterial);
    }

    /** Called from {@link MythicalGT}'s {@code PostMaterialEvent} listener. Adds tools to the 8 reused materials. */
    public static void modify() {
        REUSED_MATERIALS.forEach((name, supplier) -> {
            Material material = supplier.get();
            if (material.hasProperty(PropertyKey.TOOL)) {
                // Already has a tool line from GTCEu itself - don't overwrite it.
                return;
            }
            Stats stats = REUSED_MATERIAL_STATS.get(name);
            material.setProperty(PropertyKey.TOOL, buildToolProperty(stats));
            material.addFlags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD,
                    MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_BOLT_SCREW,
                    MaterialFlags.GENERATE_GEAR);
        });
    }

    private static void registerNewMaterial(String name, Stats stats) {
        Material.Builder builder = new Material.Builder(MythicalGT.id(name));
        if (stats.gem()) {
            builder.gem(stats.harvestLevel());
        } else {
            builder.ingot(stats.harvestLevel());
        }
        builder.color(stats.color())
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_BOLT_SCREW,
                        MaterialFlags.GENERATE_GEAR)
                .toolStats(buildToolProperty(stats))
                .buildAndRegister();
    }

    private static ToolProperty buildToolProperty(Stats stats) {
        return ToolProperty.Builder
                .of(stats.harvestSpeed(), stats.attackDamage(), stats.durability(), stats.harvestLevel())
                .enchantability(stats.enchantability())
                .build();
    }
}
