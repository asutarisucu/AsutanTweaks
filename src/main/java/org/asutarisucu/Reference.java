package org.asutarisucu;

import org.asutarisucu.Event.LastUseCancel;

public class Reference {
    public static final String MOD_ID = "AsutanTweaks";
    public static final String VERSION = "1.0.1";

    public static void LoadEvent(){
        LastUseCancel.UseBlockEvents();

    }
}
