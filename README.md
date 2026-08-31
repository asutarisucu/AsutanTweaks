# AsutanTweaks
[![Static Badge](https://img.shields.io/badge/JapaneseText-blue)](README_ja.md)<br>
[![License](https://img.shields.io/github/license/asutarisucu/Asutantweaks.svg)](https://opensource.org/licenses/MIT)<br>

AsutanTweaks is a FabricMOD that runs client-side only.<br>
It is developed exclusively for asutarisucu <br>
Any requests for additional features will also be ignored.

## How to
Press `B`+`C`to open config GUI.<br>
you can also access the config GUI via [Mod Menu](https://legacy.curseforge.com/minecraft/mc-mods/modmenu)

## Features
### LastUseCancel
>If you have only one item in your main hand you cannot use it.
### ItemRestock
>This is a feature implemented in [tweakeroo](https://github.com/maruohon/tweakeroo) as well, but when the hot bar is low on items, the items are replenished from the inventory.<br>
>The timing of replenishment can be changed by changing the `Restock Count`.<br>
>If `LastUseCancel` is enabled, it leaves one item in the inventory to be replenished.
### AutoFillInventory
>Pick up items from the container so that the count of items in the Player's inventory is maximized.
`LastUseCansel` and `ItemRestock` can be used together to replenish items without changing the structure of the items in the inventory.
This means you can always transport the optimal amount of items.
### DisableVoidDive
>When you try to fall into the Void, it automatically stops you from falling with fireworks.<br>
>For each dimension, the height at which it is recognized as void can be changed.<br>
>Setting `VoidDisconnect` to True will automatically disconnect the game when there are no fireworks or elytra.<br>
>Resuming the game, however, will not save your life.
### SchematicRestrictionWhiteList
>You can freely specify the BlockState to be checked in TweakerMore's `schematicBlockRestriction`<br>
>Enter the BlockState you wish to check in the `RestrictionStateWhiteList` in lower or upper case.
### SimpleEntityRender
>Simplifies Entity rendering and improves fps.<br>
>If you have a large number of Entities clustered together, combine them into one. <br>
>Enable `SimpleEntityRenderCount` to see how many Entities are grouped there. <br>
>It also prevents ItemEntity from moving in a depressing way. <br>
>Currently, it is only available for ItemEntity and MobEntity.
### EnderChestMaterialList
>Add the contents of the ender chest to the count in [Litematica](https://github.com/maruohon/litematica)'s MaterialList.<br>
>From the `EnderChestMaterialListWhiteList` and `EnderChestMaterialListBlocklist`, 
>you can specify the colors of the shulker box boxes to exclude from the count.<br>
>If you want to use it in multiplayer, enable `serverDataSyncer` in [tweakermore](https://github.com/Fallen-Breath/tweakermore/tree/master)
>and `debugNbtQueryNoPermission` in [Carpet](https://github.com/gnembon/fabric-carpet).<br>
>You may not get it right the first time, but try several times.
### SearchHighlight
>Highlight the location of the item set in HighLightItemList.
>`SearchBlockHighlight` displays the block if it is located,
> and `SearchContainerHighlight` displays the container in which the item is located.
> The color and range can be set for each,
>  and you can also register your main hand items using hot keys.
### BlockUpdateViewer
>Visualises which blocks will receive block updates when you place or break a block.<br>
>Enable `PlacementUpdateViewer` to highlight update targets in **red** while holding a block item.<br>
>Enable `BreakingUpdateViewer` to highlight update targets in **blue** while holding a mining tool.<br>
>Enable `UpdateViewInstantOnly` to show only instant updates, excluding schedulable blocks such as pistons and observers.<br>
>Enable `UpdateSuppressionView` to display a HUD warning (`CCE suppress Ready`) when the update chain contains positions that could trigger update suppression.
### ThirdEye
>Renders the world from a second, free-moving viewpoint in a separate OS window.<br>
>When the feature is enabled the second camera starts at your current eye position.<br>
>Enable `ThirdEyeMovement` to fly that camera with `WASD` (move), `Space` (up), `Shift` (down) and `Ctrl` (sprint);<br>
>the movement speed follows [tweakeroo](https://github.com/maruohon/tweakeroo)'s fly-speed presets.
### VisualiseLazyEntity
>Renders entities that sit in chunks the server is not simulating (lazy chunks) at their correct server position.<br>
>Normally such items and falling blocks keep falling on the client, and primed TNT disappears once its client-side fuse runs out; this feature keeps them frozen in place at their real position instead.<br>
>Living entities (mobs) already display correctly, so they are left untouched.<br>
>In singleplayer the real state is read from the integrated server.
>To use it in multiplayer, enable `serverDataSyncer` in [tweakermore](https://github.com/Fallen-Breath/tweakermore/tree/master)
>and `debugNbtQueryNoPermission` in [Carpet](https://github.com/gnembon/fabric-carpet).<br>
>`LazyEntitySyncInterval` controls how often the server position is sampled.
### PickBlockUltimate
>Removes the reach limit from middle-click pick block — anything you can see in a loaded chunk can be picked.<br>
>`PickBlockReach` sets the maximum distance (default 256 blocks).<br>
>Hold the `Pick Block State` hotkey (default `Left Alt`) while picking to also copy the block's **state properties** —
>a composter's fill level, a repeater's delay, the half a slab sits in — so placing the item reproduces that state.
>Vanilla has no equivalent for this; creative mode is required, as the item has to be created.<br>
>That is a different thing from vanilla's ctrl+pick, which copies block entity NBT. The two are kept separate:
>a plain ctrl+pick on a block within normal reach is handed back to vanilla so its NBT copy still works,
>which is why the state hotkey defaults to Alt rather than Ctrl.<br>
>Entities under the crosshair are still handled by vanilla, so entity pick block is unchanged.
### WorldEditGUI
>Draws the current [WorldEdit](https://enginehub.org/worldedit) cuboid selection as a lattice on its six faces.<br>
>The selection is read over WorldEdit's own CUI plugin channel, so it works both in singleplayer and on a
>server that has WorldEdit installed. CUI support is announced with `//we cui` when you join or enable the feature.<br>
>Each selection point is outlined in its own colour (`WE Pos1 Color`, `WE Pos2 Color`) as soon as it is set,
>without waiting for the other one.<br>
>`WEGridColor`, `WEEdgeColor` and `WEGridSpacing` control the appearance; `WEGridMaxLines` caps the line count,
>doubling the spacing automatically for very large selections.<br>
>Only cuboid selections are drawn — WorldEdit's other shapes clear the display rather than showing a wrong box.
### ClearBlockRender
>Records the WorldEdit selection as a video with a **transparent background** — only the selected blocks are drawn,
>nothing around them. The world keeps running while recording, so a working redstone machine is captured in motion.<br>
>Two hotkeys: `Clear Block Render Screen` opens the settings screen — they are there rather than in the option list —
>and `Clear Block Render Rec` starts and stops the capture without opening anything, so a circuit can be recorded
>the moment it is set up. Output goes to `clear_block_render/` in your game directory.<br>
>`Width`/`Height` set the capture resolution independently of the game window, `FPS` the frame rate.<br>
>`Projection` picks `ISOMETRIC` (orthographic) or `PERSPECTIVE` (with `FOV`); `Yaw`, `Pitch` and `Zoom`
>frame the shot, and `Orbit` rotates the camera around the selection at that many degrees per second.<br>
>**Requires [ffmpeg](https://ffmpeg.org/)** — set `FFmpeg Path` if it is not on your `PATH`. Only VP9-in-WebM and
>ProRes 4444-in-MOV keep an alpha channel, so those are the two output formats (`Format`).<br>
>The settings screen shows a **live preview** of the shot next to the settings, so framing can be judged while
>adjusting it. The preview is navigated with the mouse: drag to orbit, right-drag or
>shift-drag to pan, wheel to zoom, middle-click to reset the pan and zoom. Everything it changes is written back
>to `Yaw`, `Pitch`, `Zoom`, `Pan X` and `Pan Y`, so a shot set up by hand can still be fine-tuned by number.<br>
>`Playback Speed` sets how fast the clip plays: 1.0 real time, 0.5 slow motion, 2.0 double speed. Frames are still
>captured at `FPS`, so a very slow setting looks choppy unless `FPS` is raised to match.<br>
>Blocks a piston is carrying are drawn too — they are not part of the world's block states, so they would
>otherwise vanish from a recording for the whole push.<br>
>`Save Image` on the settings screen writes a single frame as a transparent PNG, without ffmpeg — the quickest way
>to check the framing.<br>
>Fluids inside the selection are not drawn — only block models and (optionally, via `Block Entities`) block entities.
