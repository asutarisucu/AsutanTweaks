package org.asutarisucu.mixin.ThirdEye;

import org.asutarisucu.Configs.Feature;
import org.asutarisucu.tweak.ThirdEye.ThirdEye;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeCamera;
import org.asutarisucu.tweak.ThirdEye.ThirdEyeWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 12111
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vec3d;
//#if MC < 12006
import net.minecraft.client.util.math.MatrixStack;
//#elseif MC >= 12101
//$$ import net.minecraft.client.render.RenderTickCounter;
//#endif

@Mixin(GameRenderer.class)
public abstract class MixinGameRendererThirdEye {

    @Shadow private MinecraftClient client;
    @Shadow private Camera camera;

//#if MC < 12006
    @Shadow
    private void renderWorld(float tickDelta, long startTime, MatrixStack matrices) {}
//#elseif MC < 12101
//$$     @Shadow
//$$     private void renderWorld(float tickDelta, long startTime) {}
//#else
//$$     @Shadow
//$$     private void renderWorld(RenderTickCounter counter) {}
//#endif

    // Hooked at RETURN — after the main world render is fully complete.
    // Doing the ThirdEye pass here (instead of HEAD) guarantees that whatever
    // state the recursive call leaves behind cannot affect the main view of
    // the current frame, which has already finished rendering.
    //
    // Additionally, we explicitly save/restore the shared Camera's pos and
    // rotation around the recursive call so any code that reads camera state
    // AFTER renderWorld() returns (HUD, post-FX, next-frame setup, etc.) sees
    // the player's values rather than the ThirdEye values written by the
    // RETURN inject in MixinCameraThirdEye.
//#if MC < 12006
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void onRenderWorldReturn(float tickDelta, long startTime, MatrixStack matrices, CallbackInfo ci) {
//#elseif MC < 12101
//$$     @Inject(method = "renderWorld", at = @At("RETURN"))
//$$     private void onRenderWorldReturn(float tickDelta, long startTime, CallbackInfo ci) {
//#else
//$$     @Inject(method = "renderWorld", at = @At("RETURN"))
//$$     private void onRenderWorldReturn(RenderTickCounter counter, CallbackInfo ci) {
//#endif
        if (!Feature.THIRD_EYE.isEnabled())   return;
        if (ThirdEye.isRenderingThirdEye)      return;   // prevent recursion via RETURN
        if (!ThirdEyeWindow.isOpen())          return;
        if (ThirdEye.thirdEyeFbo == null)      return;
        if (!ThirdEyeCamera.initialized)       return;
        if (client.world == null)              return;

        // Snapshot the shared Camera state BEFORE we modify it for the
        // ThirdEye pass. The main world render is already complete; this
        // snapshot is what we restore afterwards so the player's camera
        // state is exactly what any post-renderWorld code expects.
        Vec3d savedPos = camera.getPos();
        float savedYaw = camera.getYaw();
        float savedPitch = camera.getPitch();

        // Clear ThirdEye FBO. getFramebuffer() is intercepted by
        // MixinMinecraftClientThirdEye to return thirdEyeFbo while
        // isRenderingThirdEye is true.
        ThirdEye.thirdEyeFbo.clear(MinecraftClient.IS_SYSTEM_MAC);

        ThirdEye.isRenderingThirdEye = true;

        try {
            // Recursive shadow call. MixinCameraThirdEye's RETURN inject on
            // Camera.update() overrides position/rotation to ThirdEye values
            // while isRenderingThirdEye is true. The HEAD/RETURN guards above
            // prevent infinite recursion.
//#if MC < 12006
            renderWorld(tickDelta, startTime, matrices);
//#elseif MC < 12101
//$$             renderWorld(tickDelta, startTime);
//#else
//$$             renderWorld(counter);
//#endif
        } finally {
            ThirdEye.isRenderingThirdEye = false;
            // Restore the original camera state directly via the protected
            // setters (exposed by MixinCameraInvokerThirdEye). Going through
            // Camera.update() instead would re-derive pos/rotation from the
            // focused entity and apply third-person clipping, which is
            // exactly the "horizontal-north offset" we are trying to avoid.
            MixinCameraInvokerThirdEye inv = (MixinCameraInvokerThirdEye)(Object) camera;
            inv.thirdeye$setPos(savedPos.x, savedPos.y, savedPos.z);
            inv.thirdeye$setRotation(savedYaw, savedPitch);
        }

        // Rebind the main framebuffer at the GL level so any subsequent
        // rendering (HUD, screen overlays) writes to the screen, not the
        // ThirdEye FBO.
        client.getFramebuffer().beginWrite(false);

        // Blit ThirdEye FBO result to the secondary OS window.
        ThirdEyeWindow.blit(
                ThirdEye.thirdEyeFbo.getColorTexId(),
                ThirdEye.thirdEyeFbo.textureWidth,
                ThirdEye.thirdEyeFbo.textureHeight,
                client.getWindow().getHandle()
        );
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void onRenderHandHead(CallbackInfo ci) {
        if (ThirdEye.isRenderingThirdEye) ci.cancel();
    }
}
//#elseif MC < 260100
//$$ import com.mojang.blaze3d.systems.CommandEncoder;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import net.minecraft.client.MinecraftClient;
//$$ import net.minecraft.client.gl.GlobalSettings;
//$$ import net.minecraft.client.option.TextureFilteringMode;
//$$ import net.minecraft.client.render.Camera;
//$$ import net.minecraft.client.render.GameRenderer;
//$$ import net.minecraft.client.render.RenderTickCounter;
//$$ import net.minecraft.util.math.Vec3d;
//$$
//$$ /**
//$$  * MC 1.21.11 ThirdEye render hook.
//$$  *
//$$  * renderWorld() reads the live Camera (via updateCameraState) and performs its
//$$  * own chunk culling, so a recursive renderWorld() with the Camera overridden to
//$$  * the ThirdEye position produces a correct second view. Unlike 1.21.1, the new
//$$  * render API never calls Camera.update() inside renderWorld(), so the position
//$$  * override is applied here directly through the protected setters (exposed by
//$$  * MixinCameraInvokerThirdEye) rather than via a Camera.update() RETURN inject.
//$$  *
//$$  * The camera-relative POSITION used by the terrain shader lives in the
//$$  * GlobalSettings UBO, which GameRenderer.render() writes once per frame (with the
//$$  * player camera) BEFORE renderWorld(). The recursive renderWorld() does not touch
//$$  * it, so without re-writing it the terrain would render relative to the player
//$$  * while entities (positioned CPU-side) follow ThirdEye. We re-write GlobalSettings
//$$  * with the overridden camera for the pass and restore it afterwards.
//$$  *
//$$  * getFramebuffer() is intercepted by MixinMinecraftClientThirdEye to return the
//$$  * ThirdEye FBO while isRenderingThirdEye is true, redirecting the render target.
//$$  */
//$$ @Mixin(GameRenderer.class)
//$$ public abstract class MixinGameRendererThirdEye {
//$$
//$$     @Shadow private MinecraftClient client;
//$$     @Shadow private Camera camera;
//$$     @Shadow private GlobalSettings globalSettings;
//$$
//$$     @Shadow public abstract void renderWorld(RenderTickCounter counter);
//$$
//$$     // Mirrors the GlobalSettings.set(...) call in GameRenderer.render(), reading
//$$     // the (currently overridden) this.camera so the terrain shader's camera-relative
//$$     // origin matches the ThirdEye view.
//$$     private void thirdeye$writeGlobalSettings(RenderTickCounter counter) {
//$$         globalSettings.set(
//$$                 client.getWindow().getFramebufferWidth(),
//$$                 client.getWindow().getFramebufferHeight(),
//$$                 (double) client.options.getGlintStrength().getValue(),
//$$                 client.world != null ? client.world.getTime() : 0L,
//$$                 counter,
//$$                 client.options.getMenuBackgroundBlurrinessValue(),
//$$                 camera,
//$$                 client.options.getTextureFiltering().getValue() == TextureFilteringMode.RGSS);
//$$     }
//$$
//$$     @Inject(method = "renderWorld", at = @At("RETURN"))
//$$     private void onRenderWorldReturn(RenderTickCounter counter, CallbackInfo ci) {
//$$         if (!Feature.THIRD_EYE.isEnabled())   return;
//$$         if (ThirdEye.isRenderingThirdEye)      return;   // prevent recursion via RETURN
//$$         if (!ThirdEyeWindow.isOpen())          return;
//$$         if (ThirdEye.thirdEyeFbo == null)      return;
//$$         if (!ThirdEyeCamera.initialized)       return;
//$$         if (client.world == null)              return;
//$$
//$$         // Snapshot live camera state (player view) to restore afterwards.
//$$         Vec3d savedPos = camera.getCameraPos();
//$$         float savedYaw = camera.getYaw();
//$$         float savedPitch = camera.getPitch();
//$$
//$$         // Clear ThirdEye FBO color + depth before the pass. Framebuffer no longer
//$$         // exposes clear(); the command encoder clears the backing GpuTextures.
//$$         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
//$$         encoder.clearColorAndDepthTextures(
//$$                 ThirdEye.thirdEyeFbo.getColorAttachment(), 0x00000000,
//$$                 ThirdEye.thirdEyeFbo.getDepthAttachment(), 1.0);
//$$
//$$         ThirdEye.isRenderingThirdEye = true;
//$$
//$$         MixinCameraInvokerThirdEye inv = (MixinCameraInvokerThirdEye)(Object) camera;
//$$         inv.thirdeye$setPos(ThirdEyeCamera.x, ThirdEyeCamera.y, ThirdEyeCamera.z);
//$$         inv.thirdeye$setRotation(ThirdEyeCamera.yaw, ThirdEyeCamera.pitch);
//$$         thirdeye$writeGlobalSettings(counter);   // terrain camera-relative origin → ThirdEye
//$$
//$$         try {
//$$             renderWorld(counter);
//$$         } finally {
//$$             ThirdEye.isRenderingThirdEye = false;
//$$             inv.thirdeye$setPos(savedPos.x, savedPos.y, savedPos.z);
//$$             inv.thirdeye$setRotation(savedYaw, savedPitch);
//$$             thirdeye$writeGlobalSettings(counter);   // restore UBO to player view
//$$         }
//$$
//$$         ThirdEyeWindow.blit(
//$$                 ThirdEye.thirdEyeFbo.getColorTexId(),
//$$                 ThirdEye.thirdEyeFbo.textureWidth,
//$$                 ThirdEye.thirdEyeFbo.textureHeight,
//$$                 client.getWindow().getHandle());
//$$     }
//$$
//$$     @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
//$$     private void onRenderHandHead(CallbackInfo ci) {
//$$         if (ThirdEye.isRenderingThirdEye) ci.cancel();
//$$     }
//$$ }
//#else
//$$ import com.mojang.blaze3d.systems.CommandEncoder;
//$$ import com.mojang.blaze3d.systems.RenderSystem;
//$$ import net.minecraft.client.Camera;
//$$ import net.minecraft.client.DeltaTracker;
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.client.TextureFilteringMethod;
//$$ import net.minecraft.client.renderer.GameRenderer;
//$$ import net.minecraft.client.renderer.GlobalSettingsUniform;
//$$ import net.minecraft.client.renderer.state.GameRenderState;
//$$ import net.minecraft.world.phys.Vec3;
//$$ import org.joml.Matrix4f;
//$$ import org.spongepowered.asm.mixin.Final;
//$$
//$$ /**
//$$  * MC 26.1 ThirdEye render hook.
//$$  *
//$$  * Unlike <=1.21.11, the 26.1 pipeline fully splits extraction from rendering:
//$$  * renderLevel() consumes the pre-extracted CameraRenderState / chunk culling /
//$$  * entity render states (filled during GameRenderer.extract() with the player
//$$  * camera) and never reads the live Camera. Overriding the camera alone would
//$$  * therefore have no effect. Instead, after the main renderLevel() completes we:
//$$  *   1. override the live Camera to the ThirdEye position/rotation,
//$$  *   2. re-run the level extraction (extractCamera + LevelRenderer.extractLevel)
//$$  *      so all extracted state — including ChunkSectionsToRender culling —
//$$  *      follows ThirdEye,
//$$  *   3. re-write the GlobalSettingsUniform (terrain camera-relative origin,
//$$  *      read from cameraRenderState.pos) for the pass,
//$$  *   4. recursively call renderLevel() with getMainRenderTarget() intercepted
//$$  *      to return the ThirdEye target,
//$$  *   5. restore the camera and re-extract the camera state + uniform so the
//$$  *      rest of the frame (post FX, GUI) sees player values.
//$$  */
//$$ @Mixin(GameRenderer.class)
//$$ public abstract class MixinGameRendererThirdEye {
//$$
//$$     @Shadow @Final private Minecraft minecraft;
//$$     @Shadow @Final private Camera mainCamera;
//$$     @Shadow @Final private GameRenderState gameRenderState;
//$$     @Shadow @Final private GlobalSettingsUniform globalSettingsUniform;
//$$
//$$     @Shadow public abstract void renderLevel(DeltaTracker deltaTracker);
//$$     @Shadow private void extractCamera(DeltaTracker deltaTracker, float tickDelta, float entityTickDelta) {}
//$$
//$$     // Mirrors the GlobalSettingsUniform.update(...) call in GameRenderer.render();
//$$     // cameraRenderState.pos reflects whichever camera was last extracted.
//$$     private void thirdeye$writeGlobalSettings(DeltaTracker deltaTracker) {
//$$         globalSettingsUniform.update(
//$$                 gameRenderState.windowRenderState.width,
//$$                 gameRenderState.windowRenderState.height,
//$$                 gameRenderState.optionsRenderState.glintStrength,
//$$                 minecraft.level != null ? minecraft.level.getGameTime() : 0L,
//$$                 deltaTracker,
//$$                 gameRenderState.optionsRenderState.menuBackgroundBlurriness,
//$$                 gameRenderState.levelRenderState.cameraRenderState.pos,
//$$                 gameRenderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS);
//$$     }
//$$
//$$     // Rebuilds the camera's cull frustum from its current pos/rotation
//$$     // (replicates the tail of Camera.update()).
//$$     private void thirdeye$rebuildCullFrustum(MixinCameraInvokerThirdEye inv) {
//$$         inv.thirdeye$prepareCullFrustum(
//$$                 mainCamera.getViewRotationMatrix(new Matrix4f()),
//$$                 inv.thirdeye$createProjectionMatrixForCulling(),
//$$                 mainCamera.position());
//$$     }
//$$
//$$     // Re-runs the camera + level extraction with whatever the live Camera holds.
//$$     private void thirdeye$reextract(DeltaTracker deltaTracker, boolean includeLevel) {
//$$         float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
//$$         float entityTickDelta = mainCamera.getCameraEntityPartialTicks(deltaTracker);
//$$         extractCamera(deltaTracker, tickDelta, entityTickDelta);
//$$         if (includeLevel) {
//$$             minecraft.levelRenderer.extractLevel(deltaTracker, mainCamera, tickDelta);
//$$         }
//$$         thirdeye$writeGlobalSettings(deltaTracker);
//$$     }
//$$
//$$     @Inject(method = "renderLevel", at = @At("RETURN"))
//$$     private void onRenderLevelReturn(DeltaTracker deltaTracker, CallbackInfo ci) {
//$$         if (!Feature.THIRD_EYE.isEnabled())   return;
//$$         if (ThirdEye.isRenderingThirdEye)      return;   // prevent recursion via RETURN
//$$         if (!ThirdEyeWindow.isOpen())          return;
//$$         if (ThirdEye.thirdEyeFbo == null)      return;
//$$         if (!ThirdEyeCamera.initialized)       return;
//$$         if (minecraft.level == null)           return;
//$$
//$$         // Snapshot live camera state (player view) to restore afterwards.
//$$         Vec3 savedPos = mainCamera.position();
//$$         float savedYaw = mainCamera.yRot();
//$$         float savedPitch = mainCamera.xRot();
//$$
//$$         // Clear ThirdEye target color + depth before the pass.
//$$         CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
//$$         encoder.clearColorAndDepthTextures(
//$$                 ThirdEye.thirdEyeFbo.getColorTexture(), 0x00000000,
//$$                 ThirdEye.thirdEyeFbo.getDepthTexture(), 1.0);
//$$
//$$         ThirdEye.isRenderingThirdEye = true;
//$$
//$$         MixinCameraInvokerThirdEye inv = (MixinCameraInvokerThirdEye)(Object) mainCamera;
//$$         inv.thirdeye$setPos(ThirdEyeCamera.x, ThirdEyeCamera.y, ThirdEyeCamera.z);
//$$         inv.thirdeye$setRotation(ThirdEyeCamera.yaw, ThirdEyeCamera.pitch);
//$$
//$$         try {
//$$             // The cull frustum is only computed in Camera.update() (player view), so
//$$             // rebuild it from the overridden pos/rotation (mirrors update()'s tail) —
//$$             // otherwise both terrain sections and entities stay culled to the
//$$             // player's view direction.
//$$             thirdeye$rebuildCullFrustum(inv);
//$$             // Terrain visibility (cullTerrain → visible section set) was computed in
//$$             // the update phase with the player frustum; re-run it for ThirdEye.
//$$             minecraft.levelRenderer.update(mainCamera);
//$$
//$$             thirdeye$reextract(deltaTracker, true);   // extracted state → ThirdEye view
//$$             renderLevel(deltaTracker);
//$$         } finally {
//$$             ThirdEye.isRenderingThirdEye = false;
//$$             inv.thirdeye$setPos(savedPos.x, savedPos.y, savedPos.z);
//$$             inv.thirdeye$setRotation(savedYaw, savedPitch);
//$$             thirdeye$rebuildCullFrustum(inv);
//$$             // Restore camera-derived state for the rest of the frame (post FX, GUI).
//$$             // Skip the level re-extraction: levelRenderState was already consumed,
//$$             // and the next frame's update phase re-culls terrain from the player.
//$$             thirdeye$reextract(deltaTracker, false);
//$$         }
//$$
//$$         ThirdEyeWindow.blit(
//$$                 ThirdEye.thirdEyeFbo.getColorTexId(),
//$$                 ThirdEye.thirdEyeFbo.width,
//$$                 ThirdEye.thirdEyeFbo.height,
//$$                 minecraft.getWindow().handle());
//$$     }
//$$
//$$     @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
//$$     private void onRenderItemInHandHead(CallbackInfo ci) {
//$$         if (ThirdEye.isRenderingThirdEye) ci.cancel();
//$$     }
//$$ }
//#endif
