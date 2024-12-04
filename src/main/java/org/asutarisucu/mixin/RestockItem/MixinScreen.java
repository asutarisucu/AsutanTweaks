package org.asutarisucu.mixin.RestockItem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HandledScreen.class)
public interface MixinScreen {
    @Invoker("onMouseClick")
    void throw_items$onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

    @Accessor("focusedSlot")
    Slot throw_items$focusedSlot();

}
