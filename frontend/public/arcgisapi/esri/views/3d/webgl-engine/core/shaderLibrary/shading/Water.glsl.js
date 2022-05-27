// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../shaderModules/interfaces ./ScreenSpaceReflections.glsl ./FoamRendering.glsl ./Gamma.glsl ./PhysicallyBasedRendering.glsl".split(" "),function(d,c,e,f,g,h){d.Water=function(a,b){a.include(h.PhysicallyBasedRendering,b);a.include(g.Gamma);a.include(f.FoamColor);b.ssrEnabled&&a.include(e.ScreenSpaceReflections,b);a.fragment.code.add(c.glsl`
    const vec3 fresnelSky =  vec3(0.02, 1.0, 15.0); // f0, f0max, exp
    const vec2 fresnelMaterial =  vec2(0.02, 0.1); // f0, f0max for specular term
    const float roughness = 0.015;
    const float foamIntensityExternal = 1.7;
    const float ssrIntensity = 0.65;
    const float ssrHeightFadeStart = 300000.0;
    const float ssrHeightFadeEnd = 500000.0;
    const float waterDiffusion = 0.775;
    const float waterSeeColorMod = 0.8;
    const float correctionViewingPowerFactor = 0.4;

    const vec3 skyZenitColor = vec3(0.52, 0.68, 0.90);
    const vec3 skyColor = vec3(0.67, 0.79, 0.9);

    PBRShadingWater shadingInfo;

    /*
    *   This function is an approximation for the sky gradient reflected
    *   the water surface and describes a combination of two fresnel terms.
    *   @parameter: cosTheta = is the result of max(dot(n,v), 0.0)
    *   @parameter: horizon = the dominant color of the sky horizon
    *   @parameter: cosTheta = the dominant color of the sky zenit
    */
    vec3 getSkyGradientColor(in float cosTheta, in vec3 horizon, in vec3 zenit) {
      float exponent = pow((1.0 - cosTheta), fresnelSky[2]);
      return mix(zenit, horizon, exponent);
    }

    /*
    *   This function determines the water color per pixel.
    *   @parameter: n = normal facing away from the surface
    *   @parameter: v = view direction facing away from the surface.
    *   @parameter: l = light direction facing away from the surface
    *   @parameter: lightIntensity = light intensity, currently between 0...PI
    *   @parameter: localUp = a normal for the general direction of the surface
    *   @parameter: shadow = the amount of shadow at this pixel (0 = no shadow)
    */
    vec3 getSeaColor(in vec3 n, in vec3 v, in vec3 l, vec3 color, in vec3 lightIntensity, in vec3 localUp, in float shadow, float foamIntensity, vec3 positionView) {

      float reflectionHit = 0.;
      vec3 seaWaterColor = linearizeGamma(color);
      // using half vector to determine the specular light
      vec3 h = normalize(l + v);
      shadingInfo.NdotL = clamp(dot(n, l), 0.0, 1.0);
      shadingInfo.NdotV = clamp(dot(n, v), 0.001, 1.0);
      shadingInfo.VdotN = clamp(dot(v, n), 0.001, 1.0);
      shadingInfo.NdotH = clamp(dot(n, h), 0.0, 1.0);
      shadingInfo.VdotH = clamp(dot(v, h), 0.0, 1.0);
      shadingInfo.LdotH = clamp(dot(l, h), 0.0, 1.0);

      // angle between vertex normal and view direction
      float upDotV = max(dot(localUp,v), 0.0);
      // reflected sky color: the reflected sky color consists of two main colors, the
      // reflected color at the horizon and the reflected color of the zenit.
      // the reflected sky color is then an approximation based on the fresnel term.
      vec3 skyHorizon = linearizeGamma(skyColor);
      vec3 skyZenit = linearizeGamma(skyZenitColor);
      vec3 skyColor = getSkyGradientColor(upDotV, skyHorizon, skyZenit );

      // we use the upDotL to smoothen out the
      // reflected color of the water
      float upDotL = max(dot(localUp,l),0.0);

      // The approximated sky color is adjusted according to the sun position.
      // This is done as approximation for e.g. night views.
      float daytimeMod = 0.1 + upDotL * 0.9;
      skyColor *= daytimeMod;

      // If a water surface is in shadow we just use a slight darkening of the
      // water surface expressed with this shadowModifier.
      float shadowModifier = clamp(shadow, 0.8, 1.0);

      // The reflected sky color consists of the fresnel reflection multiplied with the approximated sky color.
      // The shadow is influencing the frensel term to keep the shadow impression for really near views. As long
      // as reflection are absent there is a need to have a slight shadow for depth perception.
      vec3 fresnelModifier = fresnelReflection(shadingInfo.VdotN, vec3(fresnelSky[0]), fresnelSky[1]);
      vec3 reflSky = fresnelModifier * skyColor * shadowModifier;

      // The reflected sea color is the input water color combined with the reflected sky color.
      // The reflected sky color is modified by the incoming light.
      vec3 reflSea = seaWaterColor * mix(skyColor, upDotL * lightIntensity * LIGHT_NORMALIZATION, 2.0 / 3.0) * shadowModifier;

      vec3 specular = vec3(0.0);
      // This prevents the specular light to be rendered when:
      // - sun is behind a polygon (e.g. sundown for elevated polygons where nDotL might be still ok)
      // - viewer is under water (for this localUp is better than n)
      if(upDotV > 0.0 && upDotL > 0.0) {
        // calculate the cook torrance BRDF but with simplified occlusion
        vec3 specularSun = brdfSpecularWater(shadingInfo, roughness, vec3(fresnelMaterial[0]), fresnelMaterial[1]);

        // Normalize light intensity to be between 0...1. Shadow cancels out specular light here
        vec3 incidentLight = lightIntensity * LIGHT_NORMALIZATION * shadow;

        specular = shadingInfo.NdotL * incidentLight * specularSun;
      }

      vec3 foam = vec3(0.0);
      if(upDotV > 0.0) {
        foam = foamIntensity2FoamColor(foamIntensityExternal, foamIntensity, skyZenitColor, daytimeMod);
      }
      `);b.ssrEnabled?a.fragment.code.add(c.glsl`
      // Convert the world position to view position
      vec4 viewPosition = vec4(positionView.xyz, 1.0);
      vec3 viewDir = normalize(viewPosition.xyz);
      vec4 viewNormalVectorCoordinate = ssrViewMat *vec4(n, 0.0);
      vec3 viewNormal = normalize(viewNormalVectorCoordinate.xyz);
      vec4 viewUp = ssrViewMat *vec4(localUp, 0.0);

      // at steeper viewing angles we use more of a vertex normal (in this case up) then the wave normal
      // this removes some artifacts of normal mapping
      float correctionViewingFactor = pow(max(dot(-viewDir, viewUp.xyz), 0.0), correctionViewingPowerFactor);
      vec3 viewNormalCorrected = mix(viewUp.xyz, viewNormal, correctionViewingFactor);

      vec3 reflected = normalize(reflect(viewDir, viewNormalCorrected));

      // perform screen space reflection to detect hit
      vec3 hitCoordinate = screenSpaceIntersection( normalize(reflected), viewPosition.xyz, viewDir, viewUp.xyz);
      vec3 reflectedColor = vec3(0.0);

      // if there is a hit with ssr find reflected color from the reprojeted frame
      if (hitCoordinate.z > 0.0)
      {
        vec2 reprojectedCoordinate = reprojectionCoordinate(hitCoordinate);

        // fade out if there if the hit is near end of Y axis
        vec2 dCoords = smoothstep(0.3, 0.6, abs(vec2(0.5, 0.5) - hitCoordinate.xy));
        float heightMod = smoothstep(ssrHeightFadeEnd, ssrHeightFadeStart, -positionView.z);
        reflectionHit = waterDiffusion * clamp(1.0 - (1.3*dCoords.y), 0.0, 1.0) * heightMod;

        reflectedColor = linearizeGamma(texture2D(lastFrameColorMap, reprojectedCoordinate).xyz)* reflectionHit * fresnelModifier.y * ssrIntensity;
      }
      float seeColorMod =  mix(waterSeeColorMod, waterSeeColorMod*0.5, reflectionHit);
      // combining reflected sky, reflected sea, specular highlight and SSR reflections.
      return tonemapACES((1. - reflectionHit) * reflSky + reflectedColor + reflSea * seeColorMod + specular + foam);
    }
  `):a.fragment.code.add(c.glsl`
      // combining reflected sky, reflected sea, specular highlight and SSR reflections.
      return tonemapACES(reflSky + reflSea * waterSeeColorMod + specular + foam);
    }
  `)};Object.defineProperty(d,"__esModule",{value:!0})});