// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(a,d){a.DecodeNormal=function(b){const c=d.glsl`
    vec3 decodeNormal(vec2 f) {
      float z = 1.0 - abs(f.x) - abs(f.y);
      return vec3(f + sign(f) * min(z, 0.0), z);
    }
  `;b.fragment.code.add(c);b.vertex.code.add(c)};Object.defineProperty(a,"__esModule",{value:!0})});