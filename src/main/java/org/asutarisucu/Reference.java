package org.asutarisucu;

import org.asutarisucu.Event.LastUseCancel;
import org.asutarisucu.Event.RenderCash;

public class Reference {
    public static final String MOD_ID = "AsutanTweaks";
    public static final String VERSION = "1.1.0";

    public static void LoadEvent(){
        LastUseCancel.UseBlockEvents();
        RenderCash.registerCash();
    }
}
