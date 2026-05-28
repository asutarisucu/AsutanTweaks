package org.asutarisucu.mixin.BlockUpdateViewer;

//#if MC < 260100
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.asutarisucu.tweak.BlockUpdateViewer.BlockUpdateCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

//#if MC < 260100
@Mixin(BlockEntity.class)
//#endif
public class MixinBlockEntityNbtCheck {

//#if MC < 260100
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
