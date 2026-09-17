#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <asutantweaks:glass_shade.glsl>

uniform sampler2D Sampler0;

in vec2 local;
flat in vec2 params;
flat in vec2 extent;
in vec2 screenUV;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    fragColor = glassShade(Sampler0, local, params, extent, screenUV, vertexColor) * ColorModulator;
}
