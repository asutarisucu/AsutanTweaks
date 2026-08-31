package org.asutarisucu.tweak.PickBlock;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.Feature;
import org.asutarisucu.Configs.Hotkeys;

//#if MC < 12111
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
//#if MC < 12005
import net.minecraft.nbt.NbtCompound;
//#else
//$$ import net.minecraft.component.DataComponentTypes;
//$$ import net.minecraft.component.type.BlockStateComponent;
//#endif
//#elseif MC < 260100
//$$ import net.minecraft.block.BlockState;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.network.ClientPlayerEntity;
//$$ import net.minecraft.client.world.ClientWorld;
//$$ import net.minecraft.component.DataComponentTypes;
//$$ import net.minecraft.component.type.BlockStateComponent;
//$$ import net.minecraft.entity.player.PlayerInventory;
//$$ import net.minecraft.item.ItemStack;
//$$ import net.minecraft.screen.slot.SlotActionType;
//$$ import net.minecraft.state.property.Property;
//$$ import net.minecraft.util.Hand;
//$$ import net.minecraft.util.hit.BlockHitResult;
//$$ import net.minecraft.util.hit.HitResult;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import net.minecraft.util.math.Vec3d;
//$$ import net.minecraft.world.RaycastContext;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.multiplayer.ClientLevel;
//$$ import net.minecraft.client.player.LocalPlayer;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.core.component.DataComponents;
//$$ import net.minecraft.world.InteractionHand;
//$$ import net.minecraft.world.entity.player.Inventory;
//$$ import net.minecraft.world.inventory.ContainerInput;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import net.minecraft.world.item.component.BlockItemStateProperties;
//$$ import net.minecraft.world.level.ClipContext;
//$$ import net.minecraft.world.level.block.state.BlockState;
//$$ import net.minecraft.world.level.block.state.properties.Property;
//$$ import net.minecraft.world.phys.BlockHitResult;
//$$ import net.minecraft.world.phys.HitResult;
//$$ import net.minecraft.world.phys.Vec3;
//#endif

/**
 * Middle-click pick block without the vanilla reach limit.
 *
 * The pick is redone client-side rather than letting vanilla run with a
 * stretched hit result: from MC 1.21.4 on vanilla routes pick block through
 * {@code ServerboundPickItemFromBlockPacket}, and the server drops any packet
 * failing its interaction-range check, so an out-of-reach pick can only work if
 * the client does the work itself.
 *
 * Holding {@link Hotkeys#PICK_BLOCK_ULTIMATE_COMPONENT} additionally copies the
 * block's state properties — a composter's fill level, a repeater's delay, the
 * half a slab sits in — onto the stack, so placing it reproduces that state.
 * Vanilla has no equivalent; its ctrl+pick copies block entity NBT instead.
 * Those two are separate, and a plain ctrl+pick within reach is handed back to
 * vanilla so its NBT copy keeps working.
 */
public final class PickBlockUltimate {

    private PickBlockUltimate() {}

//#if MC < 12111
    /** @return true when the pick was handled here and vanilla should be skipped. */
    public static boolean pick() {
        if (!Feature.PICK_BLOCK_ULTIMATE.isEnabled()) return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        if (player == null || world == null || mc.interactionManager == null) return false;
        // An entity under the crosshair is vanilla's business — this only extends block picking.
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) return false;

        BlockHitResult hit = raycast(player, world);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = hit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return false;

        boolean withState = isComponentKeyHeld(mc);
        // Plain ctrl+pick on a block vanilla can reach: let vanilla do it, so its
        // block entity NBT copy is not lost to this feature.
        if (!withState && Screen.hasControlDown() && vanillaCanPick(mc, pos)) return false;

        ItemStack stack = state.getBlock().getPickStack(world, pos, state);
        if (stack.isEmpty()) return false;

        if (withState && player.getAbilities().creativeMode) applyBlockState(stack, state);

