# AsutanTweaks
[![Static Badge](https://img.shields.io/badge/JapaneseText-blue)](README_ja.md)<br><br>
[![License](https://img.shields.io/github/license/asutarisucu/Asutantweaks.svg)](https://opensource.org/licenses/MIT)

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
 
