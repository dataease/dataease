// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../util/RgbaFloatEncoding.glsl"],function(a,c,d){a.ReadLinearDepth=function(b){b.fragment.include(d.RgbaFloatEncoding);b.fragment.code.add(c.glsl`
    float linearDepthFromTexture(sampler2D depthTex, vec2 uv, vec2 nearFar) {
      return -(rgba2float(texture2D(depthTex, uv)) * (nearFar[1] - nearFar[0]) + nearFar[0]);
    }
  `)};Object.defineProperty(a,"__esModule",{value:!0})});