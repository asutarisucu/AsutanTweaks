// Fragment side of the glass shader. See glass_decode.glsl for how the
// parameters are packed. Sizes below are in GUI pixels (times gs, the GUI
// scale) unless they say otherwise.
//
// Modes:
//   0 backdrop   full-screen copy of what is behind the screen; a = blur, b = dim
//   1 glass      refracting panel; a = frost, b = refraction, flag = dissolve on fade
//   2 solid      anti-aliased rounded rect; flag = top sheen
//   3 hue disc   hue by angle, saturation by radius, value in colour.r
//   4 shadow     soft shadow outside the rect, reaching to the edge of the quad
//   5 bar        horizontal gradient; flag 0 = black to colour, 1 = checker to colour
//   6 outline    inner stroke of the rounded rect; a = thickness
//   7 swatch     colour over a checkerboard; b = fade out

const float GLASS_TAU = 6.28318530718;

float glassBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

vec3 glassHsv(float h, float s, float v) {
    vec3 k = clamp(abs(mod(h * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
    return v * mix(vec3(1.0), k, s);
}

float glassHash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float glassNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = glassHash(i);
    float b = glassHash(i + vec2(1.0, 0.0));
    float c = glassHash(i + vec2(0.0, 1.0));
    float d = glassHash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Gaussian-weighted spiral of taps, turned per pixel so a wide blur shows fine
// grain instead of repeated copies. radius is in screen pixels.
vec3 glassBlur(sampler2D tex, vec2 uv, vec2 texel, float radius) {
    if (radius < 0.5) {
        return textureLod(tex, uv, 0.0).rgb;
    }
    float turn = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715)))) * GLASS_TAU;
    vec3 sum = vec3(0.0);
    float total = 0.0;
    for (int i = 0; i < 40; i++) {
        float t = (float(i) + 0.5) / 40.0;
        float r = sqrt(t) * radius;
        float ang = float(i) * 2.39996323 + turn;
        float w = exp(-2.5 * t);
        sum += textureLod(tex, uv + vec2(cos(ang), sin(ang)) * r * texel, 0.0).rgb * w;
        total += w;
    }
    return sum / total;
}

