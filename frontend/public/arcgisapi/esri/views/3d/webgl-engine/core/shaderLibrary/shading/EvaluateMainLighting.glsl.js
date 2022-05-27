// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(b,c){b.EvaluateMainLighting=function(a){a=a.fragment;a.uniforms.add("lightingMainDirection","vec3");a.uniforms.add("lightingMainIntensity","vec3");a.uniforms.add("lightingFixedFactor","float");a.code.add(c.glsl`
    vec3 evaluateMainLighting(vec3 normal_global, float shadowing) {
      float dotVal = clamp(-dot(normal_global, lightingMainDirection), 0.0, 1.0);

      // move lighting towards (1.0, 1.0, 1.0) if requested
      dotVal = mix(dotVal, 1.0, lightingFixedFactor);

      return lightingMainIntensity * ((1.0 - shadowing) * dotVal);
    }
  `)};Object.defineProperty(b,"__esModule",{value:!0})});