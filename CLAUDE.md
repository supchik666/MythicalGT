# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A template repository for building **GregTech Modern (GTCEu) addons** for Minecraft 1.20.1 on Forge (via NeoForged's `legacyforge` ModDev tooling). It is not an app with business logic — it's scaffolding meant to be renamed/cloned and extended. Expect the current code to be mostly placeholder/example content (package `com.example.examplemod`, mod id `examplemod`).

## Commands

Use the Gradle wrapper (`./gradlew` on macOS/Linux, `gradlew.bat` on Windows) — do not rely on a system-installed Gradle.

- `./gradlew build` — compile and build the mod jar (this is also what CI runs, see `.github/workflows/gradle.yml`)
- `./gradlew runClient` — launch a dev Minecraft client with the mod loaded
- `./gradlew runServer` — launch a dev dedicated server
- `./gradlew runData` — run Forge data generators (outputs to `src/generated/resources/`, which is registered as an extra resources source set in `build.gradle`)
- `./gradlew spotlessApply` — auto-format Java sources per the project style
- `./gradlew spotlessCheck` — verify formatting without modifying files

There is no test source set (`src/test`) in this template; no test task is meaningful yet.

### Code formatting

Spotless (Eclipse formatter) is wired up but **optional by default** — it does not run as part of `build`/`check`. Formatting rules live in `spotless/spotless.eclipseformat.xml` (Eclipse formatter profile) and `spotless/spotless.importorder` (import ordering), wired together in `gradle/scripts/spotless.gradle`. Formatting can be locally disabled for a block with `// spotless:off` / `// spotless:on` comments.

## Architecture

### Configuration flows from `gradle.properties`

`gradle.properties` is the single source of truth for mod identity (`mod_id`, `mod_name`, `maven_group`, `archives_base_name`, `mod_author`, `mod_version`, `mod_license`) and pinned dependency versions (`minecraft_version`, `forge_version`, `gtceu_version`, `ldlib_version`, `registrate_version`, `configuration_version`, `jei_version`, `emi_version`). `build.gradle` reads these as project properties (implicit Groovy binding, e.g. `project.mod_id`) to configure the NeoForge `legacyForge` extension, dependency coordinates, and run configurations.

These same properties are also injected as `${...}` template tokens into resource files at build time via the `processResources` task's `filesMatching("META-INF/mods.toml")` block — see `src/main/resources/META-INF/mods.toml`. If you rename the mod, update `gradle.properties` first; the Java package under `src/main/java/com/example/examplemod` and the `maven_group`/`mod_id` should generally be renamed together to stay consistent, but nothing enforces this automatically — it's a manual rename across package + properties.

### Two distinct integration points with GregTech

- **`ExampleMod.java`** — the standard Forge `@Mod` entry point. Registers listeners on the mod event bus (`FMLCommonSetupEvent`, `FMLClientSetupEvent`, material/recipe-type/machine/sound registration events) and owns the addon's `GTRegistrate` instance (`EXAMPLE_REGISTRATE`), which is GregTech's wrapper around Registrate for content registration.
- **`ExampleGTAddon.java`** — implements GTCEu's `IGTAddon` and is annotated `@GTAddon`, which is how GTCEu's addon service-loader discovers this mod as a GregTech addon specifically (separate from Forge's normal mod loading). This is where addon-specific hooks live: exposing the `GTRegistrate` back to GTCEu, registering tag prefixes, recipes, and elements. Most of its methods are stubbed with commented-out examples — that's the intended extension surface for a real addon.

When adding new GT content (materials, machines, recipes, recipe types, sounds), the pattern is: register the content class's `init()` call from the appropriate stub method in `ExampleMod.java` (materials/recipe-types/machines/sounds) or `ExampleGTAddon.java` (recipes/tag prefixes/elements), following the commented-out examples already present.

### Mixins

`src/main/java/com/example/examplemod/mixin/DummyMixin.java` is a non-functional placeholder mixin, registered in `src/main/resources/examplemod.mixins.json`. The mixin config file name is templated into `build.gradle`'s `mixin {}` block and the jar manifest via `mod_id`. Prefer GregTech/Forge APIs over mixins where possible; mixins here are scaffolding for cases that genuinely require bytecode-level modification.

### Dependency repositories

`build.gradle` pulls from several mod-specific Maven repos beyond Maven Central (GTCEu's own maven, FirstDarkDev, Registrate's tterrag maven, BlameJared for Patchouli/JEI, TerraformersMC for EMI, CurseMaven for Jade). If a new dependency's artifact isn't resolving, check whether its host repo needs to be added here.
