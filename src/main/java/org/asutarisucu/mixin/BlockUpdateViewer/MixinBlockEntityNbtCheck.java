package org.asutarisucu.mixin.BlockUpdateViewer;

//#if MC < 260100
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
//#if MC < 12111
import net.minecraft.nbt.NbtCompound;
//#else
//$$ import net.minecraft.storage.ReadView;
//#endif
import net.minecraft.util.math.BlockPos;
import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class MixinBlockEntityNbtCheck {

//#if MC >= 12111
//$$ @Inject(method = "read", at = @At("HEAD"))
//$$ private void asutantweaks_checkNbtId(ReadView nbt, CallbackInfo ci) {
//$$     if (!((Object) this instanceof ShulkerBoxBlockEntity)) return;
//$$     BlockPos pos = ((BlockEntity) (Object) this).getPos().toImmutable();
//$$     String id = nbt.getString("id", "");
//$$     if (id.isEmpty()) return;
//$$     if (id.contains("shulker_box")) {
//$$         BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.remove(pos);
//$$     } else {
//$$         BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.add(pos);
//$$     }
//$$ }
//#else
    @Inject(method = "readNbt", at = @At("HEAD"))
    private void asutantweaks_checkNbtId(NbtCompound nbt, CallbackInfo ci) {
        if (!((Object) this instanceof ShulkerBoxBlockEntity)) return;
        BlockPos pos = ((BlockEntity) (Object) this).getPos().toImmutable();
        if (!nbt.contains("id")) return;
        String id = nbt.getString("id");
        if (id.isEmpty()) return;
        if (id.contains("shulker_box")) {
            BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.remove(pos);
        } else {
            BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.add(pos);
        }
    }
//#endif

}
//#else
//$$ import net.minecraft.world.level.block.entity.BlockEntity;
//$$ import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.storage.ValueInput;
//$$ import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateCalculator;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ @Mixin(BlockEntity.class)
//$$ public class MixinBlockEntityNbtCheck {
//$$
//$$     @Inject(method = "loadAdditional", at = @At("HEAD"), require = 0)
//$$     private void asutantweaks_checkNbtId(ValueInput input, CallbackInfo ci) {
//$$         if (!((Object) this instanceof ShulkerBoxBlockEntity)) return;
//$$         BlockPos pos = ((BlockEntity) (Object) this).getBlockPos().immutable();
//$$         String idStr = input.getString("id").orElse("");
//$$         if (idStr.isEmpty()) return;
//$$         if (idStr.contains("shulker_box")) {
//$$             BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.remove(pos);
//$$         } else {
//$$             BlockUpdateCalculator.CORRUPT_ENTITY_POSITIONS.add(pos);
//$$         }
//$$     }
//$$ }
//#endif
