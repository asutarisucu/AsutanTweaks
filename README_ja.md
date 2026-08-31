# AsutanTweaks
[![Static Badge](https://img.shields.io/badge/EnglishText-blue)](README.md)<br>
[![License](https://img.shields.io/github/license/asutarisucu/Asutantweaks.svg)](https://opensource.org/licenses/MIT)<br>

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
### BlockUpdateViewer
>ブロックを設置または破壊したときにブロックアップデートを受け取るブロックをビジュアライズします。<br>
>ブロックアイテムを持っているときに`PlacementUpdateViewer`を有効にすると、アップデート対象を**赤色**でハイライト表示します。<br>
>採掘ツールを持っているときに`BreakingUpdateViewer`を有効にすると、アップデート対象を**青色**でハイライト表示します。<br>
>`UpdateViewInstantOnly`を有効にすると、ピストンやオブザーバーなどスケジュール更新されるブロックを除いた即時アップデートのみを表示します。<br>
>`UpdateSuppressionView`を有効にすると、アップデートチェーンにアップデート抑制を引き起こす可能性のある座標が含まれている場合にHUDに警告（`CCE suppress Ready`）を表示します。
### ThirdEye
>ワールドを別ウィンドウに第2視点（自由に動かせるカメラ）から描画します。<br>
>有効にすると、第2カメラは現在の自分の視点位置から開始します。<br>
>`ThirdEyeMovement`を有効にすると、`WASD`（移動）・`Space`（上昇）・`Shift`（下降）・`Ctrl`（加速）でカメラを操作できます。<br>
>移動速度は[tweakeroo](https://github.com/maruohon/tweakeroo)のフライ速度プリセットに従います。
### VisualiseLazyEntity
>サーバーがシミュレーションしていないチャンク（Lazy Chunk）にあるエンティティを、本来のサーバー位置に固定して正しく描画します。<br>
>通常このようなアイテムや落下ブロックはクライアント側で落下し続け、着火済みTNTはクライアント側の信管が尽きると消えてしまいますが、本機能はそれらを本来の位置で静止させます。<br>
>生物（Mob）は元々正しく表示されるため対象外です。<br>
>シングルプレイでは統合サーバーから実際の状態を読み取ります。<br>
>マルチプレイで使用したい場合は[tweakermore](https://github.com/Fallen-Breath/tweakermore/tree/master)の`serverDataSyncer`を有効にして、
>[Carpet](https://github.com/gnembon/fabric-carpet)の`debugNbtQueryNoPermission`を有効にしてください。<br>
>`LazyEntitySyncInterval`でサーバー位置を取得する間隔を調整できます。
### PickBlockUltimate
>ミドルクリックのピックブロックからリーチ制限を取り除きます。読み込み済みチャンク内なら見えている限りどこでも取得できます。<br>
>`PickBlockReach`で最大距離を設定できます（既定256ブロック）。<br>
>`Pick Block State`キー（既定`Left Alt`）を押しながらピックすると、ブロックの**ステート**（旧state）も一緒にコピーします。
>コンポスターの堆肥レベル、リピーターの遅延、ハーフブロックの上下などが対象で、設置するとその状態が再現されます。
>バニラに同等の機能はありません。アイテムを生成する必要があるためクリエイティブモードが必要です。<br>
>これはバニラのCtrl+ピック（ブロックエンティティのNBTをコピー）とは別物です。両者は分けてあり、
>通常リーチ内で修飾キー無しのCtrl+ピックをした場合はバニラに処理を戻すのでNBTのコピーはそのまま使えます。
>ステート用のキーの既定がCtrlではなくAltなのはこのためです。<br>
>クロスヘア上にエンティティがある場合はバニラの処理に任せるため、エンティティのピックブロックは変わりません。
### WorldEditGUI
>現在の[WorldEdit](https://enginehub.org/worldedit)の直方体選択範囲を、6面すべてに格子状に描画します。<br>
>選択範囲はWorldEdit自身のCUIプラグインチャンネル経由で取得するため、シングルプレイでもWorldEdit導入済みサーバーでも動作します。
>ワールド参加時および機能を有効にしたときに`//we cui`でCUI対応を通知します。<br>
>Pos1とPos2はそれぞれ別の色（`WE Pos1 Color`・`WE Pos2 Color`）で枠取りされ、片方だけ設定した時点から表示されます。<br>
>`WEGridColor`・`WEEdgeColor`・`WEGridSpacing`で見た目を調整できます。`WEGridMaxLines`は線の本数の上限で、
>非常に大きな選択範囲では自動的に間隔を倍にして収めます。<br>
>描画対象は直方体選択のみです。WorldEditの他の形状が選ばれた場合は誤った箱を描かないよう表示をクリアします。
### ClearBlockRender
>WorldEditの選択範囲を**背景透過**の動画として録画します。選択したブロックだけが描画され、周囲は一切写りません。<br>
>録画中もワールドは動き続けるため、稼働中のレッドストーン装置をそのまま収録できます。<br>
>キーは2つあります。`Clear Block Render Screen`で設定画面が開きます（設定はコンフィグ一覧ではなくこの画面にあります）。
>`Clear Block Render Rec`は画面を開かずに録画を開始・停止するので、装置の準備が整った瞬間に録り始められます。
>出力先はゲームディレクトリの`clear_block_render/`です。<br>
>`Width`/`Height`でゲームウィンドウとは独立した解像度を、`FPS`でフレームレートを指定します。<br>
>`Projection`で`ISOMETRIC`（正投影）か`PERSPECTIVE`（`FOV`使用）を選択できます。`Yaw`・`Pitch`・`Zoom`で画角を決め、
>`Orbit`を設定すると選択範囲の周りをその角速度（度/秒）でカメラが回ります。<br>
>**[ffmpeg](https://ffmpeg.org/)が必要です。**`PATH`に無い場合は`FFmpeg Path`にパスを設定してください。
>アルファチャンネルを保持できるのはWebM(VP9)とMOV(ProRes 4444)だけなので、出力形式（`Format`）はこの2種類です。<br>
>設定画面には設定項目の隣に**ライブプレビュー**が出るので、いじりながら画角を確認できます。
>プレビュー内はマウスで操作します。ドラッグで回転、右ドラッグまたはShift+ドラッグで平行移動、ホイールでズーム、
>中クリックで平行移動とズームをリセットします。操作結果は`Yaw`・`Pitch`・`Zoom`・`Pan X`・`Pan Y`に書き戻るので、
>マウスで大まかに合わせてから数値で詰められます。<br>
>`Playback Speed`で再生速度を指定できます。1.0が等速、0.5がスローモーション、2.0が倍速です。
>フレームの取得自体は`FPS`のままなので、大きく遅くする場合は`FPS`も上げないとカクつきます。<br>
>ピストンが押しているブロックも描画します。これらはワールドのブロックステートに含まれないため、
>対応しないと押している間ずっと録画から消えてしまいます。<br>
>設定画面の`Save Image`で1フレームを透過PNGとして書き出せます。ffmpegは不要なので、画角の確認はこれが早いです。<br>
>選択範囲内の液体は描画されません。描画対象はブロックモデルと、`Block Entities`有効時のブロックエンティティのみです。
