// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/shaderModules/interfaces"],function(c,b){c.VertexDiscardByOpacity=function(a,d){a=a.vertex;a.code.add(b.glsl`
  #define VERTEX_DISCARD_CUTOFF (1.0 - 1.0 / 255.0)
  `);switch(d.vertexDiscardMode){case 0:a.code.add(b.glsl`
        #define vertexDiscardByOpacity(_opacity_) {}
      `);break;case 2:a.code.add(b.glsl`
        #define vertexDiscardByOpacity(_opacity_) { if (_opacity_ >  VERTEX_DISCARD_CUTOFF) {  gl_Position = vec4(1e38, 1e38, 1e38, 1.0); return; } }
      `);break;case 1:a.code.add(b.glsl`
        #define vertexDiscardByOpacity(_opacity_) { if (_opacity_ <= VERTEX_DISCARD_CUTOFF) {  gl_Position = vec4(1e38, 1e38, 1e38, 1.0); return; } }
      `)}};Object.defineProperty(c,"__esModule",{value:!0})});