        give(mc, player, stack, player.getAbilities().creativeMode);
        return true;
    }

    private static boolean vanillaCanPick(MinecraftClient mc, BlockPos pos) {
        return mc.crosshairTarget instanceof BlockHitResult bhr
                && bhr.getType() == HitResult.Type.BLOCK
                && bhr.getBlockPos().equals(pos);
    }

    private static BlockHitResult raycast(ClientPlayerEntity player, ClientWorld world) {
        double reach = Configs.Generic.PICK_BLOCK_REACH.getIntegerValue();
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d end = start.add(player.getRotationVec(1.0f).multiply(reach));
        return world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
    }

//#if MC < 12005
    private static void applyBlockState(ItemStack stack, BlockState state) {
        if (state.getProperties().isEmpty()) return;
        NbtCompound tag = new NbtCompound();
        for (Property<?> property : state.getProperties()) {
            tag.putString(property.getName(), nameOf(state, property));
        }
        stack.getOrCreateNbt().put("BlockStateTag", tag);
    }

    private static <T extends Comparable<T>> String nameOf(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }
//#else
    //$$ private static void applyBlockState(ItemStack stack, BlockState state) {
    //$$     if (state.getProperties().isEmpty()) return;
    //$$     BlockStateComponent component = BlockStateComponent.DEFAULT;
    //$$     for (Property<?> property : state.getProperties()) {
    //$$         component = with(component, property, state);
    //$$     }
    //$$     stack.set(DataComponentTypes.BLOCK_STATE, component);
    //$$ }
    //$$
    //$$ private static <T extends Comparable<T>> BlockStateComponent with(
    //$$         BlockStateComponent component, Property<T> property, BlockState state) {
    //$$     return component.with(property, state);
    //$$ }
//#endif

    private static void give(MinecraftClient mc, ClientPlayerEntity player, ItemStack stack, boolean creative) {
        PlayerInventory inv = player.getInventory();
        if (creative) {
            inv.addPickBlock(stack);
            mc.interactionManager.clickCreativeStack(
                    player.getStackInHand(Hand.MAIN_HAND), 36 + inv.selectedSlot);
            return;
        }
        int slot = inv.getSlotWithStack(stack);
        if (slot == -1) return;
        if (PlayerInventory.isValidHotbarIndex(slot)) inv.selectedSlot = slot;
        else mc.interactionManager.pickFromInventory(slot);
    }

    private static boolean isComponentKeyHeld(MinecraftClient mc) {
        return Hotkeys.PICK_BLOCK_ULTIMATE_COMPONENT.getHotkey().isHeld(mc.getWindow().getHandle());
    }
