// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(c,b){c.VertexTangent=function(a,d){a.varyings.add("tbnTangent","vec3");a.varyings.add("tbnBiTangent","vec3");1===d.viewingMode?a.vertex.code.add(b.glsl`
      void forwardVertexTangent(vec3 n) {
        tbnTangent = normalize(cross(vec3(0.0, 0.0, 1.0), n));
        tbnBiTangent = normalize(cross(n, tbnTangent));
      }
    `):a.vertex.code.add(b.glsl`
      void forwardVertexTangent(vec3 n) {
        tbnTangent = vec3(1.0, 0.0, 0.0);
        tbnBiTangent = normalize(cross(n, tbnTangent));
      }
    `);a.fragment.code.add(b.glsl`
      mat3 getTBNMatrix(vec3 n) {
        return mat3(tbnTangent, tbnBiTangent, n);
      }
    `)};Object.defineProperty(c,"__esModule",{value:!0})});