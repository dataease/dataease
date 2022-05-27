// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(b,c){b.AlignPixel=function(a){const d=c.glsl`
    vec4 alignToPixelCenter(vec4 clipCoord, vec2 widthHeight) {
      vec2 xy = vec2(0.500123) + 0.5 * clipCoord.xy / clipCoord.w;
      vec2 pixelSz = vec2(1.0) / widthHeight;
      vec2 ij = (floor(xy * widthHeight) + vec2(0.5)) * pixelSz;
      vec2 result = (ij * 2.0 - vec2(1.0)) * clipCoord.w;
      return vec4(result, clipCoord.zw);
    }
  `,e=c.glsl`

    vec4 alignToPixelOrigin(vec4 clipCoord, vec2 widthHeight) {
      vec2 xy = vec2(0.5) + 0.5 * clipCoord.xy / clipCoord.w;
      vec2 pixelSz = vec2(1.0) / widthHeight;
      vec2 ij = floor((xy + 0.5 * pixelSz) * widthHeight) * pixelSz;
      vec2 result = (ij * 2.0 - vec2(1.0)) * clipCoord.w;
      return vec4(result, clipCoord.zw);
    }
  `;a.vertex.code.add(d);a.vertex.code.add(e);a.fragment.code.add(d);a.fragment.code.add(e)};Object.defineProperty(b,"__esModule",{value:!0})});