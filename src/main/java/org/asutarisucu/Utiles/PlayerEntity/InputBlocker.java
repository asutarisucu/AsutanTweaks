package org.asutarisucu.Utiles.PlayerEntity;

import net.minecraft.client.option.KeyBinding;

public class InputBlocker {
    private final KeyBinding jumpKey;

    public InputBlocker(KeyBinding jumpKey) {
        this.jumpKey = jumpKey;
    }

    // 物理キーボードのジャンプキー入力を無効化する
    public void blockJumpKey() {
        KeyBinding.setKeyPressed(jumpKey.getDefaultKey(), false); // ジャンプキーを物理的に「離された」状態にする
        jumpKey.setPressed(false); // ジャンプキーの内部状態も解除
    }

    // 物理キーボードのジャンプキー入力を再び有効化する
    public void restoreJumpKey() {
        // 自然な入力ができるようにするため、何もしないか、必要なら初期化
        jumpKey.setPressed(false); // 内部状態を再度リセット（念のため）
    }
}

