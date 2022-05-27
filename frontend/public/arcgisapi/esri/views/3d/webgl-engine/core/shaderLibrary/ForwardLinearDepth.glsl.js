// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../shaderModules/interfaces"],function(d,c){d.ForwardLinearDepth=function(a,b){0===b.output&&b.receiveShadows?(a.varyings.add("linearDepth","float"),a.vertex.code.add(c.glsl`
      void forwardLinearDepth() { linearDepth = gl_Position.w; }
    `)):1===b.output||3===b.output?(a.varyings.add("linearDepth","float"),a.vertex.uniforms.add("uCameraNearFar","vec2"),a.vertex.code.add(c.glsl`
      void forwardLinearDepth() {
        linearDepth = (-position_view().z - uCameraNearFar[0]) / (uCameraNearFar[1] - uCameraNearFar[0]);
      }
    `)):a.vertex.code.add(c.glsl`
      void forwardLinearDepth() {}
    `)};Object.defineProperty(d,"__esModule",{value:!0})});