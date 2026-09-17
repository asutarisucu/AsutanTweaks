// Shared by core/glass (MC 1.21.11+) and core/glass_legacy (MC 1.19.4 - 1.21.1).
//
// The GUI only hands a quad its position, UV and colour, so the shape and its
// parameters travel in the UV:
//   UV.x = local.x + 8 * R   R: corner radius, 0-255 as a fraction of the shorter half side,
//                            plus 256 * the GUI scale, so sizes can be given in GUI pixels
//   UV.y = local.y + 8 * P   P: mode * 512 + flag * 256 + a * 16 + b
// local runs from -1 to 1 across the shape, and further out on a quad padded for
// a shadow. It must stay inside (-4, 4) for the decode to hold.
//
// Decoding happens per vertex, where the values are exact; interpolating the
// packed value instead would cost the edge its precision.
void glassDecode(vec2 uv, out vec2 local, out vec2 params) {
    float r = floor((uv.x + 4.0) / 8.0);
    float p = floor((uv.y + 4.0) / 8.0);
    local = vec2(uv.x - 8.0 * r, uv.y - 8.0 * p);
    params = vec2(r, p);
}
