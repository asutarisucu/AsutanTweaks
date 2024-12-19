package org.asutarisucu.tweak.TheThirdEye;

import javax.swing.*;
import java.awt.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;

import static org.lwjgl.opengl.GL11C.glFlush;

public class ThirdEyeWindow extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Minecraftのレンダリングシステムを呼び出す
        RenderSystem.initRenderThread();  // 初期化
        RenderSystem.clear(1, true);  // 画面のクリア

        // ここでMinecraftの描画を行う
        // Minecraftのレンダリングコード（仮想的な例）
        renderMinecraft();

        // 描画完了後、Swingのコンポーネントに反映
        glFlush();
    }

    private void renderMinecraft() {
        // Minecraftの描画


    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minecraft in Swing");
            ThirdEyeWindow panel = new ThirdEyeWindow();
            frame.add(panel);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

}
