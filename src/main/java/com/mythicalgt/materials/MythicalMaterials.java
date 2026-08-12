package com.mythicalgt.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.mythicalgt.MythicalGT;

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

    /**
     * Every material this addon touches (both the 20 newly registered ones and the 8 reused
     * GTCEu materials), populated by {@link #init()}/{@link #modify()}. Used by
     * {@link com.mythicalgt.MythicalCreativeTabs} to list every tool this addon adds without
     * needing its own separate bookkeeping of which materials/tool types actually got an item.
     */
    public static final java.util.List<Material> ALL_MATERIALS = new java.util.ArrayList<>();

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
    // Colors are averaged from each metal's actual mythicmetals ingot/gem texture (not guessed
    // from the metal's name/lore) so GT's tool tint (IGTTool#tintColor, which reads
    // Material#getMaterialARGB) roughly matches the metal's real in-game color.
    private static final java.util.Map<String, Stats> NEW_MATERIALS = java.util.Map.ofEntries(
            java.util.Map.entry("adamantite", Stats.ingot(7.0F, 5.0F, 1024, 4, 16, 0x730910)),
            java.util.Map.entry("aquarium", Stats.ingot(6.5F, 2.0F, 455, 2, 12, 0x3E6F9B)),
            java.util.Map.entry("banglum", Stats.ingot(11.0F, 2.0F, 260, 2, 1, 0x5C452C)),
            java.util.Map.entry("carmot", Stats.ingot(11.5F, 3.0F, 1130, 3, 42, 0x982A56)),
            java.util.Map.entry("celestium", Stats.ingot(25.0F, 6.0F, 2470, 5, 26, 0x8C547E)),
            java.util.Map.entry("durasteel", Stats.ingot(7.1F, 3.5F, 820, 3, 12, 0x252525)),
            java.util.Map.entry("hallowed", Stats.ingot(12.0F, 5.0F, 1984, 4, 20, 0xCBC780)),
            java.util.Map.entry("kyber", Stats.ingot(7.0F, 2.5F, 889, 3, 20, 0x8F6A97)),
            java.util.Map.entry("metallurgium", Stats.ingot(15.0F, 8.0F, 3000, 5, 30, 0x8782B3)),
            java.util.Map.entry("midas_gold", Stats.ingot(8.0F, 3.0F, 600, 2, 22, 0xBB7136)),
            java.util.Map.entry("morkite", Stats.gem(6.0F, 2.5F, 400, 2, 10, 0x4C7874)),
            java.util.Map.entry("mythril", Stats.ingot(14.3F, 3.0F, 1564, 4, 22, 0x5192AD)),
            java.util.Map.entry("orichalcum", Stats.ingot(6.0F, 4.0F, 2048, 4, 16, 0x5AA26A)),
            java.util.Map.entry("prometheum", Stats.ingot(6.0F, 4.0F, 1472, 3, 15, 0x65656B)),
            java.util.Map.entry("quadrillum", Stats.ingot(6.0F, 2.7F, 321, 2, 8, 0x194C55)),
            java.util.Map.entry("runite", Stats.ingot(8.9F, 3.3F, 1337, 3, 17, 0x3F7484)),
            java.util.Map.entry("star_platinum", Stats.ingot(9.0F, 4.0F, 1300, 4, 18, 0x725BB3)),
            java.util.Map.entry("starrite", Stats.gem(8.0F, 3.5F, 700, 3, 20, 0x826591)),
            java.util.Map.entry("stormyx", Stats.ingot(8.5F, 3.5F, 1305, 3, 20, 0x7F56BB)),
            java.util.Map.entry("unobtainium", Stats.gem(20.0F, 6.0F, 3500, 5, 30, 0x998DA4)));

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
            ALL_MATERIALS.add(material);
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
        Material material = builder.color(stats.color())
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_BOLT_SCREW,
                        MaterialFlags.GENERATE_GEAR)
                .toolStats(buildToolProperty(stats))
                .buildAndRegister();
        ALL_MATERIALS.add(material);
    }

    private static ToolProperty buildToolProperty(Stats stats) {
        return ToolProperty.Builder
                .of(stats.harvestSpeed(), stats.attackDamage(), stats.durability(), stats.harvestLevel())
                .enchantability(stats.enchantability())
                .build();
    }
}
