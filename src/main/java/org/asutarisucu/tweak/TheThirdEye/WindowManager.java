package org.asutarisucu.tweak.TheThirdEye;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.mixin.TheThirdEye.IMixinMinecraftClient;

import java.util.OptionalInt;

public class WindowManager {
    private static final Window defaultWindow=MinecraftClient.getInstance().getWindow();
    public static Window window=null;
    private static final WindowSettings windowSettings
            =new WindowSettings(
            800,600, OptionalInt.empty(),OptionalInt.empty(),false
    );
    public static void openWindow(){
        if(FeatureToggle.THE_THIRD_EYE.getBooleanValue()){
            if(window !=null){
                closeWindow();
            }else{
                WindowProvider windowProvider = new WindowProvider(MinecraftClient.getInstance());
                window = windowProvider.createWindow(windowSettings,MinecraftClient.getInstance().options.fullscreenResolution,"The Third Eye");
            }
        }
    }
    public static void closeWindow(){
        ((IMixinMinecraftClient)MinecraftClient.getInstance()).setWindow(defaultWindow);
        window.close();
        window=null;
    }

}

