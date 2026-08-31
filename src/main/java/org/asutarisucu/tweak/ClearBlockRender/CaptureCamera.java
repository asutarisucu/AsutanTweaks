package org.asutarisucu.tweak.ClearBlockRender;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.lib.config.ProjectionMode;

import org.joml.Matrix4f;

/**
 * Camera framing for a capture: the selection is always fully in frame, so the
 * distance and the orthographic extent are derived from its bounding sphere
 * rather than configured directly. {@code CBR Zoom} then scales that framing.
 *
 * All coordinates are relative to the region's minimum corner, which keeps the
 * float matrices well conditioned no matter where in the world the region is.
 */
public final class CaptureCamera {

    /** Extra room around the bounding sphere so the selection is not flush with the frame edge. */
    private static final float MARGIN = 1.08f;

    public final float camX, camY, camZ;
    public final float yaw, pitch;
    private final float radius;
    private final float distance;

    private CaptureCamera(float camX, float camY, float camZ, float yaw, float pitch,
                          float radius, float distance) {
        this.camX = camX; this.camY = camY; this.camZ = camZ;
        this.yaw = yaw; this.pitch = pitch;
        this.radius = radius;
        this.distance = distance;
    }

    /**
     * @param bounds    inclusive block bounds {minX,minY,minZ,maxX,maxY,maxZ}
     * @param yawOffset extra yaw in degrees contributed by the orbit
     */
    public static CaptureCamera frame(int[] bounds, double yawOffset) {
        float sizeX = bounds[3] - bounds[0] + 1;
        float sizeY = bounds[4] - bounds[1] + 1;
        float sizeZ = bounds[5] - bounds[2] + 1;
        // centre in region-local space
        float cx = sizeX / 2f, cy = sizeY / 2f, cz = sizeZ / 2f;
        float radius = (float) Math.sqrt(cx * cx + cy * cy + cz * cz) * MARGIN
                / (float) Configs.Generic.CBR_ZOOM.getDoubleValue();

        float yaw = (float) (Configs.Generic.CBR_YAW.getDoubleValue() + yawOffset);
        float pitch = (float) Configs.Generic.CBR_PITCH.getDoubleValue();

        float distance;
        if (Configs.Generic.CBR_PROJECTION.getValue() == ProjectionMode.PERSPECTIVE) {
            float halfFov = (float) Math.toRadians(Configs.Generic.CBR_FOV.getDoubleValue()) / 2f;
            distance = radius / (float) Math.tan(halfFov);
        } else {
            // orthographic: distance only has to clear the geometry
            distance = radius * 2f;
        }

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        // view direction, MC convention: yaw 0 looks towards +Z, positive pitch looks down
        float dirX = (float) (-Math.sin(yawRad) * Math.cos(pitchRad));
        float dirY = (float) (-Math.sin(pitchRad));
        float dirZ = (float) (Math.cos(yawRad) * Math.cos(pitchRad));

        // Pan shifts the eye across its own view plane. The rotation is untouched,
        // so the whole picture slides rather than turning.
        float horiz = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
        float rightX = horiz == 0f ? 1f : -dirZ / horiz;
        float rightZ = horiz == 0f ? 0f : dirX / horiz;
        // up = right x forward
        float upX = -rightZ * dirY;
        float upY = rightZ * dirX - rightX * dirZ;
        float upZ = rightX * dirY;
        float upLen = (float) Math.sqrt(upX * upX + upY * upY + upZ * upZ);
        if (upLen > 0f) { upX /= upLen; upY /= upLen; upZ /= upLen; }

        float panX = (float) Configs.Generic.CBR_PAN_X.getDoubleValue();
        float panY = (float) Configs.Generic.CBR_PAN_Y.getDoubleValue();
        float eyeX = cx - dirX * distance + rightX * panX + upX * panY;
        float eyeY = cy - dirY * distance + upY * panY;   // right has no Y component
        float eyeZ = cz - dirZ * distance + rightZ * panX + upZ * panY;

        return new CaptureCamera(eyeX, eyeY, eyeZ, yaw, pitch, radius, distance);
    }

    /** Half-height of the framed area in blocks; used to scale drag distances. */
    public float radius() {
        return radius;
    }

    /** View matrix in region-local space (rotation then translation, as the world render builds it). */
    public Matrix4f viewMatrix() {
        return new Matrix4f()
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(yaw + 180.0f))
                .translate(-camX, -camY, -camZ);
    }

    public Matrix4f projectionMatrix(int width, int height) {
        float aspect = (float) width / (float) height;
        float near = 0.05f;
        float far = distance + radius * 2f;
        boolean perspective = Configs.Generic.CBR_PROJECTION.getValue() == ProjectionMode.PERSPECTIVE;
        float halfH = radius;
        float halfW = radius * aspect;
//#if MC < 260200
        if (perspective) {
            return new Matrix4f().perspective(
                    (float) Math.toRadians(Configs.Generic.CBR_FOV.getDoubleValue()), aspect, near, far);
        }
        return new Matrix4f().ortho(-halfW, halfW, -halfH, halfH, near, far);
//#else
        //$$ // MC 26.2 uses a reversed depth range: Projection.getMatrix swaps near and
        //$$ // far, and the clip-space Z convention comes from the device.
        //$$ boolean zZeroToOne = com.mojang.blaze3d.systems.RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
        //$$ if (perspective) {
        //$$     return new Matrix4f().setPerspective(
        //$$             (float) Math.toRadians(Configs.Generic.CBR_FOV.getDoubleValue()), aspect, far, near, zZeroToOne);
        //$$ }
        //$$ return new Matrix4f().setOrtho(-halfW, halfW, -halfH, halfH, far, near, zZeroToOne);
//#endif
    }
}
