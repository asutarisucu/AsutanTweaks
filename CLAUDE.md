# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AsutanTweaks is a client-side-only Fabric mod for Minecraft, built with multi-version support using the [ReplayMod preprocessor](https://github.com/Fallen-Breath/preprocessor). It integrates deeply with malilib, tweakeroo, tweakermore, litematica, and item-scroller.

## Build Commands

```powershell
# Build all versions and collect jars into build/libs/
./gradlew buildAndGather

# Build a specific version subproject
./gradlew :1.21.1:build

# Run the Minecraft client for a specific version (IntelliJ run configs also available)
./gradlew :1.21.1:runClient

# Run the Minecraft server for a specific version
./gradlew :1.21.1:runServer
```

Supported versions are defined in `settings.json` and currently include: `1.19.4`, `1.20.1`, `1.20.4`, `1.20.6`, `1.21.1`, `1.21.11`, `26.1`, `26.2`.

`26.1` and `26.2` use the year-based Minecraft version format (no leading `1.`). They require **Java 25** and use Mojang's official mappings (Yarn/Intermediary discontinued from `26.1` on).

`26.2` reworked the render and screen APIs again on top of `26.1`: the current screen moved from `Minecraft` to `Minecraft.gui`, the main render target from `Minecraft` to `GameRenderer.mainRenderTarget()`, level extraction from `LevelRenderer.extractLevel` to `Minecraft.levelExtractor.extract`, and `Tesselator` / `MultiBufferSource` / `RenderType.draw(MeshData)` were removed in favour of `SubmitNodeCollector`. Code shared with `26.1` therefore needs `//#if MC >= 260200` branches in those places.

## Architecture

### Multi-Version Source

Source lives in `src/main/java/` and is shared across all versions via the preprocessor. Version-specific code uses preprocessor directives:

```java
//#if MC >=12101
//$$ // code for 1.21.1+
//#endif
```

Each version subproject in `versions/<mc-version>/` contains only a `gradle.properties` with that version's dependency versions. The build file for every subproject is `common.gradle` at the root (set via `settings.gradle`). After preprocessing, output lands in `versions/<mc-version>/build/preprocessed/`.

### Mod Entrypoint Flow

1. `AsutanTweaks` (client entrypoint) → registers `InitHandler` with malilib's `InitializationHandler`
2. `InitHandler.registerModHandlers()` → registers config handler, keybind providers, and calls `Reference.LoadEvent()`
3. `Reference.LoadEvent()` → registers Fabric event listeners (currently `LastUseCancel.UseBlockEvents()`)

### Configuration System

Config is malilib-based. Three categories:
- `Configs.Generic` — `ConfigInteger`, `ConfigDouble`, `ConfigBoolean`, `ConfigStringList`, `ConfigOptionList` values. Serialized to `asutantweaks.json` in the Minecraft config dir.
- `FeatureToggle` (enum) — hotkey-togglable boolean features. Each enum value corresponds to a feature.
- `Hotkeys` — standalone hotkeys (e.g., open config GUI: `B+C`).

The GUI is opened via `B+C` and also accessible through Mod Menu.

### Feature Structure

Each feature follows this pattern:
- `src/main/java/org/asutarisucu/tweak/<FeatureName>/` — core logic
- `src/main/java/org/asutarisucu/mixin/<FeatureName>/` — Mixin injections into Minecraft/mod classes
- Toggle in `FeatureToggle` enum; config options in `Configs.Generic`

Current features: `LastUseCancel`, `ItemRestock`, `AutoFillInventory`, `DisableVoidDive`, `SchematicRestrictionWhiteList`, `SimpleItemEntityRender`, `EnderChestMaterialList`.

### Utility Packages

- `org.asutarisucu.Utiles.Block` — BlockState helpers
- `org.asutarisucu.Utiles.Entity` — ItemEntity / MobEntity helpers
- `org.asutarisucu.Utiles.Inventory` — Inventory and Screen helpers
- `org.asutarisucu.Utiles.Render` — Rendering utilities
- `org.asutarisucu.Utiles.tweakerMore` — TweakerMore integration helpers

### Mixin Registration

All mixins are declared in `src/main/resources/asutantweaks.mixins.json`. Server-safe mixins go in `"mixins"`, client-only go in `"client"`. The `conditionalMixin` library is available for conditional mixin application at runtime.

### Version-specific dependency versions

Per-version dependency versions (minecraft, yarn mappings, fabric API, malilib, tweakeroo, litematica, etc.) are declared in `versions/<mc-version>/gradle.properties`. Common properties (mod ID, version, loader version) are in the root `gradle.properties`.

`26.1` and `26.2` have no `yarn_mappings` property — `loom.officialMojangMappings()` is used instead (see `common.gradle`). The preprocessor integers are `260100` and `260200`, keeping them numerically above all `1.x.x` values (e.g., `1.21.1` = `12101`).
