// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(b,c){b.CameraSpace=function(a){a.fragment.uniforms.add("projInfo","vec4");a.fragment.uniforms.add("zScale","vec2");a.fragment.code.add(c.glsl`
    vec3 reconstructPosition(vec2 fragCoord, float depth) {
      return vec3((fragCoord * projInfo.xy + projInfo.zw) * (zScale.x * depth + zScale.y), depth);
    }
  `)};Object.defineProperty(b,"__esModule",{value:!0})});