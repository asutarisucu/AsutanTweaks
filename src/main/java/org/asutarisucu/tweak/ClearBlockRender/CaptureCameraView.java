package org.asutarisucu.tweak.ClearBlockRender;

//#if MC < 260100
import net.minecraft.client.render.Camera;
//#else
//$$ import net.minecraft.client.Camera;
//#endif

/**
 * A {@link Camera} standing where the capture is framed from.
 *
 * Entity renderers read the dispatcher's camera for shadow fade, name tag
 * distance and anything they billboard. Left pointing at the player, an entity
 * recorded from further than 16 blocks away loses its shadow entirely, so the
 * dispatcher is handed this camera for the length of the pass and given its own
 * back afterwards.
 *
 * The position is in world space, because the renderers compare it against
 * entity world positions; the geometry itself is still region-local.
 *
 * Camera's setters are protected, which a subclass reaches on its own — no
 * accessor mixin needed. They also carry each version's own rotation
 * convention, which is why {@code getRotation()} is what fills the orientation
 * the newer pipelines billboard with.
 */
public final class CaptureCameraView extends Camera {

    public void place(double x, double y, double z, float yaw, float pitch) {
//#if MC < 260100
        this.setPos(x, y, z);
//#else
        //$$ this.setPosition(x, y, z);
//#endif
        this.setRotation(yaw, pitch);
    }
}
