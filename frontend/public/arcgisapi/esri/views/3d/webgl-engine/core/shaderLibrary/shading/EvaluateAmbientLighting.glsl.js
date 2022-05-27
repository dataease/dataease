// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(e,b){e.EvaluateAmbientLighting=function(a,c){a=a.fragment;const d=void 0!==c.lightingSphericalHarmonicsOrder?c.lightingSphericalHarmonicsOrder:2;0===d?(a.uniforms.add("lightingAmbientSH0","vec3"),a.code.add(b.glsl`
      vec3 calculateAmbientIrradiance(vec3 normal, float ambientOcclusion) {
        vec3 ambientLight = 0.282095 * lightingAmbientSH0;
        return ambientLight * (1.0 - ambientOcclusion);
      }
    `)):1===d?(a.uniforms.add("lightingAmbientSH_R","vec4"),a.uniforms.add("lightingAmbientSH_G","vec4"),a.uniforms.add("lightingAmbientSH_B","vec4"),a.code.add(b.glsl`
      vec3 calculateAmbientIrradiance(vec3 normal, float ambientOcclusion) {
        vec4 sh0 = vec4(
          0.282095,
          0.488603 * normal.x,
          0.488603 * normal.z,
          0.488603 * normal.y
        );
        vec3 ambientLight = vec3(
          dot(lightingAmbientSH_R, sh0),
          dot(lightingAmbientSH_G, sh0),
          dot(lightingAmbientSH_B, sh0)
        );
        return ambientLight * (1.0 - ambientOcclusion);
      }
    `)):2===d&&(a.uniforms.add("lightingAmbientSH0","vec3"),a.uniforms.add("lightingAmbientSH_R1","vec4"),a.uniforms.add("lightingAmbientSH_G1","vec4"),a.uniforms.add("lightingAmbientSH_B1","vec4"),a.uniforms.add("lightingAmbientSH_R2","vec4"),a.uniforms.add("lightingAmbientSH_G2","vec4"),a.uniforms.add("lightingAmbientSH_B2","vec4"),a.code.add(b.glsl`
      vec3 calculateAmbientIrradiance(vec3 normal, float ambientOcclusion) {
        vec3 ambientLight = 0.282095 * lightingAmbientSH0;

        vec4 sh1 = vec4(
          0.488603 * normal.x,
          0.488603 * normal.z,
          0.488603 * normal.y,
          1.092548 * normal.x * normal.y
        );
        vec4 sh2 = vec4(
          1.092548 * normal.y * normal.z,
          0.315392 * (3.0 * normal.z * normal.z - 1.0),
          1.092548 * normal.x * normal.z,
          0.546274 * (normal.x * normal.x - normal.y * normal.y)
        );
        ambientLight += vec3(
          dot(lightingAmbientSH_R1, sh1),
          dot(lightingAmbientSH_G1, sh1),
          dot(lightingAmbientSH_B1, sh1)
        );
        ambientLight += vec3(
          dot(lightingAmbientSH_R2, sh2),
          dot(lightingAmbientSH_G2, sh2),
          dot(lightingAmbientSH_B2, sh2)
        );
        return ambientLight * (1.0 - ambientOcclusion);
      }
    `),1!==c.pbrMode&&2!==c.pbrMode||a.code.add(b.glsl`
        const vec3 skyTransmittance = vec3(0.9, 0.9, 1.0);

        vec3 calculateAmbientRadiance(float ambientOcclusion)
        {
          vec3 ambientLight = 1.2 * (0.282095 * lightingAmbientSH0) - 0.2;
          return ambientLight *= (1.0 - ambientOcclusion) * skyTransmittance;
        }
      `))};Object.defineProperty(e,"__esModule",{value:!0})});