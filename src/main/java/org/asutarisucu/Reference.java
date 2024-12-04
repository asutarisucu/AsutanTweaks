package org.asutarisucu;

import org.asutarisucu.Event.LastUseCancel;

public class Reference {
    public static final String MOD_ID = "AsutanTweaks";
    public static final String VERSION = "1.0.0";
    public static class ModIds{
        public static String itemscroller="itemscroller";
    }
    public static void LoadEvent(){
        LastUseCancel.UseBlockEvents();

    }
}
