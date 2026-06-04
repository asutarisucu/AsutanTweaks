package org.asutarisucu.tweak.SearchItems;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.GUI.HudLogger;
import org.asutarisucu.lib.util.MessageUtils;

//#if MC < 260100
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.world.item.Item;
//$$ import net.minecraft.core.registries.BuiltInRegistries;
//#endif

import java.util.List;

public class RegisterItem {

    public static void addHandItem() {
//#if MC < 260100
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        Item item = mc.player.getMainHandStack().getItem();
        String itemName = Registries.ITEM.getId(item).getPath();
//#else
//$$ Minecraft mc = Minecraft.getInstance();
//$$ if (mc.player == null) return;
//$$ Item item = mc.player.getMainHandItem().getItem();
//$$ String itemName = BuiltInRegistries.ITEM.getKey(item).getPath();
//#endif
        if (!itemName.equals("air")) {
            List<String> list = Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings();
            if (!list.contains(itemName)) {
                list.add(itemName);
                Configs.Generic.HIGHLIGHT_ITEM_LIST.setValue(list);
                HudLogger.INSTANCE.log("Added to highlight list: [" + itemName + "]");
            }
        }
    }
}
