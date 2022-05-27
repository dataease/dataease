/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{Z as e,g as r}from"./PiUtils.glsl.js";function a(a){a.fragment.include(e),a.fragment.code.add(r`
    float linearDepthFromTexture(sampler2D depthTex, vec2 uv, vec2 nearFar) {
      return -(rgba2float(texture2D(depthTex, uv)) * (nearFar[1] - nearFar[0]) + nearFar[0]);
    }
  `)}function o(e){e.attributes.add("position","vec2"),e.varyings.add("uv","vec2"),e.vertex.code.add(r`
    void main(void) {
      gl_Position = vec4(position, 0.0, 1.0);
      uv = position * 0.5 + vec2(0.5);
    }
  `)}function t(e){e.fragment.uniforms.add("projInfo","vec4"),e.fragment.uniforms.add("zScale","vec2"),e.fragment.code.add(r`
    vec3 reconstructPosition(vec2 fragCoord, float depth) {
      return vec3((fragCoord * projInfo.xy + projInfo.zw) * (zScale.x * depth + zScale.y), depth);
    }
  `)}export{t as C,a as R,o as S};
