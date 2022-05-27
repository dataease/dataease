// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/shaderModules/interfaces","../../../core/shaderLibrary/attributes/VertexPosition.glsl","../../../core/shaderLibrary/util/IsNaN.glsl"],function(d,b,e,f){d.AdjustProjectedPosition=function(a,c){a.vertex.include(f.IsNaN);a.include(e.VertexPosition,c);a=a.vertex;a.uniforms.add("uDepthBias","vec2");a.uniforms.add("uViewportDimInv","vec2");c.legacy?(a.uniforms.add("uView","mat4"),a.uniforms.add("uProj","mat4")):a.uniforms.add("uTransformNormal_ViewFromGlobal","mat3");c.legacy?
a.code.add(b.glsl`
      vec2 calculateProjectedBiasXY(vec4 projPos, vec3 globalNormal) {
        float offsetXY = uDepthBias.x;
        float offsetZ  = uDepthBias.y;

        // screen space pixel offset
        // we multiply by two to account for the fact that NDC go from -1 to 1
        // we multiply by projPos.w to compensate for the perspective division that happens later
        // normalizing over xyz means that the xy influence is reduced the more the normal is pointing
        // towards the camera
        vec4 projNormal = uProj * uView * vec4(globalNormal, 0.0);

        return offsetXY * projPos.w * 2.0 * uViewportDimInv * normalize(projNormal.xyz).xy;
      }
    `):a.code.add(b.glsl`
      vec2 calculateProjectedBiasXY(vec4 projPos, vec3 globalNormal) {
        float offsetXY = uDepthBias.x;
        float offsetZ  = uDepthBias.y;

        // screen space pixel offset
        // we multiply by two to account for the fact that NDC go from -1 to 1
        // we multiply by projPos.w to compensate for the perspective division that happens later
        // normalizing over xyz means that the xy influence is reduced the more the normal is pointing
        // towards the camera
        vec4 projNormal = uTransform_ProjFromView * vec4(uTransformNormal_ViewFromGlobal * globalNormal, 0.0);

        return offsetXY * projPos.w * 2.0 * uViewportDimInv * normalize(projNormal.xyz).xy;
      }
    `);a.code.add(b.glsl`
    // A z-offset, using a depth based heuristic.
    float _calculateProjectedBiasZ(vec4 projPos) {
      float offsetZ = uDepthBias.y;
      return sqrt(projPos.z) * offsetZ;
    }

    vec4 adjustProjectedPosition(vec4 projPos, vec3 worldNormal, float lineWidth) {
      vec2 offsetXY = calculateProjectedBiasXY(projPos, worldNormal);

      // we currently have to do this check because some geometries come with 0 length edge normals.
      // see https://devtopia.esri.com/WebGIS/arcgis-js-api/issues/12890
      if (!isNaN(offsetXY.x) && !isNaN(offsetXY.y)) {
        projPos.xy += offsetXY;
      }

      projPos.z += _calculateProjectedBiasZ(projPos);

      return projPos;
    }
  `)};Object.defineProperty(d,"__esModule",{value:!0})});