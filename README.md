# Mythical GT

A GregTech CEu Modern (GTCEu) addon for the [Mythic Metals CE Port](https://github.com/supchik666/mythicmetals-forge-port) Forge 1.20.1 mod.

Depends on both `mythicmetals` and `gtceu` (both mandatory, see `mods.toml`) and doesn't
register any new ore/ingot/dust items of its own — every Mythic Metals metal's existing items
are reused via `forge:ingots/<metal>`/`forge:gems/<metal>` tags. What this addon actually adds:

- Removes the 7 alloy metals' (Bronze, Celestium, Durasteel, Hallowed, Metallurgium, Star
  Platinum, Steel) plain vanilla crafting-table recipes and replaces them with GTCEu Alloy
  Smelter recipes (`data/mythical_gt/recipes/alloy_smelter/*.json`).
- Gives every one of Mythic Metals' 28 metals GTCEu's full tool line (Saw, File, Wrench,
  Mining Hammer, Screwdriver, Wire Cutter, and more) by registering each metal as a GT
  `Material` with a `ToolProperty` (`com.mythicalgt.materials.MythicalMaterials`) — 20 metals
  get a brand-new Material; the 8 that GTCEu already ships (Bronze, Manganese, Osmium,
  Palladium, Platinum, Silver, Steel, Tin) just get a `ToolProperty` attached to the existing one.

Scaffolded from the official [GregTech-Addon-Template](https://github.com/GregTechCEu/GregTech-Addon-Template) (LGPLv3).

## Commands

```
./gradlew build        # Compile and produce the mod jar
./gradlew runClient    # Launch a dev Minecraft client with the mod loaded
./gradlew runServer    # Launch a dev dedicated server
```
