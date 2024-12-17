package org.asutarisucu.Configs;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigHotkey;

import java.util.List;

public class Hotkeys {
    //ホットキーのConfig項目
    public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey("openConfigGui","B,C","Open Config GUI Screen");
    public static final ConfigHotkey CLEAR_ITEM_COUNT=new ConfigHotkey("ClearItemCount","","Clear SimpleItemEntityRender Count");
    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            OPEN_CONFIG_GUI,
            CLEAR_ITEM_COUNT
    );
}