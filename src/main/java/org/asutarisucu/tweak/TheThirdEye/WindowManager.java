package org.asutarisucu.tweak.TheThirdEye;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.InfoUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.swing.*;
import java.awt.*;

public class WindowManager {
    public static JFrame window;
    @Environment(EnvType.CLIENT)
    public static void openWindow(){
        if(GraphicsEnvironment.isHeadless()){
            //ヘッドレス環境ならスキップ
            InfoUtils.printActionbarMessage("This Env is HeadLess!!"+ GuiBase.TXT_RED);
            return;
        }
        if (window==null)return;
        //既にWindowがあるなら閉じる
        if(window.isVisible()){
            window.removeAll();
        }else{
            //Windowがないなら新しく作る
            window.toFront();
            //windowの初期化
            window=new JFrame("Minecraft-Third Eye by AsutanTweaks");
            window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            window.setSize(800,600);

            //windowの設置
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        }
    }
}
