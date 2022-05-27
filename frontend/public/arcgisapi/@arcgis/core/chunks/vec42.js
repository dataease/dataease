/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import{h as t}from"./object.js";import{a as o,d as r}from"../core/lang.js";import{i as a,b as i,L as n}from"./Logger.js";import{throwIfAborted as s,throwIfAbortError as l,create as c,all as d}from"../core/promiseUtils.js";import u from"../core/Error.js";import{n as m}from"./compilerUtils.js";import{r as p}from"./asyncUtils.js";import{isDataProtocol as v,dataToArrayBuffer as f,dataComponents as h,makeAbsolute as g}from"../core/urlUtils.js";import x from"../request.js";import"./mathUtils2.js";import{f as b,c as y}from"./vec3f64.js";import{f as C,g as w,n as T,c as M,l as S,s as O,d as A,t as _}from"./vec3.js";import{V as P}from"./Version.js";import{k as R,m as L,t as F,r as I,s as E}from"./mat4.js";import{a as B,c as D,d as N,b as U}from"./quatf64.js";import{i as z,j as V,g as G,a as j,b as q,e as H,h as k,k as $,l as W,m as X,n as Q,o as J,p as Y,q as K,r as Z}from"./BufferView.js";import{l as ee}from"./vec32.js";import{g as te}from"./quat.js";import{g as oe,T as re,a1 as ae,O as ie,e as ne,W as se,f as le,a0 as ce,C as de,S as ue,d as me,s as pe,p as ve,a as fe,b as he,D as ge,V as xe,y as be,u as ye,h as Ce,i as we,j as Te,l as Me,m as Se,n as Oe,o as Ae,R as _e,M as Pe,q as Re,a7 as Le,r as Fe,Y as Ie,t as Ee,F as Be,Q as De}from"./PiUtils.glsl.js";import{P as Ne}from"./Program.js";import{F as Ue,m as ze,a as Ve,d as Ge}from"./isWebGL2Context.js";import{n as je,i as qe,m as He,a as ke,e as $e}from"./InterleavedLayout.js";import{f as We}from"./vec3f32.js";import{u as Xe}from"./Util.js";import{B as Qe,V as Je}from"./VertexArrayObject.js";import{b as Ye,d as Ke}from"./Object3D.js";import{d as Ze}from"./intersectorUtils.js";import{G as et}from"./GLMaterialTexture.js";import{V as tt}from"./VerticalOffset.glsl.js";import"./RenderingContext.js";import{P as ot,R as rt,F as at}from"./PhysicallyBasedRendering.glsl.js";class it{constructor(e){this.context=e,this._doublePrecisionRequiresObfuscation=null}get doublePrecisionRequiresObfuscation(){if(i(this._doublePrecisionRequiresObfuscation)){const e=lt(this.context,!1),t=lt(this.context,!0);this._doublePrecisionRequiresObfuscation=0!==e&&(0===t||e/t>5)}return this._doublePrecisionRequiresObfuscation}}let nt=null;function st(e){a(nt)&&nt.context===e&&(nt=null)}function lt(e,t){const o=new Ue(e,{colorTarget:0,depthStencilTarget:0},{target:3553,wrapMode:33071,pixelFormat:6408,dataType:5121,samplingMode:9728,width:1,height:1});const r=Qe.createVertex(e,35044,new Uint16Array([0,0,1,0,0,1,1,1])),a=new Je(e,{a_pos:0},{geometry:[{name:"a_pos",count:2,type:5123,offset:0,stride:4,normalized:!1}]},{geometry:r}),i=b(5633261.287538229,2626832.878767164,1434988.0495278358),n=b(5633271.46742708,2626873.6381334523,1434963.231608387),s=function(o,r){const a=new Ne(e,`\n\n  precision highp float;\n\n  attribute vec2 a_pos;\n\n  uniform vec3 u_highA;\n  uniform vec3 u_lowA;\n  uniform vec3 u_highB;\n  uniform vec3 u_lowB;\n\n  varying vec4 v_color;\n\n  ${t?"#define DOUBLE_PRECISION_REQUIRES_OBFUSCATION":""}\n\n  #ifdef DOUBLE_PRECISION_REQUIRES_OBFUSCATION\n\n  vec3 dpPlusFrc(vec3 a, vec3 b) {\n    return mix(a, a + b, vec3(notEqual(b, vec3(0))));\n  }\n\n  vec3 dpMinusFrc(vec3 a, vec3 b) {\n    return mix(vec3(0), a - b, vec3(notEqual(a, b)));\n  }\n\n  vec3 dpAdd(vec3 hiA, vec3 loA, vec3 hiB, vec3 loB) {\n    vec3 t1 = dpPlusFrc(hiA, hiB);\n    vec3 e = dpMinusFrc(t1, hiA);\n    vec3 t2 = dpMinusFrc(hiB, e) + dpMinusFrc(hiA, dpMinusFrc(t1, e)) + loA + loB;\n    return t1 + t2;\n  }\n\n  #else\n\n  vec3 dpAdd(vec3 hiA, vec3 loA, vec3 hiB, vec3 loB) {\n    vec3 t1 = hiA + hiB;\n    vec3 e = t1 - hiA;\n    vec3 t2 = ((hiB - e) + (hiA - (t1 - e))) + loA + loB;\n    return t1 + t2;\n  }\n\n  #endif\n\n  const float MAX_RGBA_FLOAT =\n    255.0 / 256.0 +\n    255.0 / 256.0 / 256.0 +\n    255.0 / 256.0 / 256.0 / 256.0 +\n    255.0 / 256.0 / 256.0 / 256.0 / 256.0;\n\n  const vec4 FIXED_POINT_FACTORS = vec4(1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0);\n\n  vec4 float2rgba(const float value) {\n    // Make sure value is in the domain we can represent\n    float valueInValidDomain = clamp(value, 0.0, MAX_RGBA_FLOAT);\n\n    // Decompose value in 32bit fixed point parts represented as\n    // uint8 rgba components. Decomposition uses the fractional part after multiplying\n    // by a power of 256 (this removes the bits that are represented in the previous\n    // component) and then converts the fractional part to 8bits.\n    vec4 fixedPointU8 = floor(fract(valueInValidDomain * FIXED_POINT_FACTORS) * 256.0);\n\n    // Convert uint8 values (from 0 to 255) to floating point representation for\n    // the shader\n    const float toU8AsFloat = 1.0 / 255.0;\n\n    return fixedPointU8 * toU8AsFloat;\n  }\n\n  void main() {\n    vec3 val = dpAdd(u_highA, u_lowA, -u_highB, -u_lowB);\n\n    v_color = float2rgba(val.z / 25.0);\n\n    gl_Position = vec4(a_pos * 2.0 - 1.0, 0.0, 1.0);\n  }\n  `,"\n  precision highp float;\n\n  varying vec4 v_color;\n\n  void main() {\n    gl_FragColor = v_color;\n  }\n  ",{a_pos:0}),i=new Float32Array(6);Ye(o,i,3);const n=new Float32Array(6);return Ye(r,n,3),e.bindProgram(a),a.setUniform3f("u_highA",i[0],i[2],i[4]),a.setUniform3f("u_lowA",i[1],i[3],i[5]),a.setUniform3f("u_highB",n[0],n[2],n[4]),a.setUniform3f("u_lowB",n[1],n[3],n[5]),a}(i,n),l=e.getBoundFramebufferObject(),{x:c,y:d,width:u,height:m}=e.getViewport();e.bindFramebuffer(o),e.setViewport(0,0,1,1),e.bindVAO(a),e.drawArrays(5,0,4);const p=new Uint8Array(4);o.readPixels(0,0,1,1,6408,5121,p),s.dispose(),a.dispose(!1),r.dispose(),o.dispose(),e.setViewport(c,d,u,m),e.bindFramebuffer(l);const v=(i[2]-n[2])/25,f=Xe(p);return Math.abs(v-f)}function ct({code:e},t){t.doublePrecisionRequiresObfuscation?e.add(oe`
      vec3 dpPlusFrc(vec3 a, vec3 b) {
        return mix(a, a + b, vec3(notEqual(b, vec3(0))));
      }

      vec3 dpMinusFrc(vec3 a, vec3 b) {
        return mix(vec3(0), a - b, vec3(notEqual(a, b)));
      }

      // based on https://www.thasler.com/blog/blog/glsl-part2-emu
      vec3 dpAdd(vec3 hiA, vec3 loA, vec3 hiB, vec3 loB) {
        vec3 t1 = dpPlusFrc(hiA, hiB);
        vec3 e = dpMinusFrc(t1, hiA);
        vec3 t2 = dpMinusFrc(hiB, e) + dpMinusFrc(hiA, dpMinusFrc(t1, e)) + loA + loB;
        return t1 + t2;
      }
    `):e.add(oe`
      vec3 dpAdd(vec3 hiA, vec3 loA, vec3 hiB, vec3 loB) {
        vec3 t1 = hiA + hiB;
        vec3 e = t1 - hiA;
        vec3 t2 = ((hiB - e) + (hiA - (t1 - e))) + loA + loB;
        return t1 + t2;
      }
    `)}function dt(e){return!!t("force-double-precision-obfuscation")||function(e){return(i(nt)||nt.context!==e)&&(nt=new it(e)),nt}(e).doublePrecisionRequiresObfuscation}function ut(e,t){t.instanced&&t.instancedDoublePrecision&&(e.attributes.add("modelOriginHi","vec3"),e.attributes.add("modelOriginLo","vec3"),e.attributes.add("model","mat3"),e.attributes.add("modelNormal","mat3")),t.instancedDoublePrecision&&(e.vertex.include(ct,t),e.vertex.uniforms.add("viewOriginHi","vec3"),e.vertex.uniforms.add("viewOriginLo","vec3"));const o=[oe`
    vec3 calculateVPos() {
      ${t.instancedDoublePrecision?"return model * localPosition().xyz;":"return localPosition().xyz;"}
    }
    `,oe`
    vec3 subtractOrigin(vec3 _pos) {
      ${t.instancedDoublePrecision?oe`
          vec3 originDelta = dpAdd(viewOriginHi, viewOriginLo, -modelOriginHi, -modelOriginLo);
          return _pos - originDelta;`:"return vpos;"}
    }
    `,oe`
    vec3 dpNormal(vec4 _normal) {
      ${t.instancedDoublePrecision?"return normalize(modelNormal * _normal.xyz);":"return normalize(_normal.xyz);"}
    }
    `,oe`
    vec3 dpNormalView(vec4 _normal) {
      ${t.instancedDoublePrecision?"return normalize((viewNormal * vec4(modelNormal * _normal.xyz, 1.0)).xyz);":"return normalize((viewNormal * _normal).xyz);"}
    }
    `,t.vertexTangets?oe`
    vec4 dpTransformVertexTangent(vec4 _tangent) {
      ${t.instancedDoublePrecision?"return vec4(modelNormal * _tangent.xyz, _tangent.w);":"return _tangent;"}

    }
    `:oe``];e.vertex.code.add(o[0]),e.vertex.code.add(o[1]),e.vertex.code.add(o[2]),2===t.output&&e.vertex.code.add(o[3]),e.vertex.code.add(o[4])}!function(e){e.Uniforms=class{},e.bindCustomOrigin=function(e,t){Ke(t,mt,pt,3),e.setUniform3fv("viewOriginHi",mt),e.setUniform3fv("viewOriginLo",pt)}}(ut||(ut={}));const mt=y(),pt=y();function vt(e,t){1===t.attributeTextureCoordinates&&(e.attributes.add("uv0","vec2"),e.varyings.add("vuv0","vec2"),e.vertex.code.add(oe`
      void forwardTextureCoordinates() {
        vuv0 = uv0;
      }
    `)),2===t.attributeTextureCoordinates&&(e.attributes.add("uv0","vec2"),e.varyings.add("vuv0","vec2"),e.attributes.add("uvRegion","vec4"),e.varyings.add("vuvRegion","vec4"),e.vertex.code.add(oe`
      void forwardTextureCoordinates() {
        vuv0 = uv0;
        vuvRegion = uvRegion;
      }
    `)),0===t.attributeTextureCoordinates&&e.vertex.code.add(oe`
      void forwardTextureCoordinates() {}
    `)}function ft(e){e.extensions.add("GL_EXT_shader_texture_lod"),e.extensions.add("GL_OES_standard_derivatives"),e.fragment.code.add(oe`
    #ifndef GL_EXT_shader_texture_lod
      float calcMipMapLevel(const vec2 ddx, const vec2 ddy) {
        float deltaMaxSqr = max(dot(ddx, ddx), dot(ddy, ddy));
        return max(0.0, 0.5 * log2(deltaMaxSqr));
      }
    #endif

    vec4 textureAtlasLookup(sampler2D texture, vec2 textureSize, vec2 textureCoordinates, vec4 atlasRegion) {
      //[umin, vmin, umax, vmax]
      vec2 atlasScale = atlasRegion.zw - atlasRegion.xy;
      vec2 uvAtlas = fract(textureCoordinates) * atlasScale + atlasRegion.xy;

      // calculate derivative of continuous texture coordinate
      // to avoid mipmapping artifacts caused by manual wrapping in shader
      vec2 dUVdx = dFdx(textureCoordinates) * atlasScale;
      vec2 dUVdy = dFdy(textureCoordinates) * atlasScale;

      #ifdef GL_EXT_shader_texture_lod
        return texture2DGradEXT(texture, uvAtlas, dUVdx, dUVdy);
      #else
        // use bias to compensate for difference in automatic vs desired mipmap level
        vec2 dUVdxAuto = dFdx(uvAtlas);
        vec2 dUVdyAuto = dFdy(uvAtlas);
        float mipMapLevel = calcMipMapLevel(dUVdx * textureSize, dUVdy * textureSize);
        float autoMipMapLevel = calcMipMapLevel(dUVdxAuto * textureSize, dUVdyAuto * textureSize);

        return texture2D(texture, uvAtlas, mipMapLevel - autoMipMapLevel);
      #endif
    }
  `)}function ht(e,t){e.include(vt,t),e.fragment.code.add(oe`
  struct TextureLookupParameter {
    vec2 uv;
    ${t.supportsTextureAtlas?"vec2 size;":""}
  } vtc;
  `),1===t.attributeTextureCoordinates&&e.fragment.code.add(oe`
      vec4 textureLookup(sampler2D tex, TextureLookupParameter params) {
        return texture2D(tex, params.uv);
      }
    `),2===t.attributeTextureCoordinates&&(e.include(ft),e.fragment.code.add(oe`
    vec4 textureLookup(sampler2D tex, TextureLookupParameter params) {
        return textureAtlasLookup(tex, params.size, params.uv, vuvRegion);
      }
    `))}const gt=We(0,.6,.2);function xt(e,t){const o=e.fragment,r=t.hasMetalnessAndRoughnessTexture||t.hasEmissionTexture||t.hasOcclusionTexture;1===t.pbrMode&&r&&e.include(ht,t),2!==t.pbrMode?(0===t.pbrMode&&o.code.add(oe`
      float getBakedOcclusion() { return 1.0; }
  `),1===t.pbrMode&&(o.uniforms.add("emissionFactor","vec3"),o.uniforms.add("mrrFactors","vec3"),o.code.add(oe`
      vec3 mrr;
      vec3 emission;
      float occlusion;
    `),t.hasMetalnessAndRoughnessTexture&&(o.uniforms.add("texMetallicRoughness","sampler2D"),t.supportsTextureAtlas&&o.uniforms.add("texMetallicRoughnessSize","vec2"),o.code.add(oe`
      void applyMetallnessAndRoughness(TextureLookupParameter params) {
        vec3 metallicRoughness = textureLookup(texMetallicRoughness, params).rgb;

        mrr[0] *= metallicRoughness.b;
        mrr[1] *= metallicRoughness.g;
      }`)),t.hasEmissionTexture&&(o.uniforms.add("texEmission","sampler2D"),t.supportsTextureAtlas&&o.uniforms.add("texEmissionSize","vec2"),o.code.add(oe`
      void applyEmission(TextureLookupParameter params) {
        emission *= textureLookup(texEmission, params).rgb;
      }`)),t.hasOcclusionTexture?(o.uniforms.add("texOcclusion","sampler2D"),t.supportsTextureAtlas&&o.uniforms.add("texOcclusionSize","vec2"),o.code.add(oe`
      void applyOcclusion(TextureLookupParameter params) {
        occlusion *= textureLookup(texOcclusion, params).r;
      }

      float getBakedOcclusion() {
        return occlusion;
      }
      `)):o.code.add(oe`
      float getBakedOcclusion() { return 1.0; }
      `),o.code.add(oe`
    void applyPBRFactors() {
      mrr = mrrFactors;
      emission = emissionFactor;
      occlusion = 1.0;
      ${r?"vtc.uv = vuv0;":""}
      ${t.hasMetalnessAndRoughnessTexture?t.supportsTextureAtlas?"vtc.size = texMetallicRoughnessSize; applyMetallnessAndRoughness(vtc);":"applyMetallnessAndRoughness(vtc);":""}
      ${t.hasEmissionTexture?t.supportsTextureAtlas?"vtc.size = texEmissionSize; applyEmission(vtc);":"applyEmission(vtc);":""}
      ${t.hasOcclusionTexture?t.supportsTextureAtlas?"vtc.size = texOcclusionSize; applyOcclusion(vtc);":"applyOcclusion(vtc);":""}
    }
  `))):o.code.add(oe`
      const vec3 mrr = vec3(0.0, 0.6, 0.2);
      const vec3 emission = vec3(0.0);
      float occlusion = 1.0;

      void applyPBRFactors() {}

      float getBakedOcclusion() { return 1.0; }
    `)}function bt(e){e.vertex.code.add(oe`
    vec4 offsetBackfacingClipPosition(vec4 posClip, vec3 posWorld, vec3 normalWorld, vec3 camPosWorld) {
      vec3 camToVert = posWorld - camPosWorld;

      bool isBackface = dot(camToVert, normalWorld) > 0.0;
      if (isBackface) {
        posClip.z += 0.0000003 * posClip.w;
      }
      return posClip;
    }
  `)}function yt(e){const t=oe`
    vec3 decodeNormal(vec2 f) {
      float z = 1.0 - abs(f.x) - abs(f.y);
      return vec3(f + sign(f) * min(z, 0.0), z);
    }
  `;e.fragment.code.add(t),e.vertex.code.add(t)}function Ct(e,t){0===t.normalType&&(e.attributes.add("normal","vec3"),e.vertex.code.add(oe`
      vec3 normalModel() {
        return normal;
      }
    `)),1===t.normalType&&(e.include(yt),e.attributes.add("normalCompressed","vec2"),e.vertex.code.add(oe`
      vec3 normalModel() {
        return decodeNormal(normalCompressed);
      }
    `)),3===t.normalType&&(e.extensions.add("GL_OES_standard_derivatives"),e.fragment.code.add(oe`
      vec3 screenDerivativeNormal(vec3 positionView) {
        return normalize(cross(dFdx(positionView), dFdy(positionView)));
      }
    `))}function wt(e){e.attributes.add("position","vec3"),e.vertex.code.add(oe`
    vec3 positionModel() { return position; }
  `)}function Tt(e){e.vertex.code.add(oe`
    vec4 decodeSymbolColor(vec4 symbolColor, out int colorMixMode) {
      float symbolAlpha = 0.0;

      const float maxTint = 85.0;
      const float maxReplace = 170.0;
      const float scaleAlpha = 3.0;

      if (symbolColor.a > maxReplace) {
        colorMixMode = ${oe.int(1)};
        symbolAlpha = scaleAlpha * (symbolColor.a - maxReplace);
      } else if (symbolColor.a > maxTint) {
        colorMixMode = ${oe.int(3)};
        symbolAlpha = scaleAlpha * (symbolColor.a - maxTint);
      } else if (symbolColor.a > 0.0) {
        colorMixMode = ${oe.int(4)};
        symbolAlpha = scaleAlpha * symbolColor.a;
      } else {
        colorMixMode = ${oe.int(1)};
        symbolAlpha = 0.0;
      }

      return vec4(symbolColor.r, symbolColor.g, symbolColor.b, symbolAlpha);
    }
  `)}function Mt(e,t){t.symbolColor?(e.include(Tt),e.attributes.add("symbolColor","vec4"),e.varyings.add("colorMixMode","mediump float")):e.fragment.uniforms.add("colorMixMode","int"),t.symbolColor?e.vertex.code.add(oe`
    int symbolColorMixMode;

    vec4 getSymbolColor() {
      return decodeSymbolColor(symbolColor, symbolColorMixMode) * 0.003921568627451;
    }

    void forwardColorMixMode() {
      colorMixMode = float(symbolColorMixMode) + 0.5;
    }
  `):e.vertex.code.add(oe`
    vec4 getSymbolColor() { return vec4(1.0); }
    void forwardColorMixMode() {}
    `)}function St(e,t){e.include(wt),e.vertex.include(ct,t),e.varyings.add("vPositionWorldCameraRelative","vec3"),e.varyings.add("vPosition_view","vec3"),e.vertex.uniforms.add("uTransform_WorldFromModel_RS","mat3"),e.vertex.uniforms.add("uTransform_WorldFromModel_TH","vec3"),e.vertex.uniforms.add("uTransform_WorldFromModel_TL","vec3"),e.vertex.uniforms.add("uTransform_WorldFromView_TH","vec3"),e.vertex.uniforms.add("uTransform_WorldFromView_TL","vec3"),e.vertex.uniforms.add("uTransform_ViewFromCameraRelative_RS","mat3"),e.vertex.uniforms.add("uTransform_ProjFromView","mat4"),e.vertex.code.add(oe`
    // compute position in world space orientation, but relative to the camera position
    vec3 positionWorldCameraRelative() {
      vec3 rotatedModelPosition = uTransform_WorldFromModel_RS * positionModel();

      vec3 transform_CameraRelativeFromModel = dpAdd(
        uTransform_WorldFromModel_TL,
        uTransform_WorldFromModel_TH,
        -uTransform_WorldFromView_TL,
        -uTransform_WorldFromView_TH
      );

      return transform_CameraRelativeFromModel + rotatedModelPosition;
    }

    // position in view space, that is relative to the camera position and orientation
    vec3 position_view() {
      return uTransform_ViewFromCameraRelative_RS * positionWorldCameraRelative();
    }

    // compute gl_Position and forward related varyings to fragment shader
    void forwardPosition() {
      vPositionWorldCameraRelative = positionWorldCameraRelative();
      vPosition_view = position_view();
      gl_Position = uTransform_ProjFromView * vec4(vPosition_view, 1.0);
    }

    vec3 positionWorld() {
      return uTransform_WorldFromView_TL + vPositionWorldCameraRelative;
    }
  `),e.fragment.uniforms.add("uTransform_WorldFromView_TL","vec3"),e.fragment.code.add(oe`
    vec3 positionWorld() {
      return uTransform_WorldFromView_TL + vPositionWorldCameraRelative;
    }
  `)}function Ot(e,t){0===t.normalType||1===t.normalType?(e.include(Ct,t),e.varyings.add("vNormalWorld","vec3"),e.varyings.add("vNormalView","vec3"),e.vertex.uniforms.add("uTransformNormal_GlobalFromModel","mat3"),e.vertex.uniforms.add("uTransformNormal_ViewFromGlobal","mat3"),e.vertex.code.add(oe`
      void forwardNormal() {
        vNormalWorld = uTransformNormal_GlobalFromModel * normalModel();
        vNormalView = uTransformNormal_ViewFromGlobal * vNormalWorld;
      }
    `)):2===t.normalType?(e.include(St,t),e.varyings.add("vNormalWorld","vec3"),e.vertex.code.add(oe`
    void forwardNormal() {
      vNormalWorld = ${1===t.viewingMode?oe`normalize(vPositionWorldCameraRelative);`:oe`vec3(0.0, 0.0, 1.0);`}
    }
    `)):e.vertex.code.add(oe`
      void forwardNormal() {}
    `)}function At(e,t){const o=e.vertex.code,r=e.fragment.code;1!==t.output&&3!==t.output||(e.include(re,{linearDepth:!0}),e.include(vt,t),e.include(ae,t),e.include(ie,t),e.include(ne,t),e.vertex.uniforms.add("nearFar","vec2"),e.varyings.add("depth","float"),t.hasColorTexture&&e.fragment.uniforms.add("tex","sampler2D"),o.add(oe`
      void main(void) {
        vpos = calculateVPos();
        vpos = subtractOrigin(vpos);
        vpos = addVerticalOffset(vpos, localOrigin);
        gl_Position = transformPositionWithDepth(proj, view, vpos, nearFar, depth);
        forwardTextureCoordinates();
      }
    `),e.include(se,t),r.add(oe`
      void main(void) {
        discardBySlice(vpos);
        ${t.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        discardOrAdjustAlpha(texColor);`:""}
        outputDepth(depth);
      }
    `)),2===t.output&&(e.include(re,{linearDepth:!1}),e.include(Ct,t),e.include(Ot,t),e.include(vt,t),e.include(ae,t),t.hasColorTexture&&e.fragment.uniforms.add("tex","sampler2D"),e.vertex.uniforms.add("viewNormal","mat4"),e.varyings.add("vPositionView","vec3"),o.add(oe`
      void main(void) {
        vpos = calculateVPos();
        vpos = subtractOrigin(vpos);
        ${0===t.normalType?oe`
        vNormalWorld = dpNormalView(vvLocalNormal(normalModel()));`:""}
        vpos = addVerticalOffset(vpos, localOrigin);
        gl_Position = transformPosition(proj, view, vpos);
        forwardTextureCoordinates();
      }
    `),e.include(ne,t),e.include(se,t),r.add(oe`
      void main() {
        discardBySlice(vpos);
        ${t.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        discardOrAdjustAlpha(texColor);`:""}

        ${3===t.normalType?oe`
            vec3 normal = screenDerivativeNormal(vPositionView);`:oe`
            vec3 normal = normalize(vNormalWorld);
            if (gl_FrontFacing == false) normal = -normal;`}
        gl_FragColor = vec4(vec3(0.5) + 0.5 * normal, 1.0);
      }
    `)),4===t.output&&(e.include(re,{linearDepth:!1}),e.include(vt,t),e.include(ae,t),t.hasColorTexture&&e.fragment.uniforms.add("tex","sampler2D"),o.add(oe`
      void main(void) {
        vpos = calculateVPos();
        vpos = subtractOrigin(vpos);
        vpos = addVerticalOffset(vpos, localOrigin);
        gl_Position = transformPosition(proj, view, vpos);
        forwardTextureCoordinates();
      }
    `),e.include(ne,t),e.include(se,t),e.include(le),r.add(oe`
      void main() {
        discardBySlice(vpos);
        ${t.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        discardOrAdjustAlpha(texColor);`:""}
        outputHighlight();
      }
    `))}function _t(e,t){const o=e.fragment;o.uniforms.add("normalTexture","sampler2D"),o.uniforms.add("normalTextureSize","vec2"),t.vertexTangets?(e.attributes.add("tangent","vec4"),e.varyings.add("vTangent","vec4"),2===t.doubleSidedMode?o.code.add(oe`
      mat3 computeTangentSpace(vec3 normal) {
        float tangentHeadedness = gl_FrontFacing ? vTangent.w : -vTangent.w;
        vec3 tangent = normalize(gl_FrontFacing ? vTangent.xyz : -vTangent.xyz);
        vec3 bitangent = cross(normal, tangent) * tangentHeadedness;
        return mat3(tangent, bitangent, normal);
      }
    `):o.code.add(oe`
      mat3 computeTangentSpace(vec3 normal) {
        float tangentHeadedness = vTangent.w;
        vec3 tangent = normalize(vTangent.xyz);
        vec3 bitangent = cross(normal, tangent) * tangentHeadedness;
        return mat3(tangent, bitangent, normal);
      }
    `)):(e.extensions.add("GL_OES_standard_derivatives"),o.code.add(oe`
    mat3 computeTangentSpace(vec3 normal, vec3 pos, vec2 st) {

      vec3 Q1 = dFdx(pos);
      vec3 Q2 = dFdy(pos);

      vec2 stx = dFdx(st);
      vec2 sty = dFdy(st);

      float det = stx.t * sty.s - sty.t * stx.s;

      vec3 T = stx.t * Q2 - sty.t * Q1; // compute tangent
      T = T - normal * dot(normal, T); // orthogonalize tangent
      T *= inversesqrt(max(dot(T,T), 1.e-10)); // "soft" normalize - goes to 0 when T goes to 0
      vec3 B = sign(det) * cross(normal, T); // assume normal is normalized, B has the same lenght as B
      return mat3(T, B, normal); // T and B go to 0 when the tangent space is not well defined by the uv coordinates
    }
  `)),0!==t.attributeTextureCoordinates&&(e.include(ht,t),o.code.add(oe`
    vec3 computeTextureNormal(mat3 tangentSpace, vec2 uv) {
      vtc.uv = uv;
      ${t.supportsTextureAtlas?"vtc.size = normalTextureSize;":""}
      vec3 rawNormal = textureLookup(normalTexture, vtc).rgb * 2.0 - 1.0;
      return tangentSpace * rawNormal;
    }
  `))}function Pt(e,t){const o=e.fragment;t.receiveAmbientOcclusion?(o.uniforms.add("ssaoTex","sampler2D"),o.uniforms.add("viewportPixelSz","vec4"),o.code.add(oe`
      float evaluateAmbientOcclusion() {
        return 1.0 - texture2D(ssaoTex, (gl_FragCoord.xy - viewportPixelSz.xy) * viewportPixelSz.zw).a;
      }

      float evaluateAmbientOcclusionInverse() {
        float ssao = texture2D(ssaoTex, (gl_FragCoord.xy - viewportPixelSz.xy) * viewportPixelSz.zw).a;
        return viewportPixelSz.z < 0.0 ? 1.0 : ssao;
      }
    `)):o.code.add(oe`
      float evaluateAmbientOcclusion() { return 0.0; } // no occlusion
      float evaluateAmbientOcclusionInverse() { return 1.0; }
    `)}function Rt(e,t){const o=e.fragment,r=void 0!==t.lightingSphericalHarmonicsOrder?t.lightingSphericalHarmonicsOrder:2;0===r?(o.uniforms.add("lightingAmbientSH0","vec3"),o.code.add(oe`
      vec3 calculateAmbientIrradiance(vec3 normal, float ambientOcclusion) {
        vec3 ambientLight = 0.282095 * lightingAmbientSH0;
        return ambientLight * (1.0 - ambientOcclusion);
      }
    `)):1===r?(o.uniforms.add("lightingAmbientSH_R","vec4"),o.uniforms.add("lightingAmbientSH_G","vec4"),o.uniforms.add("lightingAmbientSH_B","vec4"),o.code.add(oe`
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
    `)):2===r&&(o.uniforms.add("lightingAmbientSH0","vec3"),o.uniforms.add("lightingAmbientSH_R1","vec4"),o.uniforms.add("lightingAmbientSH_G1","vec4"),o.uniforms.add("lightingAmbientSH_B1","vec4"),o.uniforms.add("lightingAmbientSH_R2","vec4"),o.uniforms.add("lightingAmbientSH_G2","vec4"),o.uniforms.add("lightingAmbientSH_B2","vec4"),o.code.add(oe`
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
    `),1!==t.pbrMode&&2!==t.pbrMode||o.code.add(oe`
        const vec3 skyTransmittance = vec3(0.9, 0.9, 1.0);

        vec3 calculateAmbientRadiance(float ambientOcclusion)
        {
          vec3 ambientLight = 1.2 * (0.282095 * lightingAmbientSH0) - 0.2;
          return ambientLight *= (1.0 - ambientOcclusion) * skyTransmittance;
        }
      `))}function Lt(e){const t=e.fragment;t.uniforms.add("lightingMainDirection","vec3"),t.uniforms.add("lightingMainIntensity","vec3"),t.uniforms.add("lightingFixedFactor","float"),t.code.add(oe`
    vec3 evaluateMainLighting(vec3 normal_global, float shadowing) {
      float dotVal = clamp(-dot(normal_global, lightingMainDirection), 0.0, 1.0);

      // move lighting towards (1.0, 1.0, 1.0) if requested
      dotVal = mix(dotVal, 1.0, lightingFixedFactor);

      return lightingMainIntensity * ((1.0 - shadowing) * dotVal);
    }
  `)}function Ft(e,t){const o=e.fragment;e.include(Lt),e.include(Pt,t),0!==t.pbrMode&&e.include(ot,t),e.include(Rt,t),t.receiveShadows&&e.include(rt,t),o.uniforms.add("lightingGlobalFactor","float"),o.uniforms.add("ambientBoostFactor","float"),e.include(ce),o.code.add(oe`
    const float GAMMA_SRGB = 2.1;
    const float INV_GAMMA_SRGB = 0.4761904;
    ${0===t.pbrMode?"":"const vec3 GROUND_REFLECTANCE = vec3(0.2);"}
  `),t.useOldSceneLightInterface?o.code.add(oe`
    vec3 evaluateSceneLightingExt(vec3 normal, vec3 albedo, float shadow, float ssao, vec3 additionalLight) {
      // evaluate the main light
      #if defined(TREE_RENDERING)
        // Special case for tree rendering:
        // We shift the Lambert lobe to the back, allowing it to reach part of the hemisphere
        // facing away from the light. The idea is to get an effect where light is transmitted
        // through the tree.
        float minDot = -0.5;
        float dotRange = 1.0 - minDot;
        float dotNormalization = 0.66; // guessed & hand tweaked value, for an exact value we could precompute an integral over the sphere

        float dotVal = dotNormalization * (clamp(-dot(normal, lightingMainDirection), 1.0 - dotRange, 1.0) - minDot) * (1.0 / dotRange);
      #else
        float dotVal = clamp(-dot(normal, lightingMainDirection), 0.0, 1.0);
      #endif

      // move lighting towards (1.0, 1.0, 1.0) if requested
      dotVal = mix(dotVal, 1.0, lightingFixedFactor);

      vec3 mainLight = (1.0 - shadow) * lightingMainIntensity * dotVal;
      vec3 ambientLight = calculateAmbientIrradiance(normal, ssao);

      // inverse gamma correction on the albedo color
      vec3 albedoGammaC = pow(albedo, vec3(GAMMA_SRGB));

      // physically correct BRDF normalizes by PI
      vec3 totalLight = mainLight + ambientLight + additionalLight;
      totalLight = min(totalLight, vec3(PI));
      vec3 outColor = vec3((albedoGammaC / PI) * (totalLight));

      // apply gamma correction to the computed color
      outColor = pow(outColor, vec3(INV_GAMMA_SRGB));

      return outColor;
    }
  `):(1===t.viewingMode?o.code.add(oe`
      float _oldHeuristicLighting(vec3 vPosWorld) {
        vec3 shadingNormalWorld = normalize(vPosWorld);
        float vndl = -dot(shadingNormalWorld, lightingMainDirection);

        return smoothstep(0.0, 1.0, clamp(vndl * 2.5, 0.0, 1.0));
      }
    `):o.code.add(oe`
      float _oldHeuristicLighting(vec3 vPosWorld) {
        float vndl = -dot(vec3(0.0, 0.0, 1.0), lightingMainDirection);
        return smoothstep(0.0, 1.0, clamp(vndl * 2.5, 0.0, 1.0));
      }
    `),o.code.add(oe`
      vec3 evaluateAdditionalLighting(float ambientOcclusion, vec3 vPosWorld) {
        float additionalAmbientScale = _oldHeuristicLighting(vPosWorld);
        return (1.0 - ambientOcclusion) * additionalAmbientScale * ambientBoostFactor * lightingGlobalFactor * lightingMainIntensity;
      }
    `),0===t.pbrMode||4===t.pbrMode?o.code.add(oe`
      vec3 evaluateSceneLighting(vec3 normalWorld, vec3 baseColor, float mainLightShadow, float ambientOcclusion, vec3 additionalLight)
      {
        vec3 mainLighting = evaluateMainLighting(normalWorld, mainLightShadow);
        vec3 ambientLighting = calculateAmbientIrradiance(normalWorld, ambientOcclusion);
        // inverse gamma correction on the base color
        vec3 baseColorLinear = pow(baseColor, vec3(GAMMA_SRGB));

        // physically correct BRDF normalizes by PI
        vec3 totalLight = mainLighting + ambientLighting + additionalLight;
        totalLight = min(totalLight, vec3(PI));
        vec3 outColor = vec3((baseColorLinear / PI) * totalLight);

        // apply gamma correction to the computed color
        outColor = pow(outColor, vec3(INV_GAMMA_SRGB));

        return outColor;
      }
      `):1!==t.pbrMode&&2!==t.pbrMode||(o.code.add(oe`
      const float fillLightIntensity = 0.25;
      const float horizonLightDiffusion = 0.4;
      const float additionalAmbientIrradianceFactor = 0.02;

      vec3 evaluateSceneLightingPBR(vec3 normal, vec3 albedo, float shadow, float ssao, vec3 additionalLight, vec3 viewDir, vec3 normalGround, vec3 mrr, vec3 _emission, float additionalAmbientIrradiance)
      {
        // Calculate half vector between view and light direction
        vec3 viewDirection = -viewDir;
        vec3 mainLightDirection = -lightingMainDirection;
        vec3 h = normalize(viewDirection + mainLightDirection);

        PBRShadingInfo inputs;
        inputs.NdotL = clamp(dot(normal, mainLightDirection), 0.001, 1.0);
        inputs.NdotV = clamp(abs(dot(normal, viewDirection)), 0.001, 1.0);
        inputs.NdotH = clamp(dot(normal, h), 0.0, 1.0);
        inputs.VdotH = clamp(dot(viewDirection, h), 0.0, 1.0);
        inputs.NdotNG = clamp(dot(normal, normalGround), -1.0, 1.0);
        vec3 reflectedView = normalize(reflect(viewDirection, normal));
        inputs.RdotNG = clamp(dot(reflectedView, normalGround), -1.0, 1.0);

        inputs.albedoLinear = pow(albedo, vec3(GAMMA_SRGB));
        inputs.ssao = ssao;

        inputs.metalness = mrr[0];
        inputs.roughness = clamp(mrr[1] * mrr[1], 0.001, 0.99);
      `),o.code.add(oe`
        inputs.f0 = (0.16 * mrr[2] * mrr[2]) * (1.0 - inputs.metalness) + inputs.albedoLinear * inputs.metalness;
        inputs.f90 = vec3(clamp(dot(inputs.f0, vec3(50.0 * 0.33)), 0.0, 1.0)); // more accurate then using  f90 = 1.0
        inputs.diffuseColor = inputs.albedoLinear * (vec3(1.0) - inputs.f0) * (1.0 - inputs.metalness);
      `),o.code.add(oe`
        vec3 ambientDir = vec3(5.0 * normalGround[1] - normalGround[0] * normalGround[2], - 5.0 * normalGround[0] - normalGround[2] * normalGround[1], normalGround[1] * normalGround[1] + normalGround[0] * normalGround[0]);
        ambientDir = ambientDir != vec3(0.0)? normalize(ambientDir) : normalize(vec3(5.0, -1.0, 0.0));

        inputs.NdotAmbDir = abs(dot(normal, ambientDir));

        // Calculate the irradiance components: sun, fill lights and the sky.
        vec3 mainLightIrradianceComponent  = inputs.NdotL * (1.0 - shadow) * lightingMainIntensity;
        vec3 fillLightsIrradianceComponent = inputs.NdotAmbDir * lightingMainIntensity * fillLightIntensity;
        // calculateAmbientIrradiance for localView and additionalLight for gloabalView
        vec3 ambientLightIrradianceComponent = calculateAmbientIrradiance(normal, ssao) + additionalLight;

        // Assemble the overall irradiance of the sky that illuminates the surface
        inputs.skyIrradianceToSurface    = ambientLightIrradianceComponent + mainLightIrradianceComponent + fillLightsIrradianceComponent ;
        // Assemble the overall irradiance of the ground that illuminates the surface. for this we use the simple model that changes only the sky irradiance by the groundReflectance
        inputs.groundIrradianceToSurface = GROUND_REFLECTANCE * ambientLightIrradianceComponent + mainLightIrradianceComponent + fillLightsIrradianceComponent ;
      `),o.code.add(oe`
        vec3 horizonRingDir = inputs.RdotNG * normalGround - reflectedView;
        vec3 horizonRingH = normalize(viewDirection + horizonRingDir);
        inputs.NdotH_Horizon = dot(normal, horizonRingH);

        vec3 mainLightRadianceComponent  = normalDistribution(inputs.NdotH, inputs.roughness) * lightingMainIntensity * (1.0 - shadow);
        vec3 horizonLightRadianceComponent = normalDistribution(inputs.NdotH_Horizon, min(inputs.roughness + horizonLightDiffusion, 1.0)) * lightingMainIntensity * fillLightIntensity;
        vec3 ambientLightRadianceComponent = calculateAmbientRadiance(ssao) + additionalLight; // calculateAmbientRadiance for localView and additionalLight for gloabalView

        // Assemble the overall radiance of the sky that illuminates the surface
        inputs.skyRadianceToSurface    =  ambientLightRadianceComponent + mainLightRadianceComponent + horizonLightRadianceComponent;
        // Assemble the overall radiance of the ground that illuminates the surface. for this we use the simple model that changes only the sky radince by the groundReflectance
        inputs.groundRadianceToSurface = GROUND_REFLECTANCE * (ambientLightRadianceComponent + horizonLightRadianceComponent) + mainLightRadianceComponent;

        // Calculate average ambient radiance - this is used int the gamut mapping part to deduce the black level that is soft compressed
        inputs.averageAmbientRadiance = ambientLightIrradianceComponent[1] * (1.0 + GROUND_REFLECTANCE[1]);
        `),o.code.add(oe`
        vec3 reflectedColorComponent = evaluateEnvironmentIllumination(inputs);
        vec3 additionalMaterialReflectanceComponent = inputs.albedoLinear * additionalAmbientIrradiance;
        vec3 emissionComponent = pow(_emission, vec3(GAMMA_SRGB));
        vec3 outColorLinear = reflectedColorComponent + additionalMaterialReflectanceComponent + emissionComponent;
        ${2===t.pbrMode?oe`vec3 outColor = pow(max(vec3(0.0), outColorLinear - 0.005 * inputs.averageAmbientRadiance), vec3(INV_GAMMA_SRGB));`:oe`vec3 outColor = pow(blackLevelSoftCompression(outColorLinear, inputs), vec3(INV_GAMMA_SRGB));`}
        return outColor;
      }
    `)))}function It(e,t){const o=e.fragment;o.code.add(oe`
    struct ShadingNormalParameters {
      vec3 normalView;
      vec3 viewDirection;
    } shadingParams;
    `),1===t.doubleSidedMode?o.code.add(oe`
      vec3 shadingNormal(ShadingNormalParameters params) {
        return dot(params.normalView, params.viewDirection) > 0.0 ? normalize(-params.normalView) : normalize(params.normalView);
      }
    `):2===t.doubleSidedMode?o.code.add(oe`
      vec3 shadingNormal(ShadingNormalParameters params) {
        return gl_FrontFacing ? normalize(params.normalView) : normalize(-params.normalView);
      }
    `):o.code.add(oe`
      vec3 shadingNormal(ShadingNormalParameters params) {
        return normalize(params.normalView);
      }
    `)}function Et(e,t){oe`
  /*
  *  ${t.name}
  *  ${0===t.output?"RenderOutput: Color":1===t.output?"RenderOutput: Depth":3===t.output?"RenderOutput: Shadow":2===t.output?"RenderOutput: Normal":4===t.output?"RenderOutput: Highlight":""}
  */
  `}function Bt(e){e.include(de),e.code.add(oe`
    vec3 mixExternalColor(vec3 internalColor, vec3 textureColor, vec3 externalColor, int mode) {
      // workaround for artifacts in OSX using Intel Iris Pro
      // see: https://devtopia.esri.com/WebGIS/arcgis-js-api/issues/10475
      vec3 internalMixed = internalColor * textureColor;
      vec3 allMixed = internalMixed * externalColor;

      if (mode == ${oe.int(1)}) {
        return allMixed;
      }
      else if (mode == ${oe.int(2)}) {
        return internalMixed;
      }
      else if (mode == ${oe.int(3)}) {
        return externalColor;
      }
      else {
        // tint (or something invalid)
        float vIn = rgb2v(internalMixed);
        vec3 hsvTint = rgb2hsv(externalColor);
        vec3 hsvOut = vec3(hsvTint.x, hsvTint.y, vIn * hsvTint.z);
        return hsv2rgb(hsvOut);
      }
    }

    float mixExternalOpacity(float internalOpacity, float textureOpacity, float externalOpacity, int mode) {
      // workaround for artifacts in OSX using Intel Iris Pro
      // see: https://devtopia.esri.com/WebGIS/arcgis-js-api/issues/10475
      float internalMixed = internalOpacity * textureOpacity;
      float allMixed = internalMixed * externalOpacity;

      if (mode == ${oe.int(2)}) {
        return internalMixed;
      }
      else if (mode == ${oe.int(3)}) {
        return externalOpacity;
      }
      else {
        // multiply or tint (or something invalid)
        return allMixed;
      }
    }
  `)}!function(e){e.bindUniforms=function(e,t,o=!1){o||(e.setUniform3fv("mrrFactors",t.mrrFactors),e.setUniform3fv("emissionFactor",t.emissiveFactor))}}(xt||(xt={})),function(e){e.ModelTransform=class{constructor(){this.worldFromModel_RS=B(),this.worldFromModel_TH=y(),this.worldFromModel_TL=y()}};e.ViewProjectionTransform=class{constructor(){this.worldFromView_TH=y(),this.worldFromView_TL=y(),this.viewFromCameraRelative_RS=B(),this.projFromView=D()}},e.bindModelTransform=function(e,t){e.setUniformMatrix3fv("uTransform_WorldFromModel_RS",t.worldFromModel_RS),e.setUniform3fv("uTransform_WorldFromModel_TH",t.worldFromModel_TH),e.setUniform3fv("uTransform_WorldFromModel_TL",t.worldFromModel_TL)},e.bindViewProjTransform=function(e,t){e.setUniform3fv("uTransform_WorldFromView_TH",t.worldFromView_TH),e.setUniform3fv("uTransform_WorldFromView_TL",t.worldFromView_TL),e.setUniformMatrix4fv("uTransform_ProjFromView",t.projFromView),e.setUniformMatrix3fv("uTransform_ViewFromCameraRelative_RS",t.viewFromCameraRelative_RS)}}(St||(St={})),function(e){e.bindUniforms=function(e,t){e.setUniformMatrix4fv("viewNormal",t)}}(Ot||(Ot={}));var Dt=Object.freeze({__proto__:null,build:function(e){const t=new ue,o=t.vertex.code,r=t.fragment.code;return t.include(Et,{name:"Default Material Shader",output:e.output}),t.vertex.uniforms.add("proj","mat4").add("view","mat4").add("camPos","vec3").add("localOrigin","vec3"),t.include(wt),t.varyings.add("vpos","vec3"),t.include(ae,e),t.include(ut,e),t.include(tt,e),0!==e.output&&7!==e.output||(t.include(Ct,e),t.include(re,{linearDepth:!1}),0===e.normalType&&e.offsetBackfaces&&t.include(bt),t.include(_t,e),t.include(Ot,e),e.instancedColor&&t.attributes.add("instanceColor","vec4"),t.varyings.add("localvpos","vec3"),t.include(vt,e),t.include(at,e),t.include(Mt,e),t.include(me,e),t.vertex.uniforms.add("externalColor","vec4"),t.varyings.add("vcolorExt","vec4"),o.add(oe`
      void main(void) {
        forwardNormalizedVertexColor();
        vcolorExt = externalColor;
        ${e.instancedColor?"vcolorExt *= instanceColor;":""}
        vcolorExt *= vvColor();
        vcolorExt *= getSymbolColor();
        forwardColorMixMode();

        if (vcolorExt.a < ${oe.float(pe)}) {
          gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
        }
        else {
          vpos = calculateVPos();
          localvpos = vpos - view[3].xyz;
          vpos = subtractOrigin(vpos);
          ${0===e.normalType?oe`
          vNormalWorld = dpNormal(vvLocalNormal(normalModel()));`:""}
          vpos = addVerticalOffset(vpos, localOrigin);
          ${e.vertexTangets?"vTangent = dpTransformVertexTangent(tangent);":""}
          gl_Position = transformPosition(proj, view, vpos);
          ${0===e.normalType&&e.offsetBackfaces?"gl_Position = offsetBackfacingClipPosition(gl_Position, vpos, vNormalWorld, camPos);":""}
        }
        forwardLinearDepth();
        forwardTextureCoordinates();
      }
    `)),7===e.output&&(t.include(ne,e),t.include(se,e),t.fragment.uniforms.add("camPos","vec3").add("localOrigin","vec3").add("opacity","float").add("layerOpacity","float"),e.hasColorTexture&&t.fragment.uniforms.add("tex","sampler2D"),t.fragment.include(Bt),r.add(oe`
      void main() {
        discardBySlice(vpos);
        ${e.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        ${e.textureAlphaPremultiplied?"texColor.rgb /= texColor.a;":""}
        discardOrAdjustAlpha(texColor);`:oe`vec4 texColor = vec4(1.0);`}
        ${e.attributeColor?oe`
        float opacity_ = layerOpacity * mixExternalOpacity(vColor.a * opacity, texColor.a, vcolorExt.a, int(colorMixMode));`:oe`
        float opacity_ = layerOpacity * mixExternalOpacity(opacity, texColor.a, vcolorExt.a, int(colorMixMode));
        `}
        gl_FragColor = vec4(opacity_);
      }
    `)),0===e.output&&(t.include(ne,e),t.include(Ft,e),t.include(Pt,e),t.include(se,e),e.receiveShadows&&t.include(rt,e),t.fragment.uniforms.add("camPos","vec3").add("localOrigin","vec3").add("ambient","vec3").add("diffuse","vec3").add("opacity","float").add("layerOpacity","float"),e.hasColorTexture&&t.fragment.uniforms.add("tex","sampler2D"),t.include(xt,e),t.include(ot,e),t.fragment.include(Bt),t.include(It,e),r.add(oe`
      void main() {
        discardBySlice(vpos);
        ${e.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        ${e.textureAlphaPremultiplied?"texColor.rgb /= texColor.a;":""}
        discardOrAdjustAlpha(texColor);`:oe`vec4 texColor = vec4(1.0);`}
        shadingParams.viewDirection = normalize(vpos - camPos);
        ${3===e.normalType?oe`
        vec3 normal = screenDerivativeNormal(localvpos);`:oe`
        shadingParams.normalView = vNormalWorld;
        vec3 normal = shadingNormal(shadingParams);`}
        ${1===e.pbrMode?"applyPBRFactors();":""}
        float ssao = evaluateAmbientOcclusionInverse();
        ssao *= getBakedOcclusion();

        float additionalAmbientScale = _oldHeuristicLighting(vpos + localOrigin);
        vec3 additionalLight = ssao * lightingMainIntensity * additionalAmbientScale * ambientBoostFactor * lightingGlobalFactor;
        ${e.receiveShadows?"float shadow = readShadowMap(vpos, linearDepth);":1===e.viewingMode?"float shadow = lightingGlobalFactor * (1.0 - additionalAmbientScale);":"float shadow = 0.0;"}
        vec3 matColor = max(ambient, diffuse);
        ${e.attributeColor?oe`
        vec3 albedo_ = mixExternalColor(vColor.rgb * matColor, texColor.rgb, vcolorExt.rgb, int(colorMixMode));
        float opacity_ = layerOpacity * mixExternalOpacity(vColor.a * opacity, texColor.a, vcolorExt.a, int(colorMixMode));`:oe`
        vec3 albedo_ = mixExternalColor(matColor, texColor.rgb, vcolorExt.rgb, int(colorMixMode));
        float opacity_ = layerOpacity * mixExternalOpacity(opacity, texColor.a, vcolorExt.a, int(colorMixMode));
        `}
        ${e.hasNormalTexture?oe`
              mat3 tangentSpace = ${e.vertexTangets?"computeTangentSpace(normal);":"computeTangentSpace(normal, vpos, vuv0);"}
              vec3 shadedNormal = computeTextureNormal(tangentSpace, vuv0);`:"vec3 shadedNormal = normal;"}
        ${1===e.pbrMode||2===e.pbrMode?1===e.viewingMode?oe`vec3 normalGround = normalize(vpos + localOrigin);`:oe`vec3 normalGround = vec3(0.0, 0.0, 1.0);`:oe``}
        ${1===e.pbrMode||2===e.pbrMode?oe`
            float additionalAmbientIrradiance = additionalAmbientIrradianceFactor * lightingMainIntensity[2];
            vec3 shadedColor = evaluateSceneLightingPBR(shadedNormal, albedo_, shadow, 1.0 - ssao, additionalLight, shadingParams.viewDirection, normalGround, mrr, emission, additionalAmbientIrradiance);`:"vec3 shadedColor = evaluateSceneLighting(shadedNormal, albedo_, shadow, 1.0 - ssao, additionalLight);"}
        gl_FragColor = highlightSlice(vec4(shadedColor, opacity_), vpos);
        ${e.OITEnabled?"gl_FragColor = premultiplyAlpha(gl_FragColor);":""}
      }
    `)),t.include(At,e),t}});class Nt extends he{initializeProgram(e){const t=Nt.shader.get(),o=this.configuration,r=t.build({OITEnabled:0===o.transparencyPassType,output:o.output,viewingMode:e.viewingMode,receiveShadows:o.receiveShadows,slicePlaneEnabled:o.slicePlaneEnabled,sliceHighlightDisabled:o.sliceHighlightDisabled,sliceEnabledForVertexPrograms:!1,symbolColor:o.symbolColors,vvSize:o.vvSize,vvColor:o.vvColor,vvInstancingEnabled:!0,instanced:o.instanced,instancedColor:o.instancedColor,instancedDoublePrecision:o.instancedDoublePrecision,useOldSceneLightInterface:!1,pbrMode:o.usePBR?o.isSchematic?2:1:0,hasMetalnessAndRoughnessTexture:o.hasMetalnessAndRoughnessTexture,hasEmissionTexture:o.hasEmissionTexture,hasOcclusionTexture:o.hasOcclusionTexture,hasNormalTexture:o.hasNormalTexture,hasColorTexture:o.hasColorTexture,receiveAmbientOcclusion:o.receiveAmbientOcclusion,useCustomDTRExponentForWater:!1,normalType:o.normalsTypeDerivate?3:0,doubleSidedMode:o.doubleSidedMode,vertexTangets:o.vertexTangents,attributeTextureCoordinates:o.hasMetalnessAndRoughnessTexture||o.hasEmissionTexture||o.hasOcclusionTexture||o.hasNormalTexture||o.hasColorTexture?1:0,textureAlphaPremultiplied:o.textureAlphaPremultiplied,attributeColor:o.vertexColors,screenSizePerspectiveEnabled:o.screenSizePerspective,verticalOffsetEnabled:o.verticalOffset,offsetBackfaces:o.offsetBackfaces,doublePrecisionRequiresObfuscation:dt(e.rctx),alphaDiscardMode:o.alphaDiscardMode,supportsTextureAtlas:!1});return new Ne(e.rctx,r.generateSource("vertex"),r.generateSource("fragment"),ge)}bindPass(e,t,o){xe.bindProjectionMatrix(this.program,o.camera.projectionMatrix);const r=this.configuration.output;7===r&&(this.program.setUniform1f("opacity",t.opacity),this.program.setUniform1f("layerOpacity",t.layerOpacity),this.program.setUniform4fv("externalColor",t.externalColor),this.program.setUniform1i("colorMixMode",be[t.colorMixMode])),0===r?(o.lighting.setUniforms(this.program,!1),this.program.setUniform3fv("ambient",t.ambient),this.program.setUniform3fv("diffuse",t.diffuse),this.program.setUniform4fv("externalColor",t.externalColor),this.program.setUniform1i("colorMixMode",be[t.colorMixMode]),this.program.setUniform1f("opacity",t.opacity),this.program.setUniform1f("layerOpacity",t.layerOpacity),this.configuration.usePBR&&xt.bindUniforms(this.program,t,this.configuration.isSchematic)):1===r||3===r?this.program.setUniform2fv("nearFar",o.camera.nearFar):4===r&&le.bindOutputHighlight(e,this.program,o),ae.bindUniformsForSymbols(this.program,t),tt.bindUniforms(this.program,t,o),ye(t.screenSizePerspective,this.program,"screenSizePerspectiveAlignment"),2!==t.textureAlphaMode&&3!==t.textureAlphaMode||this.program.setUniform1f("textureAlphaCutoff",t.textureAlphaCutoff)}bindDraw(e){const t=this.configuration.instancedDoublePrecision?b(e.camera.viewInverseTransposeMatrix[3],e.camera.viewInverseTransposeMatrix[7],e.camera.viewInverseTransposeMatrix[11]):e.origin;xe.bindViewCustomOrigin(this.program,t,e.camera.viewMatrix),(0===this.configuration.output||7===this.configuration.output||1===this.configuration.output&&this.configuration.screenSizePerspective||2===this.configuration.output&&this.configuration.screenSizePerspective||4===this.configuration.output&&this.configuration.screenSizePerspective)&&xe.bindCamPosition(this.program,t,e.camera.viewInverseTransposeMatrix),2===this.configuration.output&&this.program.setUniformMatrix4fv("viewNormal",e.camera.viewInverseTransposeMatrix),this.configuration.instancedDoublePrecision&&ut.bindCustomOrigin(this.program,t),ne.bindUniforms(this.program,this.configuration,e.slicePlane,t),0===this.configuration.output&&rt.bindViewCustomOrigin(this.program,e,t)}setPipeline(e,t){const o=this.configuration,r=3===e,a=2===e;return ze({blending:0!==o.output&&7!==o.output||!o.transparent?null:r?Ce:we(e),culling:Ut(o),depthTest:{func:Te(e)},depthWrite:r||a?o.writeDepth&&Ve:null,colorWrite:Ge,stencilWrite:o.sceneHasOcludees?Me:null,stencilTest:o.sceneHasOcludees?t?Se:Oe:null,polygonOffset:r||a?null:Ae(o.enableOffset)})}initializePipeline(){return this._occludeePipelineState=this.setPipeline(this.configuration.transparencyPassType,!0),this.setPipeline(this.configuration.transparencyPassType,!1)}getPipelineState(e){return e?this._occludeePipelineState:this.pipeline}}Nt.shader=new _e(Dt,(()=>Promise.resolve().then((function(){return Dt}))));const Ut=e=>function(e){return e.cullFace?0!==e.cullFace:!e.slicePlaneEnabled&&!e.transparent&&!e.doubleSidedMode}(e)&&{face:1===e.cullFace?1028:1029,mode:2305};class zt extends fe{constructor(){super(...arguments),this.output=0,this.alphaDiscardMode=1,this.doubleSidedMode=0,this.isSchematic=!1,this.vertexColors=!1,this.offsetBackfaces=!1,this.symbolColors=!1,this.vvSize=!1,this.vvColor=!1,this.verticalOffset=!1,this.receiveShadows=!1,this.slicePlaneEnabled=!1,this.sliceHighlightDisabled=!1,this.receiveAmbientOcclusion=!1,this.screenSizePerspective=!1,this.textureAlphaPremultiplied=!1,this.hasColorTexture=!1,this.usePBR=!1,this.hasMetalnessAndRoughnessTexture=!1,this.hasEmissionTexture=!1,this.hasOcclusionTexture=!1,this.hasNormalTexture=!1,this.instanced=!1,this.instancedColor=!1,this.instancedDoublePrecision=!1,this.vertexTangents=!1,this.normalsTypeDerivate=!1,this.writeDepth=!0,this.sceneHasOcludees=!1,this.transparent=!1,this.enableOffset=!0,this.cullFace=0,this.transparencyPassType=3}}e([ve({count:8})],zt.prototype,"output",void 0),e([ve({count:4})],zt.prototype,"alphaDiscardMode",void 0),e([ve({count:3})],zt.prototype,"doubleSidedMode",void 0),e([ve()],zt.prototype,"isSchematic",void 0),e([ve()],zt.prototype,"vertexColors",void 0),e([ve()],zt.prototype,"offsetBackfaces",void 0),e([ve()],zt.prototype,"symbolColors",void 0),e([ve()],zt.prototype,"vvSize",void 0),e([ve()],zt.prototype,"vvColor",void 0),e([ve()],zt.prototype,"verticalOffset",void 0),e([ve()],zt.prototype,"receiveShadows",void 0),e([ve()],zt.prototype,"slicePlaneEnabled",void 0),e([ve()],zt.prototype,"sliceHighlightDisabled",void 0),e([ve()],zt.prototype,"receiveAmbientOcclusion",void 0),e([ve()],zt.prototype,"screenSizePerspective",void 0),e([ve()],zt.prototype,"textureAlphaPremultiplied",void 0),e([ve()],zt.prototype,"hasColorTexture",void 0),e([ve()],zt.prototype,"usePBR",void 0),e([ve()],zt.prototype,"hasMetalnessAndRoughnessTexture",void 0),e([ve()],zt.prototype,"hasEmissionTexture",void 0),e([ve()],zt.prototype,"hasOcclusionTexture",void 0),e([ve()],zt.prototype,"hasNormalTexture",void 0),e([ve()],zt.prototype,"instanced",void 0),e([ve()],zt.prototype,"instancedColor",void 0),e([ve()],zt.prototype,"instancedDoublePrecision",void 0),e([ve()],zt.prototype,"vertexTangents",void 0),e([ve()],zt.prototype,"normalsTypeDerivate",void 0),e([ve()],zt.prototype,"writeDepth",void 0),e([ve()],zt.prototype,"sceneHasOcludees",void 0),e([ve()],zt.prototype,"transparent",void 0),e([ve()],zt.prototype,"enableOffset",void 0),e([ve({count:3})],zt.prototype,"cullFace",void 0),e([ve({count:4})],zt.prototype,"transparencyPassType",void 0);var Vt=Object.freeze({__proto__:null,build:function(e){const t=new ue,o=t.vertex.code,r=t.fragment.code;return t.vertex.uniforms.add("proj","mat4").add("view","mat4").add("camPos","vec3").add("localOrigin","vec3"),t.include(wt),t.varyings.add("vpos","vec3"),t.include(ae,e),t.include(ut,e),t.include(tt,e),0!==e.output&&7!==e.output||(t.include(Ct,e),t.include(re,{linearDepth:!1}),e.offsetBackfaces&&t.include(bt),e.instancedColor&&t.attributes.add("instanceColor","vec4"),t.varyings.add("vNormalWorld","vec3"),t.varyings.add("localvpos","vec3"),t.include(vt,e),t.include(at,e),t.include(Mt,e),t.include(me,e),t.vertex.uniforms.add("externalColor","vec4"),t.varyings.add("vcolorExt","vec4"),o.add(oe`
        void main(void) {
          forwardNormalizedVertexColor();
          vcolorExt = externalColor;
          ${e.instancedColor?"vcolorExt *= instanceColor;":""}
          vcolorExt *= vvColor();
          vcolorExt *= getSymbolColor();
          forwardColorMixMode();

          if (vcolorExt.a < ${oe.float(pe)}) {
            gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
          }
          else {
            vpos = calculateVPos();
            localvpos = vpos - view[3].xyz;
            vpos = subtractOrigin(vpos);
            vNormalWorld = dpNormal(vvLocalNormal(normalModel()));
            vpos = addVerticalOffset(vpos, localOrigin);
            gl_Position = transformPosition(proj, view, vpos);
            ${e.offsetBackfaces?"gl_Position = offsetBackfacingClipPosition(gl_Position, vpos, vNormalWorld, camPos);":""}
          }
          forwardLinearDepth();
          forwardTextureCoordinates();
        }
      `)),7===e.output&&(t.include(ne,e),t.include(se,e),t.fragment.uniforms.add("camPos","vec3").add("localOrigin","vec3").add("opacity","float").add("layerOpacity","float"),t.fragment.uniforms.add("view","mat4"),e.hasColorTexture&&t.fragment.uniforms.add("tex","sampler2D"),t.fragment.include(Bt),r.add(oe`
      void main() {
        discardBySlice(vpos);
        ${e.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        ${e.textureAlphaPremultiplied?"texColor.rgb /= texColor.a;":""}
        discardOrAdjustAlpha(texColor);`:oe`vec4 texColor = vec4(1.0);`}
        ${e.attributeColor?oe`
        float opacity_ = layerOpacity * mixExternalOpacity(vColor.a * opacity, texColor.a, vcolorExt.a, int(colorMixMode));`:oe`
        float opacity_ = layerOpacity * mixExternalOpacity(opacity, texColor.a, vcolorExt.a, int(colorMixMode));
        `}

        gl_FragColor = vec4(opacity_);
      }
    `)),0===e.output&&(t.include(ne,e),t.include(Ft,e),t.include(Pt,e),t.include(se,e),e.receiveShadows&&t.include(rt,e),t.fragment.uniforms.add("camPos","vec3").add("localOrigin","vec3").add("ambient","vec3").add("diffuse","vec3").add("opacity","float").add("layerOpacity","float"),t.fragment.uniforms.add("view","mat4"),e.hasColorTexture&&t.fragment.uniforms.add("tex","sampler2D"),t.include(xt,e),t.include(ot,e),t.fragment.include(Bt),r.add(oe`
      void main() {
        discardBySlice(vpos);
        ${e.hasColorTexture?oe`
        vec4 texColor = texture2D(tex, vuv0);
        ${e.textureAlphaPremultiplied?"texColor.rgb /= texColor.a;":""}
        discardOrAdjustAlpha(texColor);`:oe`vec4 texColor = vec4(1.0);`}
        vec3 viewDirection = normalize(vpos - camPos);
        ${1===e.pbrMode?"applyPBRFactors();":""}
        float ssao = evaluateAmbientOcclusionInverse();
        ssao *= getBakedOcclusion();

        float additionalAmbientScale = _oldHeuristicLighting(vpos + localOrigin);
        vec3 additionalLight = ssao * lightingMainIntensity * additionalAmbientScale * ambientBoostFactor * lightingGlobalFactor;
        ${e.receiveShadows?"float shadow = readShadowMap(vpos, linearDepth);":1===e.viewingMode?"float shadow = lightingGlobalFactor * (1.0 - additionalAmbientScale);":"float shadow = 0.0;"}
        vec3 matColor = max(ambient, diffuse);
        ${e.attributeColor?oe`
        vec3 albedo_ = mixExternalColor(vColor.rgb * matColor, texColor.rgb, vcolorExt.rgb, int(colorMixMode));
        float opacity_ = layerOpacity * mixExternalOpacity(vColor.a * opacity, texColor.a, vcolorExt.a, int(colorMixMode));`:oe`
        vec3 albedo_ = mixExternalColor(matColor, texColor.rgb, vcolorExt.rgb, int(colorMixMode));
        float opacity_ = layerOpacity * mixExternalOpacity(opacity, texColor.a, vcolorExt.a, int(colorMixMode));
        `}
        ${oe`
        vec3 shadedNormal = normalize(vNormalWorld);
        albedo_ *= 1.2;
        vec3 viewForward = - vec3(view[0][2], view[1][2], view[2][2]);
        float alignmentLightView = clamp(dot(-viewForward, lightingMainDirection), 0.0, 1.0);
        float transmittance = 1.0 - clamp(dot(-viewForward, shadedNormal), 0.0, 1.0);
        float treeRadialFalloff = vColor.r;
        float backLightFactor = 0.5 * treeRadialFalloff * alignmentLightView * transmittance * (1.0 - shadow);
        additionalLight += backLightFactor * lightingMainIntensity;`}
        ${1===e.pbrMode||2===e.pbrMode?1===e.viewingMode?oe`vec3 normalGround = normalize(vpos + localOrigin);`:oe`vec3 normalGround = vec3(0.0, 0.0, 1.0);`:oe``}
        ${1===e.pbrMode||2===e.pbrMode?oe`
            float additionalAmbientIrradiance = additionalAmbientIrradianceFactor * lightingMainIntensity[2];
            vec3 shadedColor = evaluateSceneLightingPBR(shadedNormal, albedo_, shadow, 1.0 - ssao, additionalLight, viewDirection, normalGround, mrr, emission, additionalAmbientIrradiance);`:"vec3 shadedColor = evaluateSceneLighting(shadedNormal, albedo_, shadow, 1.0 - ssao, additionalLight);"}
        gl_FragColor = highlightSlice(vec4(shadedColor, opacity_), vpos);
        ${e.OITEnabled?"gl_FragColor = premultiplyAlpha(gl_FragColor);":""}
      }
    `)),t.include(At,e),t}});class Gt extends Nt{initializeProgram(e){const t=Gt.shader.get(),o=this.configuration,r=t.build({OITEnabled:0===o.transparencyPassType,output:o.output,viewingMode:e.viewingMode,receiveShadows:o.receiveShadows,slicePlaneEnabled:o.slicePlaneEnabled,sliceHighlightDisabled:o.sliceHighlightDisabled,sliceEnabledForVertexPrograms:!1,symbolColor:o.symbolColors,vvSize:o.vvSize,vvColor:o.vvColor,vvInstancingEnabled:!0,instanced:o.instanced,instancedColor:o.instancedColor,instancedDoublePrecision:o.instancedDoublePrecision,useOldSceneLightInterface:!1,pbrMode:o.usePBR?1:0,hasMetalnessAndRoughnessTexture:!1,hasEmissionTexture:!1,hasOcclusionTexture:!1,hasNormalTexture:!1,hasColorTexture:o.hasColorTexture,receiveAmbientOcclusion:o.receiveAmbientOcclusion,useCustomDTRExponentForWater:!1,normalType:0,doubleSidedMode:2,vertexTangets:!1,attributeTextureCoordinates:o.hasColorTexture?1:0,textureAlphaPremultiplied:o.textureAlphaPremultiplied,attributeColor:o.vertexColors,screenSizePerspectiveEnabled:o.screenSizePerspective,verticalOffsetEnabled:o.verticalOffset,offsetBackfaces:o.offsetBackfaces,doublePrecisionRequiresObfuscation:dt(e.rctx),alphaDiscardMode:o.alphaDiscardMode,supportsTextureAtlas:!1});return new Ne(e.rctx,r.generateSource("vertex"),r.generateSource("fragment"),ge)}}Gt.shader=new _e(Vt,(()=>Promise.resolve().then((function(){return Vt}))));class jt extends Pe{constructor(e,t){super(t,e,kt),this.supportsEdges=!0,this.techniqueConfig=new zt,this.vertexBufferLayout=jt.getVertexBufferLayout(this.params),this.instanceBufferLayout=e.instanced?jt.getInstanceBufferLayout(this.params):null}isVisibleInPass(e){return 4!==e||this.params.castShadows}isVisible(){const e=this.params;if(!super.isVisible()||0===e.layerOpacity)return!1;const t=e.instanced,o=e.vertexColors,r=e.symbolColors,a=!!t&&t.indexOf("color")>-1,i=e.vvColorEnabled,n="replace"===e.colorMixMode,s=e.opacity>0,l=e.externalColor&&e.externalColor[3]>0;return o&&(a||i||r)?!!n||s:o?n?l:s:a||i||r?!!n||s:n?l:s}getTechniqueConfig(e,t){return this.techniqueConfig.output=e,this.techniqueConfig.hasNormalTexture=!!this.params.normalTextureId,this.techniqueConfig.hasColorTexture=!!this.params.textureId,this.techniqueConfig.vertexTangents=this.params.vertexTangents,this.techniqueConfig.instanced=!!this.params.instanced,this.techniqueConfig.instancedDoublePrecision=this.params.instancedDoublePrecision,this.techniqueConfig.vvSize=this.params.vvSizeEnabled,this.techniqueConfig.verticalOffset=null!==this.params.verticalOffset,this.techniqueConfig.screenSizePerspective=null!==this.params.screenSizePerspective,this.techniqueConfig.slicePlaneEnabled=this.params.slicePlaneEnabled,this.techniqueConfig.sliceHighlightDisabled=this.params.sliceHighlightDisabled,this.techniqueConfig.alphaDiscardMode=this.params.textureAlphaMode,this.techniqueConfig.normalsTypeDerivate="screenDerivative"===this.params.normals,this.techniqueConfig.transparent=this.params.transparent,this.techniqueConfig.writeDepth=this.params.writeDepth,this.techniqueConfig.sceneHasOcludees=this.params.sceneHasOcludees,this.techniqueConfig.cullFace=null!=this.params.cullFace?this.params.cullFace:0,0!==e&&7!==e||(this.techniqueConfig.vertexColors=this.params.vertexColors,this.techniqueConfig.symbolColors=this.params.symbolColors,this.params.treeRendering?this.techniqueConfig.doubleSidedMode=2:this.techniqueConfig.doubleSidedMode=this.params.doubleSided&&"normal"===this.params.doubleSidedType?1:this.params.doubleSided&&"winding-order"===this.params.doubleSidedType?2:0,this.techniqueConfig.instancedColor=!!this.params.instanced&&this.params.instanced.indexOf("color")>-1,this.techniqueConfig.receiveShadows=this.params.receiveShadows&&this.params.shadowMappingEnabled,this.techniqueConfig.receiveAmbientOcclusion=this.params.receiveSSAO,this.techniqueConfig.vvColor=this.params.vvColorEnabled,this.techniqueConfig.textureAlphaPremultiplied=!!this.params.textureAlphaPremultiplied,this.techniqueConfig.usePBR=this.params.usePBR,this.techniqueConfig.hasMetalnessAndRoughnessTexture=!!this.params.metallicRoughnessTextureId,this.techniqueConfig.hasEmissionTexture=!!this.params.emissiveTextureId,this.techniqueConfig.hasOcclusionTexture=!!this.params.occlusionTextureId,this.techniqueConfig.offsetBackfaces=!(!this.params.transparent||!this.params.offsetTransparentBackfaces),this.techniqueConfig.isSchematic=this.params.usePBR&&this.params.isSchematic,this.techniqueConfig.transparencyPassType=t?t.transparencyPassType:3,this.techniqueConfig.enableOffset=!t||t.camera.relativeElevation<Re),this.techniqueConfig}intersect(e,t,o,r,a,i,n){if(null!==this.params.verticalOffset){const e=r.camera;C(Kt,o[12],o[13],o[14]);let t=null;switch(r.viewingMode){case 1:t=T(Jt,Kt);break;case 2:t=w(Jt,Qt)}let n=0;if(null!==this.params.verticalOffset){const o=M(Zt,Kt,e.eye),r=S(o),a=O(o,o,1/r);let i=null;this.params.screenSizePerspective&&(i=A(t,a)),n+=Le(e,r,this.params.verticalOffset,i,this.params.screenSizePerspective)}O(t,t,n),_(Yt,t,r.transform.inverseRotation),a=M(Wt,a,Yt),i=M(Xt,i,Yt)}Fe(e,t,r,a,i,Ze(r.verticalOffset),n)}getGLMaterial(e){if(0===e.output||7===e.output||1===e.output||2===e.output||3===e.output||4===e.output)return new qt(e)}createBufferWriter(){return new $t(this.vertexBufferLayout,this.instanceBufferLayout)}static getVertexBufferLayout(e){const t=e.textureId||e.normalTextureId||e.metallicRoughnessTextureId||e.emissiveTextureId||e.occlusionTextureId,o=je().vec3f("position").vec3f("normal");return e.vertexTangents&&o.vec4f("tangent"),t&&o.vec2f("uv0"),e.vertexColors&&o.vec4u8("color"),e.symbolColors&&o.vec4u8("symbolColor"),o}static getInstanceBufferLayout(e){let t=je();return t=e.instancedDoublePrecision?t.vec3f("modelOriginHi").vec3f("modelOriginLo").mat3f("model").mat3f("modelNormal"):t.mat4f("model").mat4f("modelNormal"),e.instanced&&e.instanced.indexOf("color")>-1&&(t=t.vec4f("instanceColor")),e.instanced&&e.instanced.indexOf("featureAttribute")>-1&&(t=t.vec4f("instanceFeatureAttribute")),t}}class qt extends et{constructor(e){const t=e.material;super({...e,...t.params}),this.updateParameters()}updateParameters(e){const t=this.material.params;this.updateTexture(t.textureId),this.technique=t.treeRendering?this.techniqueRep.acquireAndReleaseExisting(Gt,this.material.getTechniqueConfig(this.output,e),this.technique):this.techniqueRep.acquireAndReleaseExisting(Nt,this.material.getTechniqueConfig(this.output,e),this.technique)}selectPipelines(){}_updateShadowState(e){e.shadowMappingEnabled!==this.material.params.shadowMappingEnabled&&this.material.setParameterValues({shadowMappingEnabled:e.shadowMappingEnabled})}_updateOccludeeState(e){e.hasOccludees!==this.material.params.sceneHasOcludees&&this.material.setParameterValues({sceneHasOcludees:e.hasOccludees})}ensureParameters(e){0!==this.output&&7!==this.output||(this._updateShadowState(e),this._updateOccludeeState(e)),this.updateParameters(e)}bind(e,t){e.bindProgram(this.technique.program),this.technique.bindPass(e,this.material.params,t),this.bindTexture(e,this.technique.program)}beginSlot(e){return e===(this.material.params.transparent?this.material.params.writeDepth?5:8:3)}getPipelineState(e,t){return this.technique.getPipelineState(t)}}const Ht=2.1,kt={textureId:void 0,initTextureTransparent:!1,isSchematic:!1,usePBR:!1,normalTextureId:void 0,vertexTangents:!1,occlusionTextureId:void 0,emissiveTextureId:void 0,metallicRoughnessTextureId:void 0,emissiveFactor:[0,0,0],mrrFactors:[0,1,.5],ambient:[.2,.2,.2],diffuse:[.8,.8,.8],externalColor:[1,1,1,1],colorMixMode:"multiply",opacity:1,layerOpacity:1,vertexColors:!1,symbolColors:!1,doubleSided:!1,doubleSidedType:"normal",cullFace:void 0,instanced:void 0,instancedDoublePrecision:!1,normals:"default",receiveSSAO:!0,receiveShadows:!0,castShadows:!0,shadowMappingEnabled:!1,verticalOffset:null,screenSizePerspective:null,slicePlaneEnabled:!1,sliceHighlightDisabled:!1,offsetTransparentBackfaces:!1,vvSizeEnabled:!1,vvSizeMinSize:[1,1,1],vvSizeMaxSize:[100,100,100],vvSizeOffset:[0,0,0],vvSizeFactor:[1,1,1],vvSizeValue:[1,1,1],vvColorEnabled:!1,vvColorValues:[0,0,0,0,0,0,0,0],vvColorColors:[1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0],vvSymbolAnchor:[0,0,0],vvSymbolRotationMatrix:B(),transparent:!1,writeDepth:!0,textureAlphaMode:0,textureAlphaCutoff:Ie,textureAlphaPremultiplied:!1,sceneHasOcludees:!1,...Ee};class $t{constructor(e,t){this.vertexBufferLayout=e,this.instanceBufferLayout=t}allocate(e){return this.vertexBufferLayout.createBuffer(e)}elementCount(e){return e.indices.position.length}write(e,t,o,r){Be(t,this.vertexBufferLayout,e.transformation,e.invTranspTransformation,o,r)}}const Wt=y(),Xt=y(),Qt=b(0,0,1),Jt=y(),Yt=y(),Kt=y(),Zt=y();class eo{constructor(e){this.streamDataRequester=e}async loadJSON(e,t){return this.load("json",e,t)}async loadBinary(e,t){return v(e)?(s(t),f(e)):this.load("binary",e,t)}async loadImage(e,t){return this.load("image",e,t)}async load(e,t,o){if(i(this.streamDataRequester))return(await x(t,{responseType:to[e]})).data;const r=await p(this.streamDataRequester.request(t,e,o));if(!0===r.ok)return r.value;throw l(r.error),new u("",`Request for resource failed: ${r.error}`)}}const to={image:"image",binary:"array-buffer",json:"json"},oo=n.getLogger("esri.views.3d.glTF");class ro{constructor(e){this.data=e,this.offset4=0,this.dataUint32=new Uint32Array(this.data,0,Math.floor(this.data.byteLength/4))}readUint32(){const e=this.offset4;return this.offset4+=1,this.dataUint32[e]}readUint8Array(e){const t=4*this.offset4;return this.offset4+=e/4,new Uint8Array(this.data,t,e)}remainingBytes(){return this.data.byteLength-4*this.offset4}}const ao={baseColorFactor:[1,1,1,1],metallicFactor:1,roughnessFactor:1},io={pbrMetallicRoughness:ao,emissiveFactor:[0,0,0],alphaMode:"OPAQUE",alphaCutoff:.5,doubleSided:!1},no={ESRI_externalColorMixMode:"tint"},so=(e={})=>{const t={...ao,...e.pbrMetallicRoughness},o=function(e){switch(e.ESRI_externalColorMixMode){case"multiply":case"tint":case"ignore":case"replace":break;default:m(e.ESRI_externalColorMixMode),e.ESRI_externalColorMixMode="tint"}return e}({...no,...e.extras});return{...io,...e,pbrMetallicRoughness:t,extras:o}};const lo={magFilter:9729,minFilter:9987,wrapS:10497,wrapT:10497};function co(e,t){const o=e.count;t||(t=new e.TypedArrayConstructor(o));for(let r=0;r<o;r++)t[r]=e.get(r);return t}function uo(e,t,o){const r=e.typedBuffer,a=e.typedBufferStride,i=t.typedBuffer,n=t.typedBufferStride,s=o?o.count:t.count;let l=(o&&o.dstIndex?o.dstIndex:0)*a,c=(o&&o.srcIndex?o.srcIndex:0)*n;if(qe(t.elementType)){const e=He(t.elementType);if(ke(t.elementType))for(let t=0;t<s;++t)r[l]=Math.max(i[c]/e,-1),r[l+1]=Math.max(i[c+1]/e,-1),l+=a,c+=n;else for(let t=0;t<s;++t)r[l]=i[c]/e,r[l+1]=i[c+1]/e,l+=a,c+=n}else!function(e,t,o){const r=e.typedBuffer,a=e.typedBufferStride,i=t.typedBuffer,n=t.typedBufferStride,s=o?o.count:t.count;let l=(o&&o.dstIndex?o.dstIndex:0)*a,c=(o&&o.srcIndex?o.srcIndex:0)*n;for(let e=0;e<s;++e)r[l]=i[c],r[l+1]=i[c+1],l+=a,c+=n}(e,t,o);return e}function mo(e,t,o){const r=e.typedBuffer,a=e.typedBufferStride,i=t.typedBuffer,n=t.typedBufferStride,s=o?o.count:t.count;let l=(o&&o.dstIndex?o.dstIndex:0)*a,c=(o&&o.srcIndex?o.srcIndex:0)*n;for(let e=0;e<s;++e)r[l]=i[c],r[l+1]=i[c+1],r[l+2]=i[c+2],l+=a,c+=n}function po(e,t,o){const r=e.typedBuffer,a=e.typedBufferStride,i=t.typedBuffer,n=t.typedBufferStride,s=o?o.count:t.count;let l=(o&&o.dstIndex?o.dstIndex:0)*a,c=(o&&o.srcIndex?o.srcIndex:0)*n;for(let e=0;e<s;++e)r[l]=i[c],r[l+1]=i[c+1],r[l+2]=i[c+2],r[l+3]=i[c+3],l+=a,c+=n}function vo(e,t,o,r,a,i){const n=e.typedBuffer,s=e.typedBufferStride,l=i?i.count:e.count;let c=(i&&i.dstIndex?i.dstIndex:0)*s;for(let e=0;e<l;++e)n[c]=t,n[c+1]=o,n[c+2]=r,n[c+3]=a,c+=s}function fo(e,t){return new e(new ArrayBuffer(t*e.ElementCount*$e(e.ElementType)))}const ho=1179937895,go=1313821514,xo=5130562;class bo{constructor(e,t,o,r,a){this.context=e,this.errorContext=t,this.uri=o,this.json=r,this.glbBuffer=a,this.bufferCache=new Map,this.textureCache=new Map,this.materialCache=new Map,this.nodeParentMap=new Map,this.nodeTransformCache=new Map,this.baseUri=function(e){let t,o;return e.replace(/^(.*\/)?([^/]*)$/,((e,r,a)=>(t=r||"",o=a||"",""))),{dirPart:t,filePart:o}}(this.uri).dirPart,this.checkVersionSupported(),this.checkRequiredExtensionsSupported(),t.errorUnsupportedIf(null==r.scenes,"Scenes must be defined."),t.errorUnsupportedIf(null==r.meshes,"Meshes must be defined"),t.errorUnsupportedIf(null==r.nodes,"Nodes must be defined."),this.computeNodeParents()}static async load(e,t,o,r){if(v(o)){const r=h(o);if("model/gltf-binary"!==r.mediaType)try{const a=JSON.parse(r.isBase64?atob(r.data):r.data);return new bo(e,t,o,a)}catch{}const a=f(o);if(bo.isGLBData(a))return this.fromGLBData(e,t,o,a)}if(o.endsWith(".gltf")){const a=await e.loadJSON(o,r);return new bo(e,t,o,a)}const a=await e.loadBinary(o,r);if(bo.isGLBData(a))return this.fromGLBData(e,t,o,a);const i=await e.loadJSON(o,r);return new bo(e,t,o,i)}static isGLBData(e){const t=new ro(e);return t.remainingBytes()>=4&&t.readUint32()===ho}static async fromGLBData(e,t,o,r){const a=await bo.parseGLBData(t,r);return new bo(e,t,o,a.json,a.binaryData)}static async parseGLBData(e,t){const o=new ro(t);e.assert(o.remainingBytes()>=12,"GLB binary data is insufficiently large.");const r=o.readUint32(),a=o.readUint32(),i=o.readUint32();e.assert(r===ho,"Magic first 4 bytes do not fit to expected GLB value."),e.assert(t.byteLength>=i,"GLB binary data is smaller than header specifies."),e.errorUnsupportedIf(2!==a,"An unsupported GLB container version was detected. Only version 2 is supported.");let n,s,l=0;for(;o.remainingBytes()>=8;){const t=o.readUint32(),r=o.readUint32();0===l?(e.assert(r===go,"First GLB chunk must be JSON."),e.assert(t>=0,"No JSON data found."),n=await So(o.readUint8Array(t))):1===l?(e.errorUnsupportedIf(r!==xo,"Second GLB chunk expected to be BIN."),s=o.readUint8Array(t)):e.warnUnsupported("More than 2 GLB chunks detected. Skipping."),l+=1}return n||e.error("No GLB JSON chunk detected."),{json:n,binaryData:s}}async getBuffer(e,t){const o=this.json.buffers[e],r=this.errorContext;if(null==o.uri)return r.assert(null!=this.glbBuffer,"GLB buffer not present"),this.glbBuffer;let a=this.bufferCache.get(e);if(!a){const i=await this.context.loadBinary(this.resolveUri(o.uri),t);a=new Uint8Array(i),this.bufferCache.set(e,a),r.assert(a.byteLength===o.byteLength,"Buffer byte lengths should match.")}return a}async getAccessor(e,t){const o=this.json.accessors[e],r=this.errorContext;r.errorUnsupportedIf(null==o.bufferView,"Some accessor does not specify a bufferView."),r.errorUnsupportedIf(o.type in["MAT2","MAT3","MAT4"],`AttributeType ${o.type} is not supported`);const a=this.json.bufferViews[o.bufferView],i=await this.getBuffer(a.buffer,t),n=To[o.type],s=Mo[o.componentType],l=n*s,c=a.byteStride||l;return{raw:i.buffer,byteStride:c,byteOffset:i.byteOffset+(a.byteOffset||0)+(o.byteOffset||0),entryCount:o.count,isDenselyPacked:c===l,componentCount:n,componentByteSize:s,componentType:o.componentType,min:o.min,max:o.max,normalized:!!o.normalized}}async getIndexData(e,t){if(null==e.indices)return null;const o=await this.getAccessor(e.indices,t);if(o.isDenselyPacked)switch(o.componentType){case 5121:return new Uint8Array(o.raw,o.byteOffset,o.entryCount);case 5123:return new Uint16Array(o.raw,o.byteOffset,o.entryCount);case 5125:return new Uint32Array(o.raw,o.byteOffset,o.entryCount)}else switch(o.componentType){case 5121:return co(this.wrapAccessor(G,o));case 5123:return co(this.wrapAccessor(V,o));case 5125:return co(this.wrapAccessor(z,o))}}async getPositionData(e,t){const o=this.errorContext;o.errorUnsupportedIf(null==e.attributes.POSITION,"No POSITION vertex data found.");const r=await this.getAccessor(e.attributes.POSITION,t);return o.errorUnsupportedIf(5126!==r.componentType,"Expected type FLOAT for POSITION vertex attribute, but found "+Oo[r.componentType]),o.errorUnsupportedIf(3!==r.componentCount,"POSITION vertex attribute must have 3 components, but found "+r.componentCount.toFixed()),this.wrapAccessor(j,r)}async getNormalData(e,t){const o=this.errorContext;o.assert(null!=e.attributes.NORMAL,"No NORMAL vertex data found.");const r=await this.getAccessor(e.attributes.NORMAL,t);return o.errorUnsupportedIf(5126!==r.componentType,"Expected type FLOAT for NORMAL vertex attribute, but found "+Oo[r.componentType]),o.errorUnsupportedIf(3!==r.componentCount,"NORMAL vertex attribute must have 3 components, but found "+r.componentCount.toFixed()),this.wrapAccessor(j,r)}async getTangentData(e,t){const o=this.errorContext;o.assert(null!=e.attributes.TANGENT,"No TANGENT vertex data found.");const r=await this.getAccessor(e.attributes.TANGENT,t);return o.errorUnsupportedIf(5126!==r.componentType,"Expected type FLOAT for TANGENT vertex attribute, but found "+Oo[r.componentType]),o.errorUnsupportedIf(4!==r.componentCount,"TANGENT vertex attribute must have 4 components, but found "+r.componentCount.toFixed()),new q(r.raw,r.byteOffset,r.byteStride,r.byteOffset+r.byteStride*r.entryCount)}async getTextureCoordinates(e,t){const o=this.errorContext;o.assert(null!=e.attributes.TEXCOORD_0,"No TEXCOORD_0 vertex data found.");const r=await this.getAccessor(e.attributes.TEXCOORD_0,t);return o.errorUnsupportedIf(2!==r.componentCount,"TEXCOORD_0 vertex attribute must have 2 components, but found "+r.componentCount.toFixed()),5126===r.componentType?this.wrapAccessor(H,r):(o.errorUnsupportedIf(!r.normalized,"Integer component types are only supported for a normalized accessor for TEXCOORD_0."),function(e){switch(e.componentType){case 5120:return new Z(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5121:return new K(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5122:return new Y(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5123:return new J(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5125:return new Q(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);case 5126:return new H(e.raw,e.byteOffset,e.byteStride,e.byteOffset+e.byteStride*e.entryCount);default:return void m(e.componentType)}}(r))}async getVertexColors(e,t){const o=this.errorContext;o.assert(null!=e.attributes.COLOR_0,"No COLOR_0 vertex data found.");const r=await this.getAccessor(e.attributes.COLOR_0,t);if(o.errorUnsupportedIf(4!==r.componentCount&&3!==r.componentCount,"COLOR_0 attribute must have 3 or 4 components, but found "+r.componentCount.toFixed()),4===r.componentCount){if(5126===r.componentType)return this.wrapAccessor(q,r);if(5121===r.componentType)return this.wrapAccessor(k,r);if(5123===r.componentType)return this.wrapAccessor($,r)}else if(3===r.componentCount){if(5126===r.componentType)return this.wrapAccessor(j,r);if(5121===r.componentType)return this.wrapAccessor(W,r);if(5123===r.componentType)return this.wrapAccessor(X,r)}o.errorUnsupported("Unsupported component type for COLOR_0 attribute: "+Oo[r.componentType])}hasPositions(e){return void 0!==e.attributes.POSITION}hasNormals(e){return void 0!==e.attributes.NORMAL}hasVertexColors(e){return void 0!==e.attributes.COLOR_0}hasTextureCoordinates(e){return void 0!==e.attributes.TEXCOORD_0}hasTangents(e){return void 0!==e.attributes.TANGENT}async getMaterial(e,t){const o=this.errorContext;let r=this.materialCache.get(e.material);if(!r){const a=null!=e.material?so(this.json.materials[e.material]):so(),i=a.pbrMetallicRoughness,n=this.hasVertexColors(e);let s,l,c,d,u;i.baseColorTexture&&(o.errorUnsupportedIf(0!==(i.baseColorTexture.texCoord||0),"Only TEXCOORD with index 0 is supported."),s=await this.getTexture(i.baseColorTexture.index,t)),a.normalTexture&&(0!==(a.normalTexture.texCoord||0)?o.warnUnsupported("Only TEXCOORD with index 0 is supported for the normal map texture."):l=await this.getTexture(a.normalTexture.index,t)),a.occlusionTexture&&(0!==(a.occlusionTexture.texCoord||0)?o.warnUnsupported("Only TEXCOORD with index 0 is supported for the occlusion texture."):c=await this.getTexture(a.occlusionTexture.index,t)),a.emissiveTexture&&(0!==(a.emissiveTexture.texCoord||0)?o.warnUnsupported("Only TEXCOORD with index 0 is supported for the emissive texture."):d=await this.getTexture(a.emissiveTexture.index,t)),i.metallicRoughnessTexture&&(0!==(i.metallicRoughnessTexture.texCoord||0)?o.warnUnsupported("Only TEXCOORD with index 0 is supported for the metallicRoughness texture."):u=await this.getTexture(i.metallicRoughnessTexture.index,t));const m=null!=e.material?e.material:-1;r={alphaMode:a.alphaMode,alphaCutoff:a.alphaCutoff,color:i.baseColorFactor,doubleSided:!!a.doubleSided,colorTexture:s,normalTexture:l,name:a.name,id:m,occlusionTexture:c,emissiveTexture:d,emissiveFactor:a.emissiveFactor,metallicFactor:i.metallicFactor,roughnessFactor:i.roughnessFactor,metallicRoughnessTexture:u,vertexColors:n,ESRI_externalColorMixMode:a.extras.ESRI_externalColorMixMode}}return r}async getTexture(e,t){const o=this.errorContext,r=this.json.textures[e],a=(e=>({...lo,...e}))(null!=r.sampler?this.json.samplers[r.sampler]:{});o.errorUnsupportedIf(null==r.source,"Source is expected to be defined for a texture.");const i=this.json.images[r.source];let n=this.textureCache.get(e);if(!n){let r;if(i.uri)r=await this.context.loadImage(this.resolveUri(i.uri),t);else{o.errorUnsupportedIf(null==i.bufferView,"Image bufferView must be defined."),o.errorUnsupportedIf(null==i.mimeType,"Image mimeType must be defined.");const e=this.json.bufferViews[i.bufferView],a=await this.getBuffer(e.buffer,t);o.errorUnsupportedIf(null!=e.byteStride,"byteStride not supported for image buffer"),r=await async function(e,t){return c(((o,r)=>{const a=new Blob([e],{type:t}),i=URL.createObjectURL(a),n=new Image;n.addEventListener("load",(()=>{URL.revokeObjectURL(i),"decode"in n?n.decode().then((()=>o(n)),(()=>o(n))):o(n)})),n.addEventListener("error",(e=>{URL.revokeObjectURL(i),r(e)})),n.src=i}))}(new Uint8Array(a.buffer,a.byteOffset+(e.byteOffset||0),e.byteLength),i.mimeType)}n={data:r,wrapS:a.wrapS,wrapT:a.wrapT,minFilter:a.minFilter,name:i.name,id:e},this.textureCache.set(e,n)}return n}getNodeTransform(e){if(void 0===e)return Co;let t=this.nodeTransformCache.get(e);if(!t){const o=this.getNodeTransform(this.getNodeParent(e)),r=this.json.nodes[e];r.matrix?t=L(D(),o,r.matrix):r.translation||r.rotation||r.scale?(t=N(o),r.translation&&F(t,t,r.translation),r.rotation&&(wo[3]=te(wo,r.rotation),I(t,t,wo[3],wo)),r.scale&&E(t,t,r.scale)):t=o,this.nodeTransformCache.set(e,t)}return t}wrapAccessor(e,t){return new e(t.raw,t.byteOffset,t.byteStride,t.byteOffset+t.byteStride*(t.entryCount-1)+t.componentByteSize*t.componentCount)}resolveUri(e){return g(e,this.baseUri)}getNodeParent(e){return this.nodeParentMap.get(e)}checkVersionSupported(){const e=P.parse(this.json.asset.version,"glTF");yo.validate(e)}checkRequiredExtensionsSupported(){const e=this.json,t=this.errorContext;e.extensionsRequired&&0!==e.extensionsRequired.length&&t.errorUnsupported("gltf loader was not able to load unsupported feature. Required extensions: "+e.extensionsRequired.join(", "))}computeNodeParents(){this.json.nodes.forEach(((e,t)=>{e.children&&e.children.forEach((e=>{this.nodeParentMap.set(e,t)}))}))}}const yo=new P(2,0,"glTF"),Co=R(D(),Math.PI/2),wo=U(),To={SCALAR:1,VEC2:2,VEC3:3,VEC4:4},Mo={5120:1,5121:1,5122:2,5123:2,5126:4,5125:4};async function So(e){return c(((t,o)=>{const r=new Blob([e]),a=new FileReader;a.onload=()=>{const e=a.result;t(JSON.parse(e))},a.onerror=e=>{o(e)},a.readAsText(r)}))}const Oo={5120:"BYTE",5121:"UNSIGNED_BYTE",5122:"SHORT",5123:"UNSIGNED_SHORT",5125:"UNSIGNED_INT",5126:"FLOAT"};let Ao=0;async function _o(e,t,o={}){const r=await bo.load(e,Io,t,o),n="gltf_"+Ao++,s={lods:[],materials:new Map,textures:new Map,meta:Po(r)},l=!(!r.json.asset.extras||"symbolResource"!==r.json.asset.extras.ESRI_type);return await async function(e,t){const o=e.json,r=o.scenes[o.scene||0].nodes,a=r.length>1;for(const e of r){const t=o.nodes[e],r=[i(e,0)];if(Ro(t)&&!a){const e=t.extensions.MSFT_lod.ids;r.push(...e.map(((e,t)=>i(e,t+1))))}await d(r)}async function i(r,a){const n=o.nodes[r],s=e.getNodeTransform(r);if(Io.warnUnsupportedIf(null!=n.weights,"Morph targets are not supported."),null!=n.mesh){const e=o.meshes[n.mesh];for(const o of e.primitives)await t(o,s,a,e.name)}for(const e of n.children||[])await i(e,a)}}(r,(async(e,t,l,c)=>{const d=void 0!==e.mode?e.mode:4,u=function(e){switch(e){case 4:case 5:case 6:return e;default:return null}}(d);if(i(u))return void Io.warnUnsupported("Unsupported primitive mode ("+Bo[d]+"). Skipping primitive.");if(!r.hasPositions(e))return void Io.warn("Skipping primitive without POSITION vertex attribute.");const m=await r.getMaterial(e,o),p={transform:N(t),attributes:{position:await r.getPositionData(e,o),normal:null,texCoord0:null,color:null,tangent:null},indices:await r.getIndexData(e,o),primitiveType:u,material:Lo(s,m,n)};r.hasNormals(e)&&(p.attributes.normal=await r.getNormalData(e,o)),r.hasTangents(e)&&(p.attributes.tangent=await r.getTangentData(e,o)),r.hasTextureCoordinates(e)&&(p.attributes.texCoord0=await r.getTextureCoordinates(e,o)),r.hasVertexColors(e)&&(p.attributes.color=await r.getVertexColors(e,o));let v=null;a(s.meta)&&a(s.meta.ESRI_lod)&&"screenSpaceRadius"===s.meta.ESRI_lod.metric&&(v=s.meta.ESRI_lod.thresholds[l]),s.lods[l]=s.lods[l]||{parts:[],name:c,lodThreshold:v},s.lods[l].parts.push(p)})),{model:s,meta:{isEsriSymbolResource:l,uri:r.uri},customMeta:{}}}function Po(e){const t=e.json;let o=null;return t.nodes.forEach((e=>{const t=e.extras;a(t)&&(t.ESRI_proxyEllipsoid||t.ESRI_lod)&&(o=t)})),o}function Ro(e){return e.extensions&&e.extensions.MSFT_lod&&Array.isArray(e.extensions.MSFT_lod.ids)}function Lo(e,t,o){const r=t=>{const r=`${o}_tex_${t&&t.id}${t&&t.name?"_"+t.name:""}`;if(t&&!e.textures.has(r)){const o=function(e,t={}){return{data:e,parameters:{wrap:{s:10497,t:10497,...t.wrap},noUnpackFlip:!0,mipmap:!1,...t}}}(t.data,{wrap:{s:Fo(t.wrapS),t:Fo(t.wrapT)},mipmap:Eo.some((e=>e===t.minFilter)),noUnpackFlip:!0});e.textures.set(r,o)}return r},a=`${o}_mat_${t.id}_${t.name}`;if(!e.materials.has(a)){const o=function(e={}){return{color:[1,1,1],opacity:1,alphaMode:"OPAQUE",alphaCutoff:.5,doubleSided:!1,castShadows:!0,receiveShadows:!0,receiveAmbientOcclustion:!0,textureColor:null,textureNormal:null,textureOcclusion:null,textureEmissive:null,textureMetallicRoughness:null,emissiveFactor:[0,0,0],metallicFactor:1,roughnessFactor:1,colorMixMode:"multiply",...e}}({color:[t.color[0],t.color[1],t.color[2]],opacity:t.color[3],alphaMode:t.alphaMode,alphaCutoff:t.alphaCutoff,doubleSided:t.doubleSided,colorMixMode:t.ESRI_externalColorMixMode,textureColor:t.colorTexture?r(t.colorTexture):void 0,textureNormal:t.normalTexture?r(t.normalTexture):void 0,textureOcclusion:t.occlusionTexture?r(t.occlusionTexture):void 0,textureEmissive:t.emissiveTexture?r(t.emissiveTexture):void 0,textureMetallicRoughness:t.metallicRoughnessTexture?r(t.metallicRoughnessTexture):void 0,emissiveFactor:[t.emissiveFactor[0],t.emissiveFactor[1],t.emissiveFactor[2]],metallicFactor:t.metallicFactor,roughnessFactor:t.roughnessFactor});e.materials.set(a,o)}return a}function Fo(e){if(33071===e||33648===e||10497===e)return e;Io.error(`Unexpected TextureSampler WrapMode: ${e}`)}const Io=new class{error(e){throw new u("gltf-loader-error",e)}errorUnsupported(e){throw new u("gltf-loader-unsupported-feature",e)}errorUnsupportedIf(e,t){e&&this.errorUnsupported(t)}assert(e,t){e||this.error(t)}warn(e){oo.warn(e)}warnUnsupported(e){this.warn("[Unsupported Feature] "+e)}warnUnsupportedIf(e,t){e&&this.warnUnsupported(t)}},Eo=[9987,9985],Bo=["POINTS","LINES","LINE_LOOP","LINE_STRIP","TRIANGLES","TRIANGLE_STRIP","TRIANGLE_FAN"];function Do(e){return"number"==typeof e?function(e){return De(e)}(e):o(e)||r(e)?new Uint32Array(e):e}function No(e){const t="number"==typeof e?e:e.length;if(t<3)return new Uint32Array(0);const o=t-2,r=new Uint32Array(3*o);if("number"==typeof e){let e=0;for(let t=0;t<o;t+=1)t%2==0?(r[e++]=t,r[e++]=t+1,r[e++]=t+2):(r[e++]=t+1,r[e++]=t,r[e++]=t+2)}else{let t=0;for(let a=0;a<o;a+=1)if(a%2==0){const o=e[a],i=e[a+1],n=e[a+2];r[t++]=o,r[t++]=i,r[t++]=n}else{const o=e[a+1],i=e[a],n=e[a+2];r[t++]=o,r[t++]=i,r[t++]=n}}return r}function Uo(e){const t="number"==typeof e?e:e.length;if(t<3)return new Uint32Array(0);const o=t-2,r=new Uint32Array(3*o);if("number"==typeof e){let e=0;for(let t=0;t<o;++t)r[e++]=0,r[e++]=t+1,r[e++]=t+2;return r}{const t=e[0];let a=e[1],i=0;for(let n=0;n<o;++n){const o=e[n+2];r[i++]=t,r[i++]=a,r[i++]=o,a=o}return r}}function zo(e,t,o){if(e.count!==t.count)return void ee.error("source and destination buffers need to have the same number of elements");const r=e.count,a=o[0],i=o[1],n=o[2],s=o[3],l=o[4],c=o[5],d=o[6],u=o[7],m=o[8],p=e.typedBuffer,v=e.typedBufferStride,f=t.typedBuffer,h=t.typedBufferStride;for(let e=0;e<r;e++){const t=e*v,o=e*h,r=f[o],g=f[o+1],x=f[o+2],b=f[o+3];p[t]=a*r+s*g+d*x,p[t+1]=i*r+l*g+u*x,p[t+2]=n*r+c*g+m*x,p[t+3]=b}}function Vo(e,t,o){const r=Math.min(e.count,t.count),a=e.typedBuffer,i=e.typedBufferStride,n=t.typedBuffer,s=t.typedBufferStride;for(let e=0;e<r;e++){const t=e*i,r=e*s;a[t]=o*n[r],a[t+1]=o*n[r+1],a[t+2]=o*n[r+2],a[t+3]=o*n[r+3]}}function Go(e,t,o){const r=Math.min(e.count,t.count),a=e.typedBuffer,i=e.typedBufferStride,n=t.typedBuffer,s=t.typedBufferStride;for(let e=0;e<r;e++){const t=e*i,r=e*s;a[t]=n[r]>>o,a[t+1]=n[r+1]>>o,a[t+2]=n[r+2]>>o,a[t+3]=n[r+3]>>o}}export{_t as C,jt as D,Ft as E,Et as H,Bt as M,It as N,wt as P,ft as T,Ot as V,Pt as a,eo as b,Lt as c,Tt as d,St as e,ht as f,vt as g,xt as h,dt as i,ct as j,mo as k,_o as l,st as m,Ht as n,fo as o,uo as p,po as q,vo as r,Vo as s,zo as t,Uo as u,No as v,Do as w,Go as x,gt as y};
