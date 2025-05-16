package org.asutarisucu.tweak.SearchItems;

import fi.dy.masa.malilib.util.InfoUtils;
import net.minecraft.registry.Registries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;

import org.asutarisucu.Configs.Configs;

import java.util.List;

public class RegisterItem {
    public static void addHandItem() {
        Item item= MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getMainHandStack().getItem() : null;
        if(item!=null){
            String itemName=Registries.ITEM.getId(item).getPath();
            List<String> list=Configs.Generic.HIGHLIGHT_ITEM_LIST.getStrings();
            if(!itemName.equals("air")&&!list.contains(itemName)){
                list.add(itemName);
                Configs.Generic.HIGHLIGHT_ITEM_LIST.setStrings(list);
                InfoUtils.printActionbarMessage("Add Highlight item:["+itemName+"]");
            }

        }
    }
}
