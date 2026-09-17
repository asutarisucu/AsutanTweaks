package org.asutarisucu.mixin.ConfigScreen;

//#if MC < 12005
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Before MC 1.20.5 each title screen turns a panorama of its own. The config
 * screen draws that one, so the view does not jump when it opens over the title.
 */
@Mixin(TitleScreen.class)
public interface MixinTitleScreenPanorama {

    @Accessor("backgroundRenderer")
    RotatingCubeMapRenderer asutantweaks$getBackgroundRenderer();
}
//#else
//$$ /**
//$$  * Only needed before MC 1.20.5; later versions share one panorama between all
//$$  * screens. Left out of the mixin config there.
//$$  */
//$$ public final class MixinTitleScreenPanorama {
//$$     private MixinTitleScreenPanorama() {}
//$$ }
//#endif
