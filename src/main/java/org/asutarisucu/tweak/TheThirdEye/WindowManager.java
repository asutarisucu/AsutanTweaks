package org.asutarisucu.tweak.TheThirdEye;

import org.asutarisucu.Configs.FeatureToggle;

import javax.swing.*;

public class WindowManager {
    public static JFrame window;
    public static void openWindow(){
        if(FeatureToggle.THE_THIRD_EYE.getBooleanValue()){
            if(window !=null){
                closeWindow();
            }else{
                window =new JFrame();
            }
        }
    }
    public static void closeWindow(){
        window =null;
    }

}

