# AsutanTweaks
[![Static Badge](https://img.shields.io/badge/EnglishText-blue)](README.md)<br><br>
[![License](https://img.shields.io/github/license/asutarisucu/Asutantweaks.svg)](https://opensource.org/licenses/MIT)

AsutanTweaksはクライアントサイドで動作するFabricMODです。<br>
このMODはasutarisucuのために作成されました。<br>
機能の追加に関しての要望はすべて無視します。

## 使用方法
`B`+`C`キーを押すことでConfig GUIを開くことができます。<br>
また、[Mod Menu](https://legacy.curseforge.com/minecraft/mc-mods/modmenu)を使用して開くことができます。

### Features
### LastUseCancel
>持っているアイテムがあと一つの場合、それを使用することができなくなります。
### ItemRestock
>[tweakeroo](https://github.com/maruohon/tweakeroo)でも実装されている機能ですが、ホットバーのアイテムが少なくなるとインベントリから自動で補充されます。<br>
> `LastUseCancel`を有効にしている場合、補充する際にインベントリにアイテムを一つ残して補充します。
### AutoFillInventory
>自分の持っているアイテムの個数が最大になるようにコンテナからアイテムを補充します。<br>
> `LastUseCancel`と`ItemRestock`を併用することでインベントリのアイテムの配置を変更せずに素早くアイテムを補充できます。<br>
> これは常に最適な量のアイテムを運ぶことができることを意味しています。
### DisableVoidDive
> 奈落に落ちることを防ぎます。
> 各ディメンションごとに奈落と認識する高さを変更できます。<br>
> `VoidDisconnect`を有効にすることで奈落でロケット花火かエリトラがなくなった際に自動でゲームを切断します。<br>
> ただしゲームを再開しても助かりません。
### SchematicRestrictionWhiteList
>TweakerMoreの`schematicBlockRestriction`でチェックするブロックの状態を自由に変更できます。<br>
> `RestrictionStateWhiteList`というリストにチェックしたいブロックの状態をすべて小文字かすべて大文字で入力してください。
### SimpleEntityRender
>エンティティの描画を簡単にすることでfpsを向上させます。<br>
>エンティティが密集している場合、それらを一つにまとめて描画します。<br>
>`SimpleEntityRenderCount`が有効な場合、そこにいくつのエンティティがまとめられているのかを表示します。<br>
> アイテムエンティティの鬱陶しく上下する動きを抑制します。<br>
> 現時点ではモブエンティティとアイテムエンティティにのみ有効です。
### EnderChestMaterialList
>エンダーチェストの中身を[Litematica](https://github.com/maruohon/litematica)の材料リストのカウントに加えます。<br>
> `EnderChestMaterialListWhiteList`と`EnderChestMaterialListBlocklist`から、カウントから除外するシュルカーボックスの箱の色を指定できます。<br>
> マルチプレイで使用したい場合は[tweakermore](https://github.com/Fallen-Breath/tweakermore/tree/master)の`serverDataSyncer`を有効にして、
> [Carpet](https://github.com/gnembon/fabric-carpet)の`debugNbtQueryNoPermission`を有効にしてください。<br>
> 一度目でうまく取得できない場合がありますが何度か試してください。
### SearchHighlight
>`HighLightItemList`に設定したアイテムのある場所をハイライト表示する。<br>
> `SearchBlockHighlight`ではアイテムがブロックとして設置されている場合に表示し、
> `SearchContainerHighlight`ではアイテムが中にあるコンテナを表示する。<br>
> それぞれ色と範囲を設定することができ、ホットキーから自分の持っているアイテムを登録することもできる。