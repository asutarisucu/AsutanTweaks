package org.asutarisucu;

import org.asutarisucu.Event.LastUseCancel;
import org.asutarisucu.Event.RenderCash;
import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateViewer;
import org.asutarisucu.tweak.BlockUpdateViewer.UpdateSuppressionView;
import org.asutarisucu.tweak.SimpleItemEntityRender.SimpleEntityRender;

public class Reference {
    public static final String MOD_ID = "AsutanTweaks";
    public static final String VERSION = "1.1.0";

    public static void LoadEvent(){
        LastUseCancel.UseBlockEvents();
        RenderCash.registerCash();
        BlockUpdateViewer.register();
        UpdateSuppressionView.register();
        SimpleEntityRender.register();
    }
}
