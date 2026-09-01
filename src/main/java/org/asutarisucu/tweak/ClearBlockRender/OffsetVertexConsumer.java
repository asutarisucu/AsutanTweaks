package org.asutarisucu.tweak.ClearBlockRender;

//#if MC < 260100
import net.minecraft.client.render.VertexConsumer;
//#else
//$$ import com.mojang.blaze3d.vertex.VertexConsumer;
//#endif

/**
 * Passes vertices through to another consumer, shifting each position.
 *
 * The fluid renderer takes no matrix and writes its vertices at chunk-local
 * coordinates ({@code pos & 15}), because in the game it only ever fills a
 * section mesh that is placed afterwards. Nothing places ours, so this moves
 * each fluid block from its chunk corner to where it belongs in the
 * region-local capture space.
 *
 * One instance is reused for the whole pass; {@link #set} re-points it before
 * every block.
 */
public final class OffsetVertexConsumer implements VertexConsumer {

    private VertexConsumer delegate;
    private float dx, dy, dz;
    private int vertices;
    private float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
    private float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
    private int firstColor, firstLight;

    private void note(float x, float y, float z) {
        this.vertices++;
        if (x < this.minX) this.minX = x;
        if (y < this.minY) this.minY = y;
        if (z < this.minZ) this.minZ = z;
        if (x > this.maxX) this.maxX = x;
        if (y > this.maxY) this.maxY = y;
        if (z > this.maxZ) this.maxZ = z;
    }

    /** Clears the tally at the start of a capture. */
    public void reset() {
        this.vertices = 0;
        this.minX = this.minY = this.minZ = Float.MAX_VALUE;
        this.maxX = this.maxY = this.maxZ = -Float.MAX_VALUE;
        this.firstColor = 0;
        this.firstLight = 0;
    }

    /** What went through, for the capture geometry log line. */
    public String describe() {
        if (this.vertices == 0) return "none";
        return String.format("%d verts, x %.2f..%.2f y %.2f..%.2f z %.2f..%.2f, first argb %08X light %08X",
                this.vertices, this.minX, this.maxX, this.minY, this.maxY, this.minZ, this.maxZ,
                this.firstColor, this.firstLight);
    }

    public OffsetVertexConsumer set(VertexConsumer delegate, float dx, float dy, float dz) {
        this.delegate = delegate;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        return this;
    }

//#if MC < 12101
    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        this.note((float) (x + this.dx), (float) (y + this.dy), (float) (z + this.dz));
        this.delegate.vertex(x + this.dx, y + this.dy, z + this.dz);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.delegate.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.delegate.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.delegate.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void next() {
        this.delegate.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        this.delegate.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        this.delegate.unfixColor();
    }
//#elseif MC < 12111
    //$$ @Override
    //$$ public VertexConsumer vertex(float x, float y, float z) {
    //$$     this.note(x + this.dx, y + this.dy, z + this.dz);
    //$$     this.delegate.vertex(x + this.dx, y + this.dy, z + this.dz);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer color(int red, int green, int blue, int alpha) {
    //$$     this.delegate.color(red, green, blue, alpha);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer texture(float u, float v) {
    //$$     this.delegate.texture(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer overlay(int u, int v) {
    //$$     this.delegate.overlay(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer light(int u, int v) {
    //$$     this.delegate.light(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer normal(float x, float y, float z) {
    //$$     this.delegate.normal(x, y, z);
    //$$     return this;
    //$$ }
//#elseif MC < 260100
    //$$ @Override
    //$$ public VertexConsumer vertex(float x, float y, float z) {
    //$$     this.note(x + this.dx, y + this.dy, z + this.dz);
    //$$     this.delegate.vertex(x + this.dx, y + this.dy, z + this.dz);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer color(int red, int green, int blue, int alpha) {
    //$$     this.delegate.color(red, green, blue, alpha);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer color(int argb) {
    //$$     this.delegate.color(argb);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer texture(float u, float v) {
    //$$     this.delegate.texture(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer overlay(int u, int v) {
    //$$     this.delegate.overlay(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer light(int u, int v) {
    //$$     this.delegate.light(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer normal(float x, float y, float z) {
    //$$     this.delegate.normal(x, y, z);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer lineWidth(float width) {
    //$$     this.delegate.lineWidth(width);
    //$$     return this;
    //$$ }
//#else
    //$$ @Override
    //$$ public VertexConsumer addVertex(float x, float y, float z) {
    //$$     this.note(x + this.dx, y + this.dy, z + this.dz);
    //$$     this.delegate.addVertex(x + this.dx, y + this.dy, z + this.dz);
    //$$     return this;
    //$$ }
    //$$
    //$$ // Forwarded whole rather than left to the interface default, which would take
    //$$ // the delegate through the one-attribute-at-a-time path. BufferBuilder has a
    //$$ // single-write fast path for the block format the capture draws with, and this
    //$$ // is the call the fluid renderer actually makes.
    //$$ //
    //$$ // The colour is forced opaque on the way through. FluidRenderer passes the
    //$$ // biome tint straight into the vertex colour, and the tint arrives with its
    //$$ // alpha byte clear (measured: 003F76E4 for water), which the block shader
    //$$ // then multiplies into the texture and leaves the fluid invisible. A fluid's
    //$$ // transparency comes from its texture — every version before 26.1 wrote the
    //$$ // vertex alpha as 1.0 outright.
    //$$ @Override
    //$$ public void addVertex(float x, float y, float z, int color, float u, float v,
    //$$                       int overlayCoords, int lightCoords, float nx, float ny, float nz) {
    //$$     if (this.vertices == 0) {
    //$$         this.firstColor = color;
    //$$         this.firstLight = lightCoords;
    //$$     }
    //$$     this.note(x + this.dx, y + this.dy, z + this.dz);
    //$$     this.delegate.addVertex(x + this.dx, y + this.dy, z + this.dz,
    //$$             color | 0xFF000000, u, v, overlayCoords, lightCoords, nx, ny, nz);
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setColor(int red, int green, int blue, int alpha) {
    //$$     this.delegate.setColor(red, green, blue, alpha);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setColor(int argb) {
    //$$     this.delegate.setColor(argb);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setUv(float u, float v) {
    //$$     this.delegate.setUv(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setUv1(int u, int v) {
    //$$     this.delegate.setUv1(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setUv2(int u, int v) {
    //$$     this.delegate.setUv2(u, v);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setNormal(float x, float y, float z) {
    //$$     this.delegate.setNormal(x, y, z);
    //$$     return this;
    //$$ }
    //$$
    //$$ @Override
    //$$ public VertexConsumer setLineWidth(float width) {
    //$$     this.delegate.setLineWidth(width);
    //$$     return this;
    //$$ }
//#endif
}
