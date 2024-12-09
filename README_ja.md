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
> 各dimensionごとに奈落と認識する高さを変更できます。<br>
> `VoidDisconnect`をtrueにすることで奈落でロケット花火かエリトラがなくなった際に自動でゲームを切断します。
> ただしゲームを再開しても助かりません。