// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(c,b){c.VertexColor=function(a,d){d.attributeColor?(a.attributes.add("color","vec4"),a.varyings.add("vColor","vec4"),a.vertex.code.add(b.glsl`
      void forwardVertexColor() { vColor = color; }
    `),a.vertex.code.add(b.glsl`
      void forwardNormalizedVertexColor() { vColor = color * 0.003921568627451; }
    `)):a.vertex.code.add(b.glsl`
      void forwardVertexColor() {}
      void forwardNormalizedVertexColor() {}
    `)};Object.defineProperty(c,"__esModule",{value:!0})});