vec4 glassShade(sampler2D tex, vec2 local, vec2 params, vec2 extent, vec2 screenUV, vec4 color) {
    // Derivatives first, outside any branch.
    vec2 halfSize = vec2(1.0 / max(abs(dFdx(local.x)), 1e-6), 1.0 / max(abs(dFdy(local.y)), 1e-6));
    vec2 texel = vec2(abs(dFdx(screenUV.x)), abs(dFdy(screenUV.y)));

    vec2 p = local * halfSize;
    float shortSide = min(halfSize.x, halfSize.y);
    float gs = max(floor(params.x / 256.0), 1.0);
    float radius = mod(params.x, 256.0) / 255.0 * shortSide;
    float mode = floor(params.y / 512.0);
    float flag = mod(floor(params.y / 256.0), 2.0);
    float a = mod(floor(params.y / 16.0), 16.0) / 15.0;
    float b = mod(params.y, 16.0) / 15.0;

    float d = glassBox(p, halfSize, radius);
    float cover = clamp(0.5 - d, 0.0, 1.0);

    if (mode < 0.5) {
        vec3 c = glassBlur(tex, screenUV, texel, a * 7.0 * gs);
        c *= 1.0 - b * 0.8;
        return vec4(c * color.rgb, color.a);
    }

    if (mode < 1.5) {
        float inside = max(-d, 0.0);
        float bezel = min(shortSide * 0.8, 10.0 * gs);
        float t = clamp(1.0 - inside / bezel, 0.0, 1.0);

        // Outward normal of a box rounded by the bezel width: smooth across
        // the corners, zero once past the bezel.
        vec2 q = max(abs(p) - halfSize + bezel, 0.0);
        float ql = length(q);
        vec2 n = ql > 0.0 ? q / ql * sign(p) : vec2(0.0);

        // Screen space runs bottom-up, local space top-down.
        float bend = t * t * b;
        vec2 off = -n * bend * bezel * 1.1;
        vec2 offUV = vec2(off.x, -off.y) * texel;

        vec3 c = glassBlur(tex, screenUV + offUV, texel, mix(0.3, 5.0, a) * gs);
        if (bend > 0.0) {
            vec3 sR = textureLod(tex, screenUV + offUV * 1.2, 0.0).rgb;
            vec3 sG = textureLod(tex, screenUV + offUV, 0.0).rgb;
            vec3 sB = textureLod(tex, screenUV + offUV * 0.8, 0.0).rgb;
            c += vec3(sR.r - sG.r, 0.0, sB.b - sG.b) * t;
        }

        c = mix(c, color.rgb, mix(0.16, 0.58, a));

        vec2 light = vec2(-0.6, -0.8);
        float facing = dot(n, light);
        float rim = 1.0 - smoothstep(0.0, max(1.0, 0.9 * gs), inside);
        c += vec3(rim * (0.16 + 0.55 * max(facing, 0.0)));
        c += vec3(t * t * 0.12 * facing);
        c += vec3(0.05 * (1.0 - smoothstep(-halfSize.y, halfSize.y * 0.2, p.y)));

        float alpha = cover;
        if (flag > 0.5) {
            // Melt away from the top as the fade runs.
            float k = 1.0 - color.a;
            if (k > 0.0) {
                float field = glassNoise(p / (5.0 * gs)) * 0.6 + glassNoise(p / (2.0 * gs)) * 0.25
                        + (0.5 + 0.5 * p.y / halfSize.y) * 0.3;
                float th = k * 1.2;
                if (field < th) {
                    alpha = 0.0;
                }
                float edge = 1.0 - smoothstep(th, th + 0.08, field);
                c += vec3(0.55, 0.75, 1.0) * edge * 0.8;
            }
        } else {
            alpha *= color.a;
        }
        return vec4(c, alpha);
    }

    if (mode < 2.5) {
        vec3 c = color.rgb;
        if (flag > 0.5) {
            c += vec3(0.10 * (1.0 - smoothstep(-halfSize.y, halfSize.y, p.y)));
        }
        return vec4(c, cover * color.a);
    }

    if (mode < 3.5) {
        float dist = length(p);
        float cov = clamp(0.5 - (dist - shortSide), 0.0, 1.0);
        float hue = fract(atan(p.y, p.x) / GLASS_TAU);
        float sat = clamp(dist / shortSide, 0.0, 1.0);
        return vec4(glassHsv(hue, sat, color.r), cov * color.a);
    }

    if (mode < 4.5) {
        float spread = max((extent.x - 1.0) * halfSize.x, 1.0);
        float s = 1.0 - clamp(d / spread, 0.0, 1.0);
        float outside = smoothstep(-1.0, 0.5, d);
        return vec4(color.rgb, color.a * s * s * outside);
    }

    if (mode < 5.5) {
        float f = clamp(p.x / halfSize.x * 0.5 + 0.5, 0.0, 1.0);
        vec3 c;
        if (flag < 0.5) {
            c = color.rgb * f;
        } else {
            vec2 cell = floor((p + halfSize) / (2.0 * gs));
            float chk = mod(cell.x + cell.y, 2.0) < 1.0 ? 0.85 : 0.55;
            c = mix(vec3(chk), color.rgb, f);
        }
        return vec4(c, cover * color.a);
    }

    if (mode < 6.5) {
        // Inner stroke of the rounded rect; a circle when the quad is square and fully rounded.
        float th = max(a * 15.0, 1.0) * gs;
        float dr = abs(d + th * 0.5) - th * 0.5;
        return vec4(color.rgb, clamp(0.5 - dr, 0.0, 1.0) * color.a);
    }

    vec2 cell = floor((p + halfSize) / (2.0 * gs));
    float chk = mod(cell.x + cell.y, 2.0) < 1.0 ? 0.85 : 0.55;
    return vec4(mix(vec3(chk), color.rgb, color.a), cover * (1.0 - b));
}
