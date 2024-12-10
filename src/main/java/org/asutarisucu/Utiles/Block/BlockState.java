package org.asutarisucu.Utiles.Block;

import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

public class BlockState {
    public static Property<?> getProperty(String name){
        Property<?> property=null;
        switch (name){
            case "ATTACHED","attached"-> property=Properties.ATTACHED;
            case "BERRIES","berries"->property=Properties.BERRIES;
            case "BLOOM","bloom"->property=Properties.BLOOM;
            case "BOTTOM","bottom"->property=Properties.BOTTOM;

            case "UP","up"->property=Properties.UP;
            case "DOWN","down"->property=Properties.DOWN;
            case "NORTH","north"->property=Properties.NORTH;
            case "SOUTH","south"->property=Properties.SOUTH;
            case "EAST","east"->property=Properties.EAST;
            case "WEST","west"->property=Properties.WEST;
        }
        return property;
    }
}
