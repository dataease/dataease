// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(a,c){a.FoamColor=function(b){b.fragment.code.add(c.glsl`
    vec3 foamIntensity2FoamColor(float foamIntensityExternal, float foamPixelIntensity, vec3 skyZenitColor, float dayMod){
      return foamIntensityExternal * (0.075 * skyZenitColor * pow(foamPixelIntensity, 4.) +  50.* pow(foamPixelIntensity, 23.0)) * dayMod;
    }
  `)};a.FoamIntensity=function(b){b.fragment.code.add(c.glsl`
    float normals2FoamIntensity(vec3 n, float waveStrength){
      float normalizationFactor =  max(0.015, waveStrength);
      return max((n.x + n.y)*0.3303545/normalizationFactor + 0.3303545, 0.0);
    }
  `)};Object.defineProperty(a,"__esModule",{value:!0})});