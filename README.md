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