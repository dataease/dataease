// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../shading/Water.glsl","../shading/EvaluateMainLighting.glsl"],function(d,c,e,f){d.Overlay=function(a,b){3!==b.pbrMode&&4!==b.pbrMode||a.include(e.Water,b);a.vertex.uniforms.add("overlayTexOffset","vec4");a.vertex.uniforms.add("overlayTexScale","vec4");a.varyings.add("vtcOverlay","vec4");a.vertex.code.add(c.glsl`
    void setOverlayVTC(in vec2 uv) {
      vtcOverlay = vec4(uv, uv) * overlayTexScale + overlayTexOffset;
    }
  `);a.fragment.uniforms.add("ovInnerColorTex","sampler2D");a.fragment.uniforms.add("ovOuterColorTex","sampler2D");a.fragment.uniforms.add("overlayOpacity","float");a.fragment.code.add(c.glsl`
    vec4 getOverlayColor(sampler2D ov0Tex, sampler2D ov1Tex, vec4 texCoords) {
      // read textures outside of conditions, to avoid artifacts likely related to non-uniform flow control:
      // - https://www.khronos.org/opengl/wiki/Sampler_(GLSL)#Non-uniform_flow_control
      // - https://devtopia.esri.com/WebGIS/arcgis-js-api/issues/13657
      vec4 color0 = texture2D(ov0Tex, texCoords.xy);
      vec4 color1 = texture2D(ov1Tex, texCoords.zw);

      float valid0 = float((texCoords.x >= 0.0) && (texCoords.x <= 1.0) && (texCoords.y >= 0.0) && (texCoords.y <= 1.0));
      float valid1 = float((texCoords.z >= 0.0) && (texCoords.z <= 1.0) && (texCoords.w >= 0.0) && (texCoords.w <= 1.0));

      // Pick color0 if valid, otherwise color1 if valid, otherwise vec4(0)
      return mix(color1 * valid1, color0, valid0);
    }
  `);a.fragment.code.add(c.glsl`
    vec4 getCombinedOverlayColor() {
      return overlayOpacity * getOverlayColor(ovInnerColorTex, ovOuterColorTex, vtcOverlay);
    }
  `);if(3===b.pbrMode||4===b.pbrMode)a.include(f.EvaluateMainLighting),a.fragment.code.add(c.glsl`
    vec4 getOverlayWaterColor(vec4 maskInput, vec4 colorInput, vec3 vposEyeDir,
       float shadow, vec3 localUp, mat3 tbn, vec3 position) {

      // reproject normal from 0...1 => -1...1
      // and project it to worldspace.
      vec3 n = normalize(tbn *  (2.0 * maskInput.rgb - vec3(1.0)));
      vec3 v = vposEyeDir;
      vec3 l = normalize(-lightingMainDirection);
      vec3 final = getSeaColor(n, v, l, colorInput.rgb, lightingMainIntensity, localUp, 1.0 - shadow, maskInput.w, position);

      // the terrain renderer assumes a premultiplied color output without gamma.
      return vec4(final, colorInput.w);
    }
    `)};Object.defineProperty(d,"__esModule",{value:!0})});