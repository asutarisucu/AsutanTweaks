package org.asutarisucu.Utiles.Block;

import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BlockState {
    /** Names accepted in the restriction whitelist, upper case, in the order the config screen suggests them. */
    private static final Map<String, Property<?>> PROPERTIES = new LinkedHashMap<>();

    static {
        PROPERTIES.put("AGE_1", Properties.AGE_1);
        PROPERTIES.put("AGE_15", Properties.AGE_15);
        PROPERTIES.put("AGE_2", Properties.AGE_2);
        PROPERTIES.put("AGE_25", Properties.AGE_25);
        PROPERTIES.put("AGE_3", Properties.AGE_3);
        PROPERTIES.put("AGE_4", Properties.AGE_4);
        PROPERTIES.put("AGE_5", Properties.AGE_5);
        PROPERTIES.put("AGE_7", Properties.AGE_7);
        PROPERTIES.put("ATTACHMENT", Properties.ATTACHMENT);
        PROPERTIES.put("ATTACHED", Properties.ATTACHED);
        PROPERTIES.put("AXIS", Properties.AXIS);
        PROPERTIES.put("BAMBOO_LEAVES", Properties.BAMBOO_LEAVES);
        PROPERTIES.put("BED_PART", Properties.BED_PART);
        PROPERTIES.put("BERRIES", Properties.BERRIES);
        PROPERTIES.put("BITES", Properties.BITES);
        PROPERTIES.put("BLOOM", Properties.BLOOM);
        PROPERTIES.put("BLOCK_HALF", Properties.BLOCK_HALF);
        PROPERTIES.put("BOTTOM", Properties.BOTTOM);
        PROPERTIES.put("CANDLES", Properties.CANDLES);
        PROPERTIES.put("CAN_SUMMON", Properties.CAN_SUMMON);
        PROPERTIES.put("CHARGES", Properties.CHARGES);
        PROPERTIES.put("CHEST_TYPE", Properties.CHEST_TYPE);
        PROPERTIES.put("COMPARATOR_MODE", Properties.COMPARATOR_MODE);
        PROPERTIES.put("CONDITIONAL", Properties.CONDITIONAL);
        PROPERTIES.put("DELAY", Properties.DELAY);
        PROPERTIES.put("DISARMED", Properties.DISARMED);
        PROPERTIES.put("DISTANCE_0_7", Properties.DISTANCE_0_7);
        PROPERTIES.put("DISTANCE_1_7", Properties.DISTANCE_1_7);
        PROPERTIES.put("DOOR_HINGE", Properties.DOOR_HINGE);
        PROPERTIES.put("DOUBLE_BLOCK_HALF", Properties.DOUBLE_BLOCK_HALF);
        PROPERTIES.put("DOWN", Properties.DOWN);
        PROPERTIES.put("DRAG", Properties.DRAG);
        PROPERTIES.put("EAST", Properties.EAST);
        PROPERTIES.put("EAST_WALL_SHAPE", Properties.EAST_WALL_SHAPE);
        PROPERTIES.put("EAST_WIRE_CONNECTION", Properties.EAST_WIRE_CONNECTION);
        PROPERTIES.put("EGGS", Properties.EGGS);
        PROPERTIES.put("ENABLED", Properties.ENABLED);
        PROPERTIES.put("EXTENDED", Properties.EXTENDED);
        PROPERTIES.put("EYE", Properties.EYE);
        PROPERTIES.put("FACING", Properties.FACING);
        PROPERTIES.put("FALLING", Properties.FALLING);
        PROPERTIES.put("HANGING", Properties.HANGING);
        PROPERTIES.put("HAS_BOOK", Properties.HAS_BOOK);
        PROPERTIES.put("HAS_BOTTLE_0", Properties.HAS_BOTTLE_0);
        PROPERTIES.put("HAS_BOTTLE_1", Properties.HAS_BOTTLE_1);
        PROPERTIES.put("HAS_BOTTLE_2", Properties.HAS_BOTTLE_2);
        PROPERTIES.put("HAS_RECORD", Properties.HAS_RECORD);
        PROPERTIES.put("HATCH", Properties.HATCH);
        PROPERTIES.put("HONEY_LEVEL", Properties.HONEY_LEVEL);
        PROPERTIES.put("FACE", Properties.HOPPER_FACING);
        PROPERTIES.put("HORIZONTAL_AXIS", Properties.HORIZONTAL_AXIS);
        PROPERTIES.put("HORIZONTAL_FACING", Properties.HORIZONTAL_FACING);
        PROPERTIES.put("INVERTED", Properties.INVERTED);
        PROPERTIES.put("INSTRUMENT", Properties.INSTRUMENT);
        PROPERTIES.put("IN_WALL", Properties.IN_WALL);
        PROPERTIES.put("LAYERS", Properties.LAYERS);
        PROPERTIES.put("LEVEL_1_8", Properties.LEVEL_1_8);
        PROPERTIES.put("LEVEL_15", Properties.LEVEL_15);
        PROPERTIES.put("LEVEL_3", Properties.LEVEL_3);
        PROPERTIES.put("LEVEL_8", Properties.LEVEL_8);
        PROPERTIES.put("LIT", Properties.LIT);
        PROPERTIES.put("LOCKED", Properties.LOCKED);
        PROPERTIES.put("MOISTURE", Properties.MOISTURE);
        PROPERTIES.put("NORTH", Properties.NORTH);
        PROPERTIES.put("NORTH_WALL_SHAPE", Properties.NORTH_WALL_SHAPE);
        PROPERTIES.put("NORTH_WIRE_CONNECTION", Properties.NORTH_WIRE_CONNECTION);
        PROPERTIES.put("NOTE", Properties.NOTE);
        PROPERTIES.put("OCCUPIED", Properties.OCCUPIED);
        PROPERTIES.put("OPEN", Properties.OPEN);
        PROPERTIES.put("ORIENTATION", Properties.ORIENTATION);
        PROPERTIES.put("PERSISTENT", Properties.PERSISTENT);
        PROPERTIES.put("PICKLES", Properties.PICKLES);
        PROPERTIES.put("PISTON_TYPE", Properties.PISTON_TYPE);
        PROPERTIES.put("POWER", Properties.POWER);
        PROPERTIES.put("POWERED", Properties.POWERED);
        PROPERTIES.put("RAIL_SHAPE", Properties.RAIL_SHAPE);
        PROPERTIES.put("ROTATION", Properties.ROTATION);
        PROPERTIES.put("SCULK_SENSOR_PHASE", Properties.SCULK_SENSOR_PHASE);
        PROPERTIES.put("SHORT", Properties.SHORT);
        PROPERTIES.put("SHRIEKING", Properties.SHRIEKING);
        PROPERTIES.put("SIGNAL_FIRE", Properties.SIGNAL_FIRE);
//#if MC >= 260100
//$$         PROPERTIES.put("SLOT_0_OCCUPIED", net.minecraft.world.level.block.state.properties.BlockStateProperties.SLOT_0_OCCUPIED);
//$$         PROPERTIES.put("SLOT_1_OCCUPIED", net.minecraft.world.level.block.state.properties.BlockStateProperties.SLOT_1_OCCUPIED);
//$$         PROPERTIES.put("SLOT_2_OCCUPIED", net.minecraft.world.level.block.state.properties.BlockStateProperties.SLOT_2_OCCUPIED);
//$$         PROPERTIES.put("SLOT_3_OCCUPIED", net.minecraft.world.level.block.state.properties.BlockStateProperties.SLOT_3_OCCUPIED);
//$$         PROPERTIES.put("SLOT_4_OCCUPIED", net.minecraft.world.level.block.state.properties.BlockStateProperties.SLOT_4_OCCUPIED);
//$$         PROPERTIES.put("SLOT_5_OCCUPIED", net.minecraft.world.level.block.state.properties.BlockStateProperties.SLOT_5_OCCUPIED);
//#else
        PROPERTIES.put("SLOT_0_OCCUPIED", Properties.SLOT_0_OCCUPIED);
        PROPERTIES.put("SLOT_1_OCCUPIED", Properties.SLOT_1_OCCUPIED);
        PROPERTIES.put("SLOT_2_OCCUPIED", Properties.SLOT_2_OCCUPIED);
        PROPERTIES.put("SLOT_3_OCCUPIED", Properties.SLOT_3_OCCUPIED);
        PROPERTIES.put("SLOT_4_OCCUPIED", Properties.SLOT_4_OCCUPIED);
        PROPERTIES.put("SLOT_5_OCCUPIED", Properties.SLOT_5_OCCUPIED);
//#endif
        PROPERTIES.put("SNOWY", Properties.SNOWY);
        PROPERTIES.put("SOUTH", Properties.SOUTH);
        PROPERTIES.put("SOUTH_WALL_SHAPE", Properties.SOUTH_WALL_SHAPE);
        PROPERTIES.put("SOUTH_WIRE_CONNECTION", Properties.SOUTH_WIRE_CONNECTION);
        PROPERTIES.put("STAGE", Properties.STAGE);
        PROPERTIES.put("STAIR_SHAPE", Properties.STAIR_SHAPE);
        PROPERTIES.put("STRUCTURE_BLOCK_MODE", Properties.STRUCTURE_BLOCK_MODE);
        PROPERTIES.put("TILT", Properties.TILT);
        PROPERTIES.put("TRIGGERED", Properties.TRIGGERED);
        PROPERTIES.put("UNSTABLE", Properties.UNSTABLE);
        PROPERTIES.put("UP", Properties.UP);
        PROPERTIES.put("VERTICAL_DIRECTION", Properties.VERTICAL_DIRECTION);
        PROPERTIES.put("WALL_MOUNT_LOCATION", Properties.WALL_MOUNT_LOCATION);
        PROPERTIES.put("WATERLOGGED", Properties.WATERLOGGED);
        PROPERTIES.put("WEST", Properties.WEST);
        PROPERTIES.put("WEST_WALL_SHAPE", Properties.WEST_WALL_SHAPE);
        PROPERTIES.put("WEST_WIRE_CONNECTION", Properties.WEST_WIRE_CONNECTION);
    }

    public static Property<?> getProperty(String name){
        return PROPERTIES.get(name.toUpperCase(Locale.ROOT));
    }

    public static Set<String> names() {
        return Collections.unmodifiableSet(PROPERTIES.keySet());
    }
}