//#elseif MC < 260100
//$$ /** @return true when the pick was handled here and vanilla should be skipped. */
//$$ public static boolean pick() {
//$$     if (!Feature.PICK_BLOCK_ULTIMATE.isEnabled()) return false;
//$$
//$$     MinecraftClient mc = MinecraftClient.getInstance();
//$$     ClientPlayerEntity player = mc.player;
//$$     ClientWorld world = mc.world;
//$$     if (player == null || world == null || mc.interactionManager == null) return false;
//$$     // An entity under the crosshair is vanilla's business — this only extends block picking.
//$$     if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) return false;
//$$
//$$     BlockHitResult hit = raycast(player, world);
//$$     if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;
//$$
//$$     BlockPos pos = hit.getBlockPos();
//$$     BlockState state = world.getBlockState(pos);
//$$     if (state.isAir()) return false;
//$$
//$$     boolean withState = isComponentKeyHeld(mc);
//$$     // Plain ctrl+pick on a block vanilla can reach: let vanilla do it, so the
//$$     // server-side block entity NBT copy is not lost to this feature.
//$$     if (!withState && mc.isCtrlPressed() && vanillaCanPick(mc, pos)) return false;
//$$
//$$     ItemStack stack = state.getPickStack(world, pos, false);
//$$     if (stack.isEmpty()) return false;
//$$
//$$     boolean creative = player.getAbilities().creativeMode;
//$$     if (withState && creative) applyBlockState(stack, state);
//$$
//$$     give(mc, player, stack, creative);
//$$     return true;
//$$ }
//$$
//$$ private static boolean vanillaCanPick(MinecraftClient mc, BlockPos pos) {
//$$     return mc.crosshairTarget instanceof BlockHitResult bhr
//$$             && bhr.getType() == HitResult.Type.BLOCK
//$$             && bhr.getBlockPos().equals(pos);
//$$ }
//$$
//$$ private static BlockHitResult raycast(ClientPlayerEntity player, ClientWorld world) {
//$$     double reach = Configs.Generic.PICK_BLOCK_REACH.getIntegerValue();
//$$     Vec3d start = player.getCameraPosVec(1.0f);
//$$     Vec3d end = start.add(player.getRotationVec(1.0f).multiply(reach));
//$$     return world.raycast(new RaycastContext(start, end,
//$$             RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
//$$ }
//$$
//$$ private static void applyBlockState(ItemStack stack, BlockState state) {
//$$     if (state.getProperties().isEmpty()) return;
//$$     BlockStateComponent component = BlockStateComponent.DEFAULT;
//$$     for (Property<?> property : state.getProperties()) {
//$$         component = with(component, property, state);
//$$     }
//$$     stack.set(DataComponentTypes.BLOCK_STATE, component);
//$$ }
//$$
//$$ private static <T extends Comparable<T>> BlockStateComponent with(
//$$         BlockStateComponent component, Property<T> property, BlockState state) {
//$$     return component.with(property, state);
//$$ }
//$$
//$$ private static void give(MinecraftClient mc, ClientPlayerEntity player, ItemStack stack, boolean creative) {
//$$     PlayerInventory inv = player.getInventory();
//$$     if (creative) {
//$$         // Mirrors the pre-1.21.4 addPickBlock: reuse the hotbar slot that already
//$$         // holds this stack instead of burning another one.
//$$         int held = inv.getSlotWithStack(stack);
//$$         if (PlayerInventory.isValidHotbarIndex(held)) {
//$$             inv.setSelectedSlot(held);
//$$             return;
//$$         }
//$$         inv.swapStackWithHotbar(stack);
//$$         mc.interactionManager.clickCreativeStack(
//$$                 player.getStackInHand(Hand.MAIN_HAND), 36 + inv.getSelectedSlot());
//$$         return;
//$$     }
//$$     int slot = inv.getSlotWithStack(stack);
//$$     if (slot == -1) return;
//$$     if (PlayerInventory.isValidHotbarIndex(slot)) {
//$$         inv.setSelectedSlot(slot);
//$$     } else {
//$$         // 1.21.4+ dropped the client-side pick-from-inventory packet; a real
//$$         // hotbar-swap click is server-validated and reaches the same result.
//$$         mc.interactionManager.clickSlot(player.playerScreenHandler.syncId, slot,
//$$                 inv.getSelectedSlot(), SlotActionType.SWAP, player);
//$$     }
//$$ }
//$$
//$$ private static boolean isComponentKeyHeld(MinecraftClient mc) {
//$$     return Hotkeys.PICK_BLOCK_ULTIMATE_COMPONENT.getHotkey().isHeld(mc.getWindow().getHandle());
//$$ }
//#else
//$$ /** @return true when the pick was handled here and vanilla should be skipped. */
//$$ public static boolean pick() {
//$$     if (!Feature.PICK_BLOCK_ULTIMATE.isEnabled()) return false;
//$$
//$$     Minecraft mc = Minecraft.getInstance();
//$$     LocalPlayer player = mc.player;
//$$     ClientLevel level = mc.level;
//$$     if (player == null || level == null || mc.gameMode == null) return false;
//$$     // An entity under the crosshair is vanilla's business — this only extends block picking.
//$$     if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) return false;
//$$
//$$     BlockHitResult hit = raycast(player, level);
//$$     if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;
//$$
//$$     BlockPos pos = hit.getBlockPos();
//$$     BlockState state = level.getBlockState(pos);
//$$     if (state.isAir()) return false;
//$$
//$$     boolean withState = isComponentKeyHeld(mc);
//$$     // Plain ctrl+pick on a block vanilla can reach: let vanilla do it, so the
//$$     // server-side block entity NBT copy is not lost to this feature.
//$$     if (!withState && mc.hasControlDown() && vanillaCanPick(mc, pos)) return false;
//$$
//$$     ItemStack stack = state.getCloneItemStack(level, pos, false);
//$$     if (stack.isEmpty()) return false;
//$$
//$$     boolean creative = player.getAbilities().instabuild;
//$$     if (withState && creative) applyBlockState(stack, state);
//$$
//$$     give(mc, player, stack, creative);
//$$     return true;
//$$ }
//$$
//$$ private static boolean vanillaCanPick(Minecraft mc, BlockPos pos) {
//$$     return mc.hitResult instanceof BlockHitResult bhr
//$$             && bhr.getType() == HitResult.Type.BLOCK
//$$             && bhr.getBlockPos().equals(pos);
//$$ }
//$$
//$$ private static BlockHitResult raycast(LocalPlayer player, ClientLevel level) {
//$$     double reach = Configs.Generic.PICK_BLOCK_REACH.getIntegerValue();
//$$     Vec3 start = player.getEyePosition(1.0f);
//$$     Vec3 end = start.add(player.getViewVector(1.0f).scale(reach));
//$$     return level.clip(new ClipContext(start, end,
//$$             ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
//$$ }
//$$
//$$ private static void applyBlockState(ItemStack stack, BlockState state) {
//$$     if (state.getProperties().isEmpty()) return;
//$$     BlockItemStateProperties properties = BlockItemStateProperties.EMPTY;
//$$     for (Property<?> property : state.getProperties()) {
//$$         properties = with(properties, property, state);
//$$     }
//$$     stack.set(DataComponents.BLOCK_STATE, properties);
//$$ }
//$$
//$$ private static <T extends Comparable<T>> BlockItemStateProperties with(
//$$         BlockItemStateProperties properties, Property<T> property, BlockState state) {
//$$     return properties.with(property, state);
//$$ }
//$$
//$$ private static void give(Minecraft mc, LocalPlayer player, ItemStack stack, boolean creative) {
//$$     Inventory inv = player.getInventory();
//$$     if (creative) {
//$$         // Mirrors the pre-1.21.4 addPickBlock: reuse the hotbar slot that already
//$$         // holds this stack instead of burning another one.
//$$         int held = inv.findSlotMatchingItem(stack);
//$$         if (Inventory.isHotbarSlot(held)) {
//$$             inv.setSelectedSlot(held);
//$$             return;
//$$         }
//$$         inv.addAndPickItem(stack);
//$$         mc.gameMode.handleCreativeModeItemAdd(
//$$                 player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inv.getSelectedSlot());
//$$         return;
//$$     }
//$$     int slot = inv.findSlotMatchingItem(stack);
//$$     if (slot == -1) return;
//$$     if (Inventory.isHotbarSlot(slot)) {
//$$         inv.setSelectedSlot(slot);
//$$     } else {
//$$         // 1.21.4+ dropped the client-side pick-from-inventory packet; a real
//$$         // hotbar-swap click is server-validated and reaches the same result.
//$$         mc.gameMode.handleContainerInput(player.inventoryMenu.containerId, slot,
//$$                 inv.getSelectedSlot(), ContainerInput.SWAP, player);
//$$     }
//$$ }
//$$
//$$ private static boolean isComponentKeyHeld(Minecraft mc) {
//$$     return Hotkeys.PICK_BLOCK_ULTIMATE_COMPONENT.getHotkey().isHeld(mc.getWindow().handle());
//$$ }
//#endif
}
