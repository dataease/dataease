// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputHighlight.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputDepth.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/NormalUtils.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/EvaluateSceneLighting.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/HeaderComment.glsl ../views/3d/webgl-engine/core/shaderLibrary/attributes/VertexTangent.glsl ../views/3d/webgl-engine/core/shaderLibrary/terrain/Overlay.glsl ../views/3d/webgl-engine/core/shaderLibrary/terrain/Skirts.glsl ../views/3d/webgl-engine/core/shaderLibrary/terrain/TerrainTexture.glsl".split(" "),
function(h,c,d,m,n,p,q,e,r,t,u,k,v,w){function l(b){const a=new m.ShaderBuilder;a.include(t.HeaderComment,{name:"Terrain Shader",output:b.output});a.include(v.Skirts);a.attributes.add("position","vec3");a.attributes.add("uv0","vec2");a.vertex.uniforms.add("proj","mat4").add("view","mat4").add("origin","vec3").add("skirtScale","float");if(0===b.output){a.include(d.Transform,{linearDepth:!1});a.include(e.NormalUtils,b);a.include(w.TerrainTexture,b);const g=0!==b.overlayMode,f=2===b.overlayMode;g&&a.include(k.Overlay,
{pbrMode:3,useCustomDTRExponentForWater:!1,ssrEnabled:b.ssrEnabled,highStepCount:b.highStepCount});f&&a.include(u.VertexTangent,b);a.varyings.add("vnormal","vec3");a.varyings.add("vpos","vec3");a.vertex.uniforms.add("viewNormal","mat4");b.receiveShadows&&a.varyings.add("linearDepth","float");b.tileBorders&&a.varyings.add("vuv","vec2");b.atmosphere&&(a.vertex.uniforms.add("lightingMainDirection","vec3"),a.varyings.add("wnormal","vec3"),a.varyings.add("wlight","vec3"));b.screenSizePerspective&&(a.vertex.uniforms.add("screenSizePerspective",
"vec4"),a.varyings.add("screenSizeDistanceToCamera","float"),a.varyings.add("screenSizeCosAngle","float"));a.vertex.code.add(c.glsl`
      void main(void) {
        vpos = position;
        vnormal = getLocalUp(vpos, origin);

        vec2 uv = uv0;
        vpos = applySkirts(uv, vpos, vnormal, skirtScale);
        ${b.atmosphere?c.glsl`
        wnormal = (viewNormal * vec4(normalize(vpos+origin), 1.0)).xyz;
        wlight = (view  * vec4(-lightingMainDirection, 1.0)).xyz;`:""}
        ${b.tileBorders?c.glsl`vuv = uv;`:""}
        ${b.screenSizePerspective?c.glsl`
        vec3 viewPos = (view * vec4(vpos, 1.0)).xyz;
        screenSizeDistanceToCamera = length(viewPos);
        vec3 viewSpaceNormal = (viewNormal * vec4(normalize(vpos + origin), 1.0)).xyz;
        screenSizeCosAngle = abs(viewSpaceNormal.z);`:""}
        gl_Position = transformPosition(proj, view, vpos);
        ${b.receiveShadows?c.glsl`linearDepth = gl_Position.w;`:""}
        forwardTextureCoordinates(uv);
        ${g?c.glsl`setOverlayVTC(uv);`:""}
        ${f?c.glsl`forwardVertexTangent(vnormal);`:c.glsl``}
      }
    `);a.extensions.add("GL_OES_standard_derivatives");a.extensions.add("GL_EXT_shader_texture_lod");a.include(n.Slice,b);a.include(r.EvaluateSceneLighting,b);a.fragment.uniforms.add("camPos","vec3").add("viewDirection","vec3").add("ssaoTex","sampler2D").add("viewportPixelSz","vec4").add("opacity","float");b.screenSizePerspective&&a.fragment.uniforms.add("screenSizePerspective","vec4");f&&(a.fragment.uniforms.add("ovInnerWaterTex","sampler2D"),a.fragment.uniforms.add("ovOuterWaterTex","sampler2D"),
a.fragment.uniforms.add("view","mat4"));a.fragment.code.add(c.glsl`
      const vec3 ambient = vec3(0.2, 0.2, 0.2);
      const vec3 diffuse = vec3(0.8, 0.8, 0.8);
      const float diffuseHardness = 2.5;
      const float sliceOpacity = 0.2;

      float lum(vec3 c) {
        float max = max(max(c.r, c.g), c.b);
        float min = min(min(c.r, c.g), c.b);
        return (min + max) * 0.5;
      }
      `);b.atmosphere&&a.fragment.code.add(c.glsl`
      vec3 atmosphere(vec3 lightPos, vec3 normal, vec3 view) {
        vec3 surfaceColor   = vec3(0.0);
        vec3 fuzzySpecColor = vec3(1.0);
        vec3 subColor       = vec3(0.0);
        float rollOff       = 1.0;

        vec3 Ln = normalize(lightPos);
        vec3 Nn = normalize(normal);
        vec3 Hn = normalize(view + Ln);

        float ldn = dot(Ln, Nn);
        float diffComp = max(0.0, ldn);
        // clamp necessary here because values might cause flickering: #21549
        float vdn = clamp(1.0 - dot(view, Nn), 0.0, 1.0);
        float ndv = dot(view, Ln);

        vec3 diffContrib = surfaceColor * diffComp;
        float subLamb = max(0.0, smoothstep(-rollOff, 1.0, ldn) - smoothstep(0.0, 1.0, ldn));

        vec3 subContrib = subLamb * subColor;
        vec3 vecColor = vec3(vdn);

        vec3 diffuseContrib = (subContrib + diffContrib);
        vec3 specularContrib = (vecColor * fuzzySpecColor);

        return (diffContrib + specularContrib) * rollOff;
      }
      `);a.fragment.code.add(c.glsl`
      void main() {
        ${b.receiveShadows?c.glsl`float shadow = readShadowMap(vpos, linearDepth);`:c.glsl`float shadow = 0.0;`}
        float vndl = dot(normalize(vnormal), -lightingMainDirection);
        float k = smoothstep(0.0, 1.0, clamp(vndl*diffuseHardness, 0.0, 1.0));
        vec3 d = (1.0 - shadow/1.8) * diffuse * k;

        float ssao = viewportPixelSz.w < .0 ? 1.0 : texture2D(ssaoTex, (gl_FragCoord.xy - viewportPixelSz.xy) * viewportPixelSz.zw).a;
        vec4 tileColor = getTileColor() * opacity;
        ${g?c.glsl`
            vec4 overlayColorOpaque = getOverlayColor(ovInnerColorTex, ovOuterColorTex, vtcOverlay);
            vec4 overlayColor = overlayOpacity * overlayColorOpaque;
            vec4 groundColor = tileColor;
            tileColor = tileColor * (1.0 - overlayColor.a) + overlayColor;`:""}
        if (rejectBySlice(vpos)) {
          tileColor *= sliceOpacity;
        }
        vec3 atm = vec3(0.0);
        ${b.atmosphere?c.glsl`
            float ndotl = max(0.0, min(1.0, vndl));
            atm = atmosphere(wlight, wnormal, -viewDirection);
            atm *= max(0.0, min(1.0, (1.0-lum(tileColor.rgb)*1.5))); //avoid atmosphere on bright base maps
            atm *= max(0.0, min(1.0, ndotl*2.0)); // avoid atmosphere on dark side of the globe
            atm *= tileColor.a; // premultiply with tile alpha`:""}
        vec3 albedo = atm + tileColor.rgb;
        vec3 normal = normalize(vnormal);

        // heuristic shading function used in the old terrain, now used to add ambient lighting
        float additionalAmbientScale = smoothstep(0.0, 1.0, clamp(vndl*2.5, 0.0, 1.0));
        vec3 additionalLight = ssao * lightingMainIntensity * additionalAmbientScale * ambientBoostFactor * lightingGlobalFactor;

        gl_FragColor = vec4(evaluateSceneLightingExt(normal, albedo, shadow, 1.0 - ssao, additionalLight), tileColor.a);
        ${f?c.glsl`
            vec4 overlayWaterMask = getOverlayColor(ovInnerWaterTex, ovOuterWaterTex, vtcOverlay);
            float waterNormalLength = length(overlayWaterMask);
            if (waterNormalLength > 0.95) {
              mat3 tbnMatrix = mat3(tbnTangent, tbnBiTangent, vnormal);
              vec4 waterOverlayColor = vec4(overlayColor.w > 0.0 ? overlayColorOpaque.xyz/overlayColor.w : vec3(1.0), overlayColor.w);
              vec4 viewPosition = view*vec4(vpos, 1.0);
              vec4 waterColorLinear = getOverlayWaterColor(overlayWaterMask, waterOverlayColor, -normalize(vpos - camPos), shadow, vnormal, tbnMatrix, viewPosition.xyz);
              vec4 waterColorNonLinear = delinearizeGamma(vec4(waterColorLinear.xyz, 1.0));
              // un-gamma the ground color to mix in linear space
              gl_FragColor = mix(groundColor, waterColorNonLinear, waterColorLinear.w);
            }`:""}
        ${b.screenSizePerspective?c.glsl`
          float perspectiveScale = screenSizePerspectiveScaleFloat(1.0, screenSizeCosAngle, screenSizeDistanceToCamera, screenSizePerspective);
          if (perspectiveScale <= 0.25) {
            gl_FragColor = mix(gl_FragColor, vec4(1.0, 0.0, 0.0, 1.0), perspectiveScale * 4.0);
          }
          else if (perspectiveScale <= 0.5) {
            gl_FragColor = mix(gl_FragColor, vec4(0.0, 0.0, 1.0, 1.0), (perspectiveScale - 0.25) * 4.0);
          }
          else if (perspectiveScale >= 0.99) {
            gl_FragColor = mix(gl_FragColor, vec4(0.0, 1.0, 0.0, 1.0), 0.2);
          }
          else {
            gl_FragColor = mix(gl_FragColor, vec4(1.0, 0.0, 1.0, 1.0), (perspectiveScale - 0.5) * 2.0);
          }`:""}
        ${b.tileBorders?c.glsl`
            vec2 dVuv = fwidth(vuv);
            vec2 edgeFactors = smoothstep(vec2(0.0), 1.5 * dVuv, min(vuv, 1.0 - vuv));
            float edgeFactor = 1.0 - min(edgeFactors.x, edgeFactors.y);
            gl_FragColor = mix(gl_FragColor, vec4(1.0, 0.0, 0.0, 1.0), edgeFactor);`:""}
        gl_FragColor = highlightSlice(gl_FragColor, vpos);
      }
    `)}if(1===b.output||3===b.output)a.include(d.Transform,{linearDepth:!0}),a.include(q.OutputDepth,{output:b.output}),a.include(e.NormalUtils,b),a.varyings.add("linearDepth","float"),a.vertex.uniforms.add("nearFar","vec2"),a.vertex.code.add(c.glsl`
        void main(void) {
          vec3 normal = getLocalUp(position, origin);
          vec2 uv = uv0;
          vec3 vpos = applySkirts(uv, position, normal.xyz, skirtScale);

          gl_Position = transformPositionWithDepth(proj, view, vpos, nearFar, linearDepth);
        }
    `),a.fragment.code.add(c.glsl`
        void main() {
          outputDepth(linearDepth);
        }
    `);2===b.output&&(a.include(d.Transform,{linearDepth:!1}),a.include(e.NormalUtils,b),a.varyings.add("vnormal","vec3"),a.varyings.add("vpos","vec3"),a.vertex.uniforms.add("viewNormal","mat4"),a.vertex.code.add(c.glsl`
        void main(void) {
          vec3 normal = getLocalUp(position, origin);
          vec2 uv = uv0;
          vpos = applySkirts(uv, position, normal, skirtScale);

          gl_Position = transformPosition(proj, view, vpos);
          vnormal = normalize((viewNormal * vec4(normal, 1.0)).xyz);
        }
    `),a.fragment.code.add(c.glsl`
        void main() {
          vec3 normal = normalize(vnormal);
          if (gl_FrontFacing == false) {
            normal = -normal;
          }
          gl_FragColor = vec4(vec3(0.5) + 0.5 * normal, 0.0);
        }
    `));4===b.output&&(a.include(d.Transform,{linearDepth:!1}),a.include(e.NormalUtils,b),a.include(k.Overlay,{pbrMode:0}),a.vertex.code.add(c.glsl`
          void main() {
            vec3 vnormal = getLocalUp(position, origin);
            vec2 uv = uv0;
            vec3 vpos = applySkirts(uv, position, vnormal, skirtScale);
            setOverlayVTC(uv);

            gl_Position = transformPosition(proj, view, vpos);
          }
      `),a.include(p.OutputHighlight),a.fragment.code.add(c.glsl`
        void main() {
          vec4 overlayColor = getCombinedOverlayColor();

          if (overlayColor.a == 0.0) {
            gl_FragColor = vec4(0.0);
            return;
          }

          outputHighlight();
        }
      `));return a}var x=Object.freeze({__proto__:null,build:l});h.TerrainShader=x;h.build=l});