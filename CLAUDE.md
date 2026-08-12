# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

**Mythical GT** — a GregTech CEu Modern (GTCEu) addon for [Mythic Metals CE Port](https://github.com/supchik666/mythicmetals-forge-port), a separate Forge 1.20.1 mod. It does not fork or duplicate that mod: it depends on it (`mods.toml` mandatory dependency) and only adds two things on top of its existing items:

1. GTCEu Alloy Smelter recipes for the 7 alloy metals (Bronze, Celestium, Durasteel, Hallowed, Metallurgium, Star Platinum, Steel), replacing their plain vanilla crafting-table recipes.
2. GTCEu's full tool line (Saw, File, Wrench, Mining Hammer, Screwdriver, Wire Cutter, electric Drill/Chainsaw tiers, etc. — 31 tool items per metal) for all 28 Mythic Metals metals, via GT's `Material`/`ToolProperty` system.

No new ore, ingot, dust, or raw-material items are ever registered — GTCEu resolves each material's item forms tag-first (`forge:ingots/<name>` / `forge:gems/<name>`), so tagging Mythic Metals' existing items in is what makes GT use the real items instead of generating duplicates. The only *new* items this addon creates are the GT tool items themselves (which don't exist in the base mod at all).

Scaffolded from the official [GregTech-Addon-Template](https://github.com/GregTechCEu/GregTech-Addon-Template) (LGPLv3), which uses NeoForge's `legacyforge` ModDev tooling (not classic ForgeGradle) even though the target is Forge, not NeoForge.

## Status and intent

Everything below is **done, built, and verified working** (see "Testing" for how): 7 alloy recipes converted to GTCEu Alloy Smelter recipes with the base mod's vanilla recipe deleted; all 28 metals registered as GT materials with a full tool line; item reuse tags for all 28; a dedicated `mythical_gt` creative tab; lang entries for the 20 new materials; tool tint colors matched to each metal's real texture. Pushed to `main` at every step — `git log` is the accurate history of what changed and why, prefer it over asking the user to re-explain past decisions.

Explicit product decisions already made and settled (don't re-litigate without being asked):
- This is a **separate addon**, not a fork of Mythic Metals CE Port. It must never duplicate that mod's items/blocks — only tag existing ones in.
- All 28 metals get the full GT tool line, not a subset.
- Alloy smelter recipe EU/duration values are placeholder-reasonable (LV tier, 32 EU/t, duration scaled loosely to ingredient count) — explicitly *not* balanced against real GT progression. Revisit if the user asks for balance/tuning specifically.
- Tool material colors must be derived from the actual `mythicmetals` texture PNGs (see the Python snippet under "Colors must come from the real texture"), never guessed from the metal's name or lore — this was gotten wrong once (Runite) and corrected.

Known gaps / plausible next asks, not started:
- No ore-processing chain (crusher/washer/thermal centrifuge/etc. recipes) for these metals — only the alloy smelter recipes and tools exist so far.
- No machine casings, multiblocks, or anything beyond materials/tools/the 7 alloy recipes.
- EU/duration values on the alloy recipes are unbalanced placeholders (see above).
- `gtceu_version`/`ldlib_version`/etc. in `gradle.properties` are pinned to what the addon template shipped (GTCEu 7.4.0) rather than the latest GTCEu release (7.5.3 was latest as of this writing) — deliberately not bumped mid-session to avoid an unverified dependency-version combo; bumping is safe to attempt but re-verify per "Testing" before trusting it.

## Commands

- `./gradlew build` — compile and produce the mod jar (`build/libs/mythicalgt-<version>.jar`)
- `./gradlew runClient` / `runServer` / `runData` — **do not use these to test against Mythic Metals CE Port.** See "Testing" below — they will crash with `NoSuchFieldError` and are not a real bug.
- `./gradlew spotlessApply` — auto-format Java (not run automatically by `build`, but CI/some local configs do run `spotlessCheck` — run `spotlessApply` before committing if unsure)

## Architecture

### Entry points

- `MythicalGT.java` — the `@Mod` class. Subscribes to GTCEu's material lifecycle events on the mod event bus and registers `MythicalCreativeTabs`. Owns `REGISTRATE` (a `GTRegistrate` instance), required by `IGTAddon` even though this addon doesn't register machines/recipe types through it.
- `MythicalGTAddon.java` — implements `IGTAddon`, annotated `@GTAddon`. This is how GTCEu's own addon service-loader discovers this mod, separate from Forge's normal `@Mod` loading.

### Material/tool registration — `materials/MythicalMaterials.java`

GTCEu's material lifecycle (confirmed from GTCEu's own `CommonProxy.initMaterials()`):

1. `GTMaterials.init()` — GTCEu's own ~650+ built-in materials register first.
2. `MaterialEvent` fires on every mod's own event bus (`ModLoader.postEvent`) — **register new materials here**. The registry must be created first via `MaterialRegistryEvent` → `GTCEuAPI.materialManager.createRegistry(MOD_ID)`.
3. Registry closes (no new materials possible after this point).
4. `PostMaterialEvent` fires — **modify existing materials here** (e.g. attach a `ToolProperty` to a material GTCEu already ships), matching GTCEu's own convention (see `AlloyBlastPropertyAddition.java` in GTCEu's source for the pattern this mirrors).

Two buckets, both driven by a single hand-maintained `Stats` record map in `MythicalMaterials.java`:

- **20 metals new to GT** (everything except the 8 below) get a brand-new `Material.Builder(MythicalGT.id(name))` in `init()`, with `.ingot(harvestLevel)` or `.gem(harvestLevel)` depending on which item form the metal actually has in the base mod (see "Ingot vs gem" below), `.color(...)`, the 5 `MaterialFlags` needed for full tool-recipe generation (`GENERATE_PLATE`, `GENERATE_ROD`, `GENERATE_LONG_ROD`, `GENERATE_BOLT_SCREW`, `GENERATE_GEAR` — cross-checked against every flag `ToolRecipeHandler.java` actually gates recipes on), and `.toolStats(ToolProperty.Builder.of(harvestSpeed, attackDamage, durability, harvestLevel).enchantability(e).build())`.
- **8 metals GTCEu already ships** (Bronze, Manganese, Osmium, Palladium, Platinum, Silver, Steel, Tin) get `PropertyKey.TOOL` + the same flags attached to the *existing* `GTMaterials.X` instance in `modify()`, guarded by `if (material.hasProperty(PropertyKey.TOOL)) return;` so a future GTCEu version that adds its own tool line for one of these isn't clobbered.

`MythicalMaterials.ALL_MATERIALS` accumulates every `Material` touched (new + reused) — this is what `MythicalCreativeTabs` iterates, so nothing else needs to track which materials exist.

**Colors must come from the real texture, not guessed from the metal's name/lore.** GTCEu tints tool textures via `IGTTool#tintColor()` reading `Material#getMaterialARGB()`. Guessing (e.g. "Runite" → emerald green because "rune" sounds green) produces tools that visibly clash with the base mod's actual item color. Compute it instead:

```python
from PIL import Image
img = Image.open('<mythicmetals-repo>/src/main/resources/assets/mythicmetals/textures/item/<name>_ingot.png').convert('RGBA')
r=g=b=n=0
for (pr,pg,pb,pa) in img.getdata():
    if pa > 128: r+=pr; g+=pg; b+=pb; n+=1
print(f'#{r//n:02X}{g//n:02X}{b//n:02X}')
```

**Ingot vs gem**: most metals use `<name>_ingot` in the base mod (`item/ItemSet.java`) and should be `.ingot(...)`. A few are single standalone items with no separate ingot form (Starrite, Unobtainium: item id is just `mythicmetals:<name>`; Morkite: same, registered directly rather than via `ItemSet`) — these are `.gem(...)`, and their forge tag goes in `gems/<name>.json` not `ingots/<name>.json`. Star Platinum is a special case: it *is* `.ingot(...)`-typed but its item id has no `_ingot` suffix (`mythicmetals:star_platinum`, `ItemSet`'s `bareIngotId` flag) — check the base mod's `item/MythicItems.java`/`ItemSet.java` before assuming the naming pattern for a new metal.

### Item reuse — `data/forge/tags/items/{ingots,gems}/<metal>.json`

One file per metal, each tagging in the corresponding `mythicmetals:<metal>_ingot` (or bare-name/gem variant) item. This is the entire mechanism that makes GT use real items instead of generating duplicates — confirmed from `ChemicalHelper.getItems()`: it checks the tag first, only falls back to auto-generating an item if the tag is empty. Get the tag name wrong (wrong prefix, wrong item id) and GT silently generates its own duplicate `mythical_gt:<metal>_ingot` item instead of failing loudly — always cross-check a new metal's tag against a real server boot (see "Testing"), not just a JSON-syntax check.

### Creative tab — `MythicalCreativeTabs.java`

A dedicated `mythical_gt:main` tab (title `itemGroup.mythical_gt.main`) separate from GTCEu's own shared "Tool" tab. `displayItems` iterates `MythicalMaterials.ALL_MATERIALS × GTToolType.getTypes().values()`, calling `ToolHelper.get(type, material)` (the same lookup GTCEu itself uses everywhere) and listing whichever combinations aren't `ItemStack.EMPTY`. No new items are registered by this class — it's purely a second place to find the same items.

### Recipes — `data/mythical_gt/recipes/alloy_smelter/*.json` and `data/mythicmetals/recipes/alloys/*.json`

GTCEu recipes load through the exact same vanilla `RecipeManager`/datapack pipeline as any other recipe — `GTRecipeTypes.register(name, ...)` registers a `GTRecipeSerializer` into `BuiltInRegistries.RECIPE_SERIALIZER` under the recipe type's own id (e.g. `gtceu:alloy_smelter`). **Confirmed schema** (from `GTRecipeSerializer`'s Codec + `Content.codec()` + `SizedIngredient`):

```json
{
  "type": "gtceu:alloy_smelter",
  "inputs": {
    "item": [
      { "content": { "type": "gtceu:sized", "count": 2, "ingredient": { "item": "minecraft:copper_ingot" } } }
    ],
    "eu": [ { "content": 32 } ]
  },
  "outputs": {
    "item": [
      { "content": { "type": "gtceu:sized", "count": 3, "ingredient": { "item": "mythicmetals:bronze_ingot" } } }
    ]
  },
  "duration": 100
}
```

Two easy-to-miss gotchas that silently produce `GregTechCEu ERROR: No key content in MapLike[...]` / `Not a JSON object: <n>` at load time rather than a helpful message:

- **Every** input/output/tick-input entry needs the `"content"` wrapper — `Content.codec()` requires a `fieldOf("content")`. `{"count": 2, "ingredient": {...}}` directly (no wrapper) fails.
- Item entries need `"type": "gtceu:sized"` inside the content to route through Forge's custom-ingredient dispatch (`SizedIngredient.TYPE`, registered via `CraftingHelper.register`) — plain `{"item": "..."}` has no count support without it. `ItemRecipeCapability`'s serializer is literally vanilla `Ingredient.fromJson`/`toJson`, so without the `"type"` marker you just get a count-less ingredient, not an error, which is a worse trap in EU/duration-heavy testing than an outright failure.

`data/mythicmetals/recipes/alloys/*.json` — same filenames/ids as the base mod's own alloy recipes, each just `{"type": "minecraft:crafting_shapeless", "conditions": [{"type": "forge:false"}], ...}`. Forge's universal recipe-conditions support means a `forge:false`-conditioned recipe at the same id simply never registers, deleting the base mod's vanilla recipe without touching that project's files at all.

## Testing

**`./gradlew runClient`/`runServer`/`runData` in this project will crash loading `mythicmetals.jar` with `NoSuchFieldError: f_279569_` (and the reverse is also true — `gtceu.jar` crashes the same way inside Mythic Metals CE Port's own ForgeGradle-6 dev sandbox with a mixin `@Shadow field ... was not located` error).** This is confirmed to be a dev-toolchain artifact: ForgeGradle-6-built and NeoForge-ModDevGradle-built mod jars bake SRG refmaps against different internal mapping snapshots, and neither dev sandbox can load the other's jar correctly, even pinning identical `forge_version`/`minecraft_version`. **It is not a real bug** — a genuine standalone Forge install (dedicated server or a real client instance, i.e. *not* either project's `run*` Gradle task) loads both mods fine.

To actually verify a change against Mythic Metals CE Port + GTCEu together:

1. Build both jars (`./gradlew build` in each project).
2. Get a real Forge 1.20.1-47.4.10 server: `curl -sL -o forge-installer.jar https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-installer.jar && java -jar forge-installer.jar --installServer`
3. Drop `mythicmetals-*.jar`, `mythicalgt-*.jar`, the **full** (not `-slim`) `com.gregtechceu.gtceu:gtceu-1.20.1:<version>` jar from `https://maven.gtceu.com`, and `com.lowdragmc.ldlib:ldlib-forge-1.20.1:<version>` from `https://maven.firstdark.dev/snapshots/` into that install's `mods/`.
4. `echo eula=true > eula.txt`, run `bash run.sh nogui` (or `run.bat` on Windows), and grep the log/`logs/debug.log` for `ERROR`/`NoSuchField`/`Mod Loading has failed` — a clean run reaches `Done (...)!`.
5. For a full item-registration audit (e.g. counting exactly what got registered, catching a wrong tag silently generating a duplicate item), pass `-Dforge.logging.markers=REGISTRIES` as a JVM arg (`user_jvm_args.txt`) and grep `logs/debug.log` for `Captured registration for entry <namespace>:<id> of type minecraft:item`.

This is also the only reliable way to playtest visually — either point an existing Forge-1.20.1-47.4.10 launcher profile (PrismLauncher, CurseForge, etc.) at the same 4 jars, or connect a client to the standalone server above.
