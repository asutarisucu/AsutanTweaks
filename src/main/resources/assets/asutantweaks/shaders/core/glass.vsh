#version 330

// Glass shader for MC 1.21.11+. MC 1.19.4 - 1.21.1 use core/glass_legacy.

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <asutantweaks:glass_decode.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;

out vec2 local;
flat out vec2 params;
flat out vec2 extent;
out vec2 screenUV;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    glassDecode(UV0, local, params);
    // Every corner of the quad sits the same distance out, so this is where its edge is.
    extent = abs(local);
    screenUV = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
    vertexColor = Color;
}
