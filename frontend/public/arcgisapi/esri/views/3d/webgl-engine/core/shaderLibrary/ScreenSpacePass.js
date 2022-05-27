// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../shaderModules/interfaces"],function(b,c){b.ScreenSpacePass=function(a){a.attributes.add("position","vec2");a.varyings.add("uv","vec2");a.vertex.code.add(c.glsl`
    void main(void) {
      gl_Position = vec4(position, 0.0, 1.0);
      uv = position * 0.5 + vec2(0.5);
    }
  `)};Object.defineProperty(b,"__esModule",{value:!0})});