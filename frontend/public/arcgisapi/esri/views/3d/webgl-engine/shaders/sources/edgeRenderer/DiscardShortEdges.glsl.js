// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/shaderModules/interfaces"],function(c,b){c.DiscardShortEdges=function(a,d){a=a.vertex;switch(d.mode){case 1:a.code.add(b.glsl`
        #define discardShortEdges(unpackedAttributes, lineLengthPixels) { if (lineLengthPixels <= 3.0) { gl_Position = vec4(10.0, 10.0, 10.0, 1.0); return; }}
      `);break;case 2:a.code.add(b.glsl`
        #define discardShortEdges(unpackedAttributes, lineLengthPixels) { if (unpackedAttributes.type <= 0.0 && lineLengthPixels <= 3.0) { gl_Position = vec4(10.0, 10.0, 10.0, 1.0); return; }}
      `);break;case 0:a.code.add(b.glsl`
        #define discardShortEdges(unpackedAttributes, lineLengthPixels) {}
      `)}};Object.defineProperty(c,"__esModule",{value:!0})});