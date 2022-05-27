/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import"./object.js";import{i as t,L as i,c as o,b as r}from"./Logger.js";import{c as s,s as n}from"./mathUtils2.js";import{c as a,f as l}from"./vec3f64.js";import{f as c,i as d,n as f,c as u,s as p,g as h,l as v,k as m,t as g,d as x,a as b}from"./vec3.js";import{l as O}from"./arcadeOnDemand.js";import{d as P}from"./screenUtils.js";import{a as S}from"./mat4.js";import{c as y}from"./aaBoundingRect.js";import{g as C,a as w}from"./unitConversionUtils.js";import{Z as z}from"./vec4f64.js";import{a as A,c as U}from"./quatf64.js";import{f as E}from"./mat3.js";import{c as T}from"./vec2.js";import{c as j,f as D}from"./vec2f64.js";import{g as R,S as I,e as V,Z as M,C as F,a1 as q,s as _,Y as H,f as B,p as G,a as $,b as N,D as L,V as W,i as k,R as X,M as Z,a8 as Q,c as Y,a9 as J,aa as K,a7 as ee,t as te,w as ie,v as oe,ab as re,x as se}from"./PiUtils.glsl.js";import{P as ne}from"./Program.js";import{c as ae,a as le,m as ce,d as de}from"./isWebGL2Context.js";import{n as fe}from"./InterleavedLayout.js";import{g as ue}from"./ColorMaterial.js";import{V as pe,a as he,b as ve}from"./Util.js";import{i as me}from"./Object3D.js";import{G as ge}from"./GLMaterialTexture.js";import{S as xe,V as be}from"./VerticalOffset.glsl.js";function Oe(e){const t=R`
    vec4 alignToPixelCenter(vec4 clipCoord, vec2 widthHeight) {
      vec2 xy = vec2(0.500123) + 0.5 * clipCoord.xy / clipCoord.w;
      vec2 pixelSz = vec2(1.0) / widthHeight;
      vec2 ij = (floor(xy * widthHeight) + vec2(0.5)) * pixelSz;
      vec2 result = (ij * 2.0 - vec2(1.0)) * clipCoord.w;
      return vec4(result, clipCoord.zw);
    }
  `,i=R`

    vec4 alignToPixelOrigin(vec4 clipCoord, vec2 widthHeight) {
      vec2 xy = vec2(0.5) + 0.5 * clipCoord.xy / clipCoord.w;
      vec2 pixelSz = vec2(1.0) / widthHeight;
      vec2 ij = floor((xy + 0.5 * pixelSz) * widthHeight) * pixelSz;
      vec2 result = (ij * 2.0 - vec2(1.0)) * clipCoord.w;
      return vec4(result, clipCoord.zw);
    }
  `;e.vertex.code.add(t),e.vertex.code.add(i),e.fragment.code.add(t),e.fragment.code.add(i)}function Pe(e){return function(e){return e instanceof Float32Array&&e.length>=16}(e)||function(e){return Array.isArray(e)&&e.length>=16}(e)}function Se(e,t){const i=e;i.include(xe),i.attributes.add("position","vec3"),i.attributes.add("normal","vec3"),i.attributes.add("auxpos1","vec4"),i.vertex.uniforms.add("proj","mat4"),i.vertex.uniforms.add("view","mat4"),i.vertex.uniforms.add("viewNormal","mat4"),i.vertex.uniforms.add("viewport","vec4"),i.vertex.uniforms.add("camPos","vec3"),i.vertex.uniforms.add("polygonOffset","float"),i.vertex.uniforms.add("cameraGroundRelative","float"),i.vertex.uniforms.add("pixelRatio","float"),i.vertex.uniforms.add("perDistancePixelRatio","float"),i.vertex.uniforms.add("uRenderTransparentlyOccludedHUD","float"),t.verticalOffsetEnabled&&i.vertex.uniforms.add("verticalOffset","vec4"),t.screenSizePerspectiveEnabled&&i.vertex.uniforms.add("screenSizePerspectiveAlignment","vec4"),i.vertex.uniforms.add("hudVisibilityTexture","sampler2D"),i.vertex.defines.addFloat("SMALL_OFFSET_ANGLE",.984807753012208),i.vertex.code.add(R`
    struct ProjectHUDAux {
      vec3 posModel;
      vec3 posView;
      vec3 vnormal;

      float distanceToCamera;
      float absCosAngle;
    };
  `),i.vertex.code.add(R`
    float applyHUDViewDependentPolygonOffset(float pointGroundDistance, float absCosAngle, inout vec3 posView) {
      float pointGroundSign = sign(pointGroundDistance);

      if (pointGroundSign == 0.0) {
        pointGroundSign = cameraGroundRelative;
      }

      // cameraGroundRelative is -1 if camera is below ground, 1 if above ground
      // groundRelative is 1 if both camera and symbol are on the same side of the ground, -1 otherwise
      float groundRelative = cameraGroundRelative * pointGroundSign;

      // view angle dependent part of polygon offset emulation
      // we take the absolute value because the sign that is dropped is
      // instead introduced using the ground-relative position of the symbol and the camera
      if (polygonOffset > .0) {
        float cosAlpha = clamp(absCosAngle, 0.01, 1.0);

        float tanAlpha = sqrt(1.0 - cosAlpha * cosAlpha) / cosAlpha;
        float factor = (1.0 - tanAlpha / viewport[2]);

        // same side of the terrain
        if (groundRelative > 0.0) {
          posView *= factor;
        }
        // opposite sides of the terrain
        else {
          posView /= factor;
        }
      }

      return groundRelative;
    }
  `),t.isDraped||i.vertex.code.add(R`
    void applyHUDVerticalGroundOffset(vec3 normalModel, inout vec3 posModel, inout vec3 posView) {
      float distanceToCamera = length(posView);

      // Compute offset in world units for a half pixel shift
      float pixelOffset = distanceToCamera * perDistancePixelRatio * 0.5;

      // Apply offset along normal in the direction away from the ground surface
      vec3 modelOffset = normalModel * cameraGroundRelative * pixelOffset;

      // Apply the same offset also on the view space position
      vec3 viewOffset = (viewNormal * vec4(modelOffset, 1.0)).xyz;

      posModel += modelOffset;
      posView += viewOffset;
    }
  `),i.vertex.code.add(R`
    vec4 projectPositionHUD(out ProjectHUDAux aux) {
      // centerOffset is in view space and is used to implement world size offsetting
      // of labels with respect to objects. It also pulls the label towards the viewer
      // so that the label is visible in front of the object.
      vec3 centerOffset = auxpos1.xyz;

      // The pointGroundDistance is the distance of the geometry to the ground and is
      // negative if the point is below the ground, or positive if the point is above
      // ground.
      float pointGroundDistance = auxpos1.w;

      aux.posModel = position;
      aux.posView = (view * vec4(aux.posModel, 1.0)).xyz;
      aux.vnormal = normal;
      ${t.isDraped?"":"applyHUDVerticalGroundOffset(aux.vnormal, aux.posModel, aux.posView);"}

      // Screen sized offset in world space, used for example for line callouts
      // Note: keep this implementation in sync with the CPU implementation, see
      //   - MaterialUtil.verticalOffsetAtDistance
      //   - HUDMaterial.applyVerticalOffsetTransformation

      aux.distanceToCamera = length(aux.posView);

      vec3 viewDirObjSpace = normalize(camPos - aux.posModel);
      float cosAngle = dot(aux.vnormal, viewDirObjSpace);

      aux.absCosAngle = abs(cosAngle);

      ${t.screenSizePerspectiveEnabled&&(t.verticalOffsetEnabled||1===t.screenCenterOffsetUnitsEnabled)?"vec4 perspectiveFactor = screenSizePerspectiveScaleFactor(aux.absCosAngle, aux.distanceToCamera, screenSizePerspectiveAlignment);":""}

      ${t.verticalOffsetEnabled?t.screenSizePerspectiveEnabled?"float verticalOffsetScreenHeight = applyScreenSizePerspectiveScaleFactorFloat(verticalOffset.x, perspectiveFactor);":"float verticalOffsetScreenHeight = verticalOffset.x;":""}

      ${t.verticalOffsetEnabled?R`
            float worldOffset = clamp(verticalOffsetScreenHeight * verticalOffset.y * aux.distanceToCamera, verticalOffset.z, verticalOffset.w);
            vec3 modelOffset = aux.vnormal * worldOffset;
            aux.posModel += modelOffset;
            vec3 viewOffset = (viewNormal * vec4(modelOffset, 1.0)).xyz;
            aux.posView += viewOffset;
            // Since we elevate the object, we need to take that into account
            // in the distance to ground
            pointGroundDistance += worldOffset;`:""}

      float groundRelative = applyHUDViewDependentPolygonOffset(pointGroundDistance, aux.absCosAngle, aux.posView);

      ${1!==t.screenCenterOffsetUnitsEnabled?R`
            // Apply x/y in view space, but z in screen space (i.e. along posView direction)
            aux.posView += vec3(centerOffset.x, centerOffset.y, 0.0);

            // Same material all have same z != 0.0 condition so should not lead to
            // branch fragmentation and will save a normalization if it's not needed
            if (centerOffset.z != 0.0) {
              aux.posView -= normalize(aux.posView) * centerOffset.z;
            }
          `:""}

      vec4 posProj = proj * vec4(aux.posView, 1.0);

      ${1===t.screenCenterOffsetUnitsEnabled?t.screenSizePerspectiveEnabled?"float centerOffsetY = applyScreenSizePerspectiveScaleFactorFloat(centerOffset.y, perspectiveFactor);":"float centerOffsetY = centerOffset.y;":""}

      ${1===t.screenCenterOffsetUnitsEnabled?"posProj.xy += vec2(centerOffset.x, centerOffsetY) * pixelRatio * 2.0 / viewport.zw * posProj.w;":""}

      // constant part of polygon offset emulation
      posProj.z -= groundRelative * polygonOffset * posProj.w;
      return posProj;
    }
  `),i.vertex.code.add(R`
    bool testVisibilityHUD(vec4 posProj) {
      // For occlusion testing, use the nearest pixel center to avoid
      // subpixel filtering messing up the color we use to test for
      vec4 posProjCenter = alignToPixelCenter(posProj, viewport.zw);

      vec4 occlusionPixel = texture2D(hudVisibilityTexture, .5 + .5 * posProjCenter.xy / posProjCenter.w);

      // the red pixel here indicates that the occlusion pixel passed the depth test against solid geometry and was written
      // the green pixel stores transparency of transparent geometry (1.0 -> fully transparent)
      // note that we also check against green == 0.0, i.e. transparent geometry that has opaque parts

      // thus we render visible pixels that are occluded by semi-transparent (but not fully transparent!) geometry here
      if (uRenderTransparentlyOccludedHUD > 0.5) {
        // multiplying by uRenderTransparentlyOccludedHUD allows us to ignore the second condition if
        // uRenderTransparentlyOccludedHUD = 0.75
        return occlusionPixel.r * occlusionPixel.g > 0.0 && occlusionPixel.g * uRenderTransparentlyOccludedHUD < 1.0;
      }

      // and visible pixels that are not occluded by semi-transparent geometry here
      return occlusionPixel.r * occlusionPixel.g > 0.0 && occlusionPixel.g == 1.0;
    }
  `)}function ye(e){const t=e;t.vertex.code.add(R`
  void main(void) {
    vec4 posProjCenter;
    if (dot(position, position) > 0.0) {
      // Render single point to center of the pixel to avoid subpixel
      // filtering to affect the marker color
      ProjectHUDAux projectAux;
      vec4 posProj = projectPositionHUD(projectAux);
      posProjCenter = alignToPixelCenter(posProj, viewport.zw);

      vec3 vpos = projectAux.posModel;
      if (rejectBySlice(vpos)) {
        // Project out of clip space
        posProjCenter = vec4(1e038, 1e038, 1e038, 1.0);
      }
    }
    else {
      // Project out of clip space
      posProjCenter = vec4(1e038, 1e038, 1e038, 1.0);
    }

    gl_Position = posProjCenter;
    gl_PointSize = 1.0;
  }
  `),t.fragment.uniforms.add("color","vec4"),t.fragment.code.add(R`
  void main() {
    gl_FragColor = color;
  }
  `)}var Ce,we;function ze(e,t=Ae){var i,o,r;return e.textureIsSignedDistanceField?(i=e.anchorPos,o=e.distanceFieldBoundingBox,(r=t)[0]=i[0]*(o[2]-o[0])+o[0],r[1]=i[1]*(o[3]-o[1])+o[1]):T(t,e.anchorPos),t}!function(e){e.bindUniforms=function(e,t){e.setUniform1f("uRenderTransparentlyOccludedHUD",0===t.renderTransparentlyOccludedHUD?1:1===t.renderTransparentlyOccludedHUD?0:.75)},e.bindVisibilityTexture=function(e,t,i){t.setUniform1i("hudVisibilityTexture",4),e.bindTexture(i.hudVisibilityTexture,4),e.setActiveTexture(0)}}(Se||(Se={})),function(e){e.bindUniforms=function(e){e.setUniform4f("color",1,1,1,1)}}(Ce||(Ce={})),function(e){e.bindUniforms=function(e,t,i){e.setUniform4fv("materialColor",t.color),t.textureIsSignedDistanceField&&(t.outlineColor[3]<=0||t.outlineSize<=0?(e.setUniform4fv("outlineColor",z),e.setUniform1f("outlineSize",0)):(e.setUniform4fv("outlineColor",t.outlineColor),e.setUniform1f("outlineSize",t.outlineSize))),e.setUniform2f("screenOffset",2*t.screenOffset[0]*i,2*t.screenOffset[1]*i),e.setUniform2fv("anchorPos",ze(t))}}(we||(we={}));const Ae=j();var Ue=Object.freeze({__proto__:null,build:function(e){const t=new I,i=e.signedDistanceFieldEnabled;if(t.include(Oe),t.include(Se,e),t.include(V,e),6===e.output)return t.include(ye,e),t;t.include(xe),t.fragment.include(M),t.fragment.include(F),t.include(q,e),t.varyings.add("vcolor","vec4"),t.varyings.add("vtc","vec2"),t.varyings.add("vsize","vec2"),e.binaryHighlightOcclusionEnabled&&t.varyings.add("voccluded","float"),t.vertex.uniforms.add("screenOffset","vec2").add("anchorPos","vec2").add("textureCoordinateScaleFactor","vec2").add("materialColor","vec4"),i&&t.vertex.uniforms.add("outlineColor","vec4"),e.screenSizePerspectiveEnabled&&t.vertex.uniforms.add("screenSizePerspective","vec4"),(e.debugDrawBorder||e.binaryHighlightOcclusionEnabled)&&t.varyings.add("debugBorderCoords","vec4"),t.attributes.add("uv0","vec2"),t.attributes.add("color","vec4"),t.attributes.add("size","vec2"),t.attributes.add("auxpos2","vec4"),t.vertex.code.add(R`
    void main(void) {
      ProjectHUDAux projectAux;
      vec4 posProj = projectPositionHUD(projectAux);

      if (rejectBySlice(projectAux.posModel)) {
        // Project outside of clip plane
        gl_Position = vec4(1e038, 1e038, 1e038, 1.0);
        return;
      }
      vec2 inputSize;
      ${e.screenSizePerspectiveEnabled?R`
      inputSize = screenSizePerspectiveScaleVec2(size, projectAux.absCosAngle, projectAux.distanceToCamera, screenSizePerspective);
      vec2 screenOffsetScaled = screenSizePerspectiveScaleVec2(screenOffset, projectAux.absCosAngle, projectAux.distanceToCamera, screenSizePerspectiveAlignment);
         `:R`
      inputSize = size;
      vec2 screenOffsetScaled = screenOffset;`}

      ${e.vvSize?"inputSize *= vvScale(auxpos2).xx;":""}

      vec2 combinedSize = inputSize * pixelRatio;
      vec4 quadOffset = vec4(0.0);

      ${e.occlusionTestEnabled||e.binaryHighlightOcclusionEnabled?"bool visible = testVisibilityHUD(posProj);":""}

      ${e.binaryHighlightOcclusionEnabled?"voccluded = visible ? 0.0 : 1.0;":""}
    `);const o=R`
      vec2 uv01 = floor(uv0);
      vec2 uv = uv0 - uv01;
      quadOffset.xy = ((uv01 - anchorPos) * 2.0 * combinedSize + screenOffsetScaled) / viewport.zw * posProj.w;
  `,r=e.pixelSnappingEnabled?i?R`
  posProj = alignToPixelOrigin(posProj, viewport.zw) + quadOffset;`:R`
  posProj += quadOffset;
  if (inputSize.x == size.x) {
    posProj = alignToPixelOrigin(posProj, viewport.zw);
  }`:R`posProj += quadOffset;`;t.vertex.code.add(R`
      ${e.occlusionTestEnabled?"if (visible) {":""}
      ${o}
      ${e.vvColor?"vcolor = vvGetColor(auxpos2, vvColorValues, vvColorColors) * materialColor;":"vcolor = color / 255.0 * materialColor;"}

      bool alphaDiscard = vcolor.a < ${R.float(_)};
      ${i?`alphaDiscard = alphaDiscard && outlineColor.a < ${R.float(_)};`:""}
      if (alphaDiscard) {
        // "early discard" if both symbol color (= fill) and outline color (if applicable) are transparent
        gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
        return;
      } else {
        ${r}
        gl_Position = posProj;
      }

      vtc = uv * textureCoordinateScaleFactor;

      ${e.debugDrawBorder?"debugBorderCoords = vec4(uv01, 1.5 / combinedSize);":""}
      vsize = inputSize;
      ${e.occlusionTestEnabled?R`} else { vtc = vec2(0.0);
        ${e.debugDrawBorder?"debugBorderCoords = vec4(0.5, 0.5, 1.5 / combinedSize);}":"}"}`:""}
    }
    `),t.fragment.uniforms.add("tex","sampler2D"),i&&(t.fragment.uniforms.add("outlineColor","vec4"),t.fragment.uniforms.add("outlineSize","float"));const s=e.debugDrawBorder?R`(isBorder > 0.0 ? 0.0 : ${R.float(H)})`:R.float(H),n=R`
    ${e.debugDrawBorder?R`
      float isBorder = float(any(lessThan(debugBorderCoords.xy, debugBorderCoords.zw)) || any(greaterThan(debugBorderCoords.xy, 1.0 - debugBorderCoords.zw)));`:""}

    ${i?R`
      vec4 fillPixelColor = vcolor;

      // Attempt to sample texel centers to avoid that thin cross outlines
      // disappear with large symbol sizes.
      // see: https://devtopia.esri.com/WebGIS/arcgis-js-api/issues/7058#issuecomment-603041
      const float txSize = 128.0;
      const float texelSize = 1.0 / txSize;
      // Calculate how much we have to add/subtract to/from each texel to reach the size of an onscreen pixel
      vec2 scaleFactor = (vsize - txSize) * texelSize;
      vec2 samplePos = vtc + (vec2(1.0, -1.0) * texelSize) * scaleFactor;

      // Get distance and map it into [-0.5, 0.5]
      float d = rgba2float(texture2D(tex, samplePos)) - 0.5;

      // Distance in output units (i.e. pixels)
      float dist = d * vsize.x;

      // Create smooth transition from the icon into its outline
      float fillAlphaFactor = clamp(0.5 - dist, 0.0, 1.0);
      fillPixelColor.a *= fillAlphaFactor;

      if (outlineSize > 0.25) {
        vec4 outlinePixelColor = outlineColor;
        float clampedOutlineSize = min(outlineSize, 0.5*vsize.x);

        // Create smooth transition around outline
        float outlineAlphaFactor = clamp(0.5 - (abs(dist) - 0.5*clampedOutlineSize), 0.0, 1.0);
        outlinePixelColor.a *= outlineAlphaFactor;

        if (
          outlineAlphaFactor + fillAlphaFactor < ${s} ||
          fillPixelColor.a + outlinePixelColor.a < ${R.float(_)}
        ) {
          discard;
        }

        // perform un-premultiplied over operator (see https://en.wikipedia.org/wiki/Alpha_compositing#Description)
        float compositeAlpha = outlinePixelColor.a + fillPixelColor.a * (1.0 - outlinePixelColor.a);
        vec3 compositeColor = vec3(outlinePixelColor) * outlinePixelColor.a +
          vec3(fillPixelColor) * fillPixelColor.a * (1.0 - outlinePixelColor.a);

        gl_FragColor = vec4(compositeColor, compositeAlpha);
      } else {
        if (fillAlphaFactor < ${s}) {
          discard;
        }

        gl_FragColor = premultiplyAlpha(fillPixelColor);
      }

      // visualize SDF:
      // gl_FragColor = vec4(clamp(-dist/vsize.x*2.0, 0.0, 1.0), clamp(dist/vsize.x*2.0, 0.0, 1.0), 0.0, 1.0);
      `:R`
          vec4 texColor = texture2D(tex, vtc, -0.5);
          if (texColor.a < ${s}) {
            discard;
          }
          gl_FragColor = texColor * premultiplyAlpha(vcolor);
          `}

    ${e.debugDrawBorder?R`gl_FragColor = mix(gl_FragColor, vec4(1.0, 0.0, 1.0, 1.0), isBorder);`:""}
  `;return 7===e.output&&t.fragment.code.add(R`
      void main() {
        ${n}
        gl_FragColor = vec4(gl_FragColor.a);
      }
      `),0===e.output&&t.fragment.code.add(R`
    void main() {
      ${n}
      ${e.FrontFacePass?"gl_FragColor.rgb /= gl_FragColor.a;":""}
    }
    `),4===e.output&&(t.include(B),t.fragment.code.add(R`
    void main() {
      ${n}
      ${e.binaryHighlightOcclusionEnabled?R`
          if (voccluded == 1.0) {
            gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
          } else {
            gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
          }`:"outputHighlight();"}
    }
    `)),t},get HUDMaterial(){return we},calculateAnchorPosForRendering:ze});class Ee extends N{initializeProgram(e){const t=Ee.shader.get(),i=this.configuration,o=t.build({output:i.output,FrontFacePass:2===i.transparencyPassType,viewingMode:e.viewingMode,occlusionTestEnabled:i.occlusionTestEnabled,signedDistanceFieldEnabled:i.sdf,slicePlaneEnabled:i.slicePlaneEnabled,sliceHighlightDisabled:!1,sliceEnabledForVertexPrograms:!0,debugDrawBorder:i.debugDrawBorder,binaryHighlightOcclusionEnabled:i.binaryHighlightOcclusion,screenCenterOffsetUnitsEnabled:i.screenCenterOffsetUnitsEnabled,screenSizePerspectiveEnabled:i.screenSizePerspective,verticalOffsetEnabled:i.verticalOffset,pixelSnappingEnabled:i.pixelSnappingEnabled,vvSize:i.vvSize,vvColor:i.vvColor,vvInstancingEnabled:!1,isDraped:i.isDraped});return new ne(e.rctx,o.generateSource("vertex"),o.generateSource("fragment"),L)}bindPass(e,t,i){W.bindProjectionMatrix(this.program,i.camera.projectionMatrix),this.program.setUniform1f("cameraGroundRelative",i.camera.aboveGround?1:-1),this.program.setUniform1f("perDistancePixelRatio",Math.tan(i.camera.fovY/2)/(i.camera.fullViewport[2]/2)),this.program.setUniformMatrix4fv("viewNormal",i.camera.viewInverseTransposeMatrix),this.program.setUniform1f("polygonOffset",t.shaderPolygonOffset),be.bindUniforms(this.program,t,i),xe.bindUniforms(this.program,t),this.program.setUniform1f("pixelRatio",i.camera.pixelRatio||1),W.bindViewport(this.program,i),6===this.configuration.output?Ce.bindUniforms(this.program):(Se.bindUniforms(this.program,i),we.bindUniforms(this.program,t,i.camera.pixelRatio||1),q.bindUniforms(this.program,t),this.configuration.occlusionTestEnabled&&Se.bindVisibilityTexture(e,this.program,i)),4===this.configuration.output&&B.bindOutputHighlight(e,this.program,i)}bindDraw(e){W.bindView(this.program,e),W.bindCamPosition(this.program,e.origin,e.camera.viewInverseTransposeMatrix),V.bindUniformsWithOrigin(this.program,this.configuration,e)}setPipelineState(e){const t=this.configuration,i=3===e,o=2===e,r=this.configuration.polygonOffsetEnabled&&Te,s=(i||o)&&4!==t.output?(t.depthEnabled||6===t.output)&&le:null;return ce({blending:0===t.output||7===t.output||4===t.output?i?je:k(e):null,depthTest:{func:515},depthWrite:s,colorWrite:de,polygonOffset:r})}initializePipeline(){return this.setPipelineState(this.configuration.transparencyPassType)}get primitiveType(){return 6===this.configuration.output?0:4}}Ee.shader=new X(Ue,(()=>Promise.resolve().then((function(){return Ue}))));const Te={factor:0,units:-4},je=ae(1,771);class De extends ${constructor(){super(...arguments),this.output=0,this.occlusionTestEnabled=!0,this.sdf=!1,this.vvSize=!1,this.vvColor=!1,this.verticalOffset=!1,this.screenSizePerspective=!1,this.screenCenterOffsetUnitsEnabled=0,this.debugDrawBorder=!0,this.binaryHighlightOcclusion=!0,this.slicePlaneEnabled=!1,this.polygonOffsetEnabled=!1,this.depthEnabled=!0,this.transparencyPassType=3,this.pixelSnappingEnabled=!0,this.isDraped=!1}}e([G({count:8})],De.prototype,"output",void 0),e([G()],De.prototype,"occlusionTestEnabled",void 0),e([G()],De.prototype,"sdf",void 0),e([G()],De.prototype,"vvSize",void 0),e([G()],De.prototype,"vvColor",void 0),e([G()],De.prototype,"verticalOffset",void 0),e([G()],De.prototype,"screenSizePerspective",void 0),e([G({count:2})],De.prototype,"screenCenterOffsetUnitsEnabled",void 0),e([G()],De.prototype,"debugDrawBorder",void 0),e([G()],De.prototype,"binaryHighlightOcclusion",void 0),e([G()],De.prototype,"slicePlaneEnabled",void 0),e([G()],De.prototype,"polygonOffsetEnabled",void 0),e([G()],De.prototype,"depthEnabled",void 0),e([G({count:4})],De.prototype,"transparencyPassType",void 0),e([G()],De.prototype,"pixelSnappingEnabled",void 0),e([G()],De.prototype,"isDraped",void 0);class Re extends Z{constructor(e,t){super(t,e,tt),this.techniqueConfig=new De}getTechniqueConfig(e,t){return this.techniqueConfig.output=e,this.techniqueConfig.slicePlaneEnabled=this.params.slicePlaneEnabled,this.techniqueConfig.verticalOffset=!!this.params.verticalOffset,this.techniqueConfig.screenSizePerspective=!!this.params.screenSizePerspective,this.techniqueConfig.screenCenterOffsetUnitsEnabled="screen"===this.params.centerOffsetUnits?1:0,this.techniqueConfig.polygonOffsetEnabled=this.params.polygonOffset,this.techniqueConfig.isDraped=this.params.isDraped,this.techniqueConfig.occlusionTestEnabled=this.params.occlusionTest,this.techniqueConfig.pixelSnappingEnabled=this.params.pixelSnappingEnabled,this.techniqueConfig.sdf=this.params.textureIsSignedDistanceField,this.techniqueConfig.vvSize=!!this.params.vvSizeEnabled,this.techniqueConfig.vvColor=!!this.params.vvColorEnabled,0===e&&(this.techniqueConfig.debugDrawBorder=!!this.params.debugDrawBorder),4===e&&(this.techniqueConfig.binaryHighlightOcclusion=this.params.binaryHighlightOcclusion),this.techniqueConfig.depthEnabled=this.params.depthEnabled,this.techniqueConfig.transparencyPassType=t?t.transparencyPassType:3,this.techniqueConfig}intersect(e,t,i,o,r,s,n,a,l){l?this.intersectDrapedHudGeometry(e,s,n,a):this.intersectHudGeometry(e,t,i,o,n,a)}intersectDrapedHudGeometry(e,t,i,o){const r=e.getAttribute(pe.POSITION),s=e.getAttribute(pe.SIZE),n=this.params,a=ze(n);let l=1,c=1;if(o){const e=o(Qe);l=e[0],c=e[5]}l*=e.screenToWorldRatio,c*=e.screenToWorldRatio;const d=Je*e.screenToWorldRatio;for(let o=0;o<r.data.length/r.size;o++){const f=o*r.size,u=r.data[f],p=r.data[f+1],h=o*s.size;let v;Ke[0]=s.data[h]*l,Ke[1]=s.data[h+1]*c,n.textureIsSignedDistanceField&&(v=n.outlineSize*e.screenToWorldRatio/2),Fe(t,u,p,Ke,d,v,n,a)&&i()}}intersectHudGeometry(e,t,i,o,r,s){if(!o.options.selectionMode||!o.options.hud)return;if(me(t))return;const n=e.data,l=this.params;let g=1,x=1;if(E(Le,i),s){const e=s(Qe);g=e[0],x=e[5],function(e){const t=e[0],i=e[1],o=e[2],r=e[3],s=e[4],n=e[5],a=e[6],l=e[7],c=e[8],d=1/Math.sqrt(t*t+i*i+o*o),f=1/Math.sqrt(r*r+s*s+n*n),u=1/Math.sqrt(a*a+l*l+c*c);e[0]=t*d,e[1]=i*d,e[2]=o*d,e[3]=r*f,e[4]=s*f,e[5]=n*f,e[6]=a*u,e[7]=l*u,e[8]=c*u}(Le)}const b=n.getVertexAttr()[pe.POSITION],O=n.getVertexAttr()[pe.SIZE],P=n.getVertexAttr()[pe.NORMAL],y=n.getVertexAttr()[pe.AUXPOS1];he(b.size>=3);const C=o.point,w=o.camera,z=ze(l);g*=w.pixelRatio,x*=w.pixelRatio;const A="screen"===this.params.centerOffsetUnits;for(let e=0;e<b.data.length/b.size;e++){const t=e*b.size;c(He,b.data[t],b.data[t+1],b.data[t+2]),d(He,He,i);const s=e*O.size;Ke[0]=O.data[s]*g,Ke[1]=O.data[s+1]*x,d(He,He,w.viewMatrix);const n=e*y.size;if(c(Xe,y.data[n+0],y.data[n+1],y.data[n+2]),!A&&(He[0]+=Xe[0],He[1]+=Xe[1],0!==Xe[2])){const e=Xe[2];f(Xe,He),u(He,He,p(Xe,Xe,e))}const U=e*P.size;if(c(Be,P.data[U],P.data[U+1],P.data[U+2]),this.normalAndViewAngle(Be,Le,w,Ze),this.applyVerticalOffsetTransformationView(He,Ze,w,qe),w.applyProjection(He,Ge),Ge[0]>-1){let e=Math.floor(Ge[0])+this.params.screenOffset[0],t=Math.floor(Ge[1])+this.params.screenOffset[1];A&&(e+=Xe[0],0!==Xe[1]&&(t+=Q(Xe[1],qe.factorAlignment))),Y(Ke,qe.factor,Ke);const i=Ye*w.pixelRatio;let s;if(l.textureIsSignedDistanceField&&(s=l.outlineSize*w.pixelRatio/2),Fe(C,e,t,Ke,i,s,l,z)){const e=o.ray;if(d(Ne,He,S(ke,w.viewMatrix)),Ge[0]=C[0],Ge[1]=C[1],w.unprojectFromRenderScreen(Ge,He)){const t=a();h(t,e.direction);const i=1/v(t);p(t,t,i);r(m(e.origin,He)*i,t,-1,1,!0,Ne)}}}}}computeAttachmentOrigin(e,t){const i=e.data,o="getVertexAttr"in i?i.getVertexAttr():"vertexAttr"in i?i.vertexAttr:null;if(!o)return null;const r=pe.POSITION,s=o[r],n="getIndices"in i?i.getIndices(r):"indices"in i?i.indices[r]:null;return J(s,n,t)}createBufferWriter(){return new ot(this)}normalAndViewAngle(e,t,i,o){return Pe(t)&&(t=E(We,t)),g(o.normal,e,t),d(o.normal,o.normal,i.viewInverseTransposeMatrix),o.cosAngle=x($e,et),o}updateScaleInfo(e,t,i){const o=this.params;o.screenSizePerspective?K(i,t,o.screenSizePerspective,e.factor):(e.factor.scale=1,e.factor.factor=0,e.factor.minPixelSize=0,e.factor.paddingPixels=0),o.screenSizePerspectiveAlignment?K(i,t,o.screenSizePerspectiveAlignment,e.factorAlignment):(e.factorAlignment.factor=e.factor.factor,e.factorAlignment.scale=e.factor.scale,e.factorAlignment.minPixelSize=e.factor.minPixelSize,e.factorAlignment.paddingPixels=e.factor.paddingPixels)}applyShaderOffsetsView(e,t,i,o,r,s,n){const a=this.normalAndViewAngle(t,i,r,Ze);return this.applyVerticalGroundOffsetView(e,a,r,n),this.applyVerticalOffsetTransformationView(n,a,r,s),this.applyPolygonOffsetView(n,a,o[3],r,n),this.applyCenterOffsetView(n,o,n),n}applyShaderOffsetsNDC(e,i,o,r,s){return this.applyCenterOffsetNDC(e,i,o,r),t(s)&&h(s,r),this.applyPolygonOffsetNDC(r,i,o,r),r}applyPolygonOffsetView(e,t,i,o,r){const a=o.aboveGround?1:-1;let l=n(i);0===l&&(l=a);const c=a*l;if(this.params.shaderPolygonOffset<=0)return h(r,e);const d=s(Math.abs(t.cosAngle),.01,1),f=1-Math.sqrt(1-d*d)/d/o.viewport[2];return p(r,e,c>0?f:1/f),r}applyVerticalGroundOffsetView(e,t,i,o){const r=v(e),s=i.aboveGround?1:-1,n=.5*i.computeRenderPixelSizeAtDist(r),a=p(He,t.normal,s*n);return b(o,e,a),o}applyVerticalOffsetTransformationView(e,t,i,o){const r=this.params;if(!r.verticalOffset||!r.verticalOffset.screenLength){if(r.screenSizePerspective||r.screenSizePerspectiveAlignment){const i=v(e);this.updateScaleInfo(o,i,t.cosAngle)}else o.factor.scale=1,o.factorAlignment.scale=1;return e}const s=v(e),n=r.screenSizePerspectiveAlignment||r.screenSizePerspective,a=ee(i,s,r.verticalOffset,t.cosAngle,n);return this.updateScaleInfo(o,s,t.cosAngle),p(t.normal,t.normal,a),b(e,e,t.normal)}applyCenterOffsetView(e,t,i){const o="screen"!==this.params.centerOffsetUnits;return i!==e&&h(i,e),o&&(i[0]+=t[0],i[1]+=t[1],t[2]&&(f(Be,i),b(i,i,p(Be,Be,t[2])))),i}applyCenterOffsetNDC(e,t,i,o){const r="screen"!==this.params.centerOffsetUnits;return o!==e&&h(o,e),r||(o[0]+=t[0]/i.fullWidth*2,o[1]+=t[1]/i.fullHeight*2),o}applyPolygonOffsetNDC(e,t,i,o){const r=this.params.shaderPolygonOffset;if(e!==o&&h(o,e),r){const e=i.aboveGround?1:-1,s=e*n(t[3]);o[2]-=(s||e)*r}return o}getGLMaterial(e){return 0===e.output||7===e.output?new Ve(e):4===e.output?new Me(e):void 0}calculateRelativeScreenBounds(e,t,i=y()){return function(e,t,i,o=_e){T(o,e.anchorPos),o[0]*=-t[0],o[1]*=-t[1],o[0]+=e.screenOffset[0]*i,o[1]+=e.screenOffset[1]*i}(this.params,e,t,i),i[2]=i[0]+e[0],i[3]=i[1]+e[1],i}}class Ie extends ge{constructor(e){super({...e,...e.material.params}),this.updateParameters()}beginSlot(e){return e===(this.material.params.drawInSecondSlot?19:18)}updateParameters(e){this.updateTexture(this.material.params.textureId),this.selectProgram(e)}selectProgram(e){this.technique=this.techniqueRep.acquireAndReleaseExisting(Ee,this.material.getTechniqueConfig(this.output,e),this.technique)}ensureParameters(e){this.updateParameters(e)}bind(e,t){e.bindProgram(this.technique.program),this.bindTexture(e,this.technique.program),this.bindTextureScale(e,this.technique.program),this.technique.bindPass(e,this.material.params,t)}}class Ve extends Ie{constructor(e){super(e),this.isOcclusionSlot=!1}beginSlot(e){const t=this.material.params.drawInSecondSlot?19:18;return this.material.params.occlusionTest?(this.isOcclusionSlot=12===e,12===e||e===t):(this.isOcclusionSlot=!1,e===t)}getTechnique(){return this.isOcclusionSlot?this.occlusionTechnique:this.technique}selectProgram(e){this.technique=this.techniqueRep.acquireAndReleaseExisting(Ee,this.material.getTechniqueConfig(this.output,e),this.technique),this.occlusionTechnique=this.techniqueRep.acquireAndReleaseExisting(Ee,this.material.getTechniqueConfig(6,e),this.occlusionTechnique)}bind(e,t){const i=this.getTechnique();e.bindProgram(i.program),this.isOcclusionSlot||(this.bindTexture(e,i.program),this.bindTextureScale(e,i.program)),i.bindPass(e,this.material.params,t)}}class Me extends Ie{constructor(e){super({...e,output:4})}}function Fe(e,t,i,o,r,s,n,a){let l=t-r-(a[0]>0?o[0]*a[0]:0),c=l+o[0]+2*r,d=i-r-(a[1]>0?o[1]*a[1]:0),f=d+o[1]+2*r;if(n.textureIsSignedDistanceField){const e=n.distanceFieldBoundingBox;l+=o[0]*e[0],d+=o[1]*e[1],c-=o[0]*(1-e[2]),f-=o[1]*(1-e[3]),l-=s,c+=s,d-=s,f+=s}return e[0]>l&&e[0]<c&&e[1]>d&&e[1]<f}const qe={factor:{scale:0,factor:0,minPixelSize:0,paddingPixels:0},factorAlignment:{scale:0,factor:0,minPixelSize:0,paddingPixels:0}},_e=j(),He=a(),Be=a(),Ge=P(),$e=a(),Ne=a(),Le=A(),We=A(),ke=U(),Xe=a(),Ze={normal:$e,cosAngle:0},Qe=U(),Ye=1,Je=2,Ke=[0,0],et=l(0,0,1),tt={texCoordScale:[1,1],occlusionTest:!0,binaryHighlightOcclusion:!0,drawInSecondSlot:!1,color:[1,1,1,1],outlineColor:[1,1,1,1],outlineSize:0,textureIsSignedDistanceField:!1,distanceFieldBoundingBox:null,vvSizeEnabled:!1,vvSizeMinSize:[1,1,1],vvSizeMaxSize:[100,100,100],vvSizeOffset:[0,0,0],vvSizeFactor:[1,1,1],vvColorEnabled:!1,vvColorValues:[0,0,0,0,0,0,0,0],vvColorColors:[1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0],screenOffset:[0,0],verticalOffset:null,screenSizePerspective:null,screenSizePerspectiveAlignment:null,slicePlaneEnabled:!1,anchorPos:D(.5,.5),shaderPolygonOffset:1e-5,polygonOffset:!1,textureId:null,centerOffsetUnits:"world",depthEnabled:!0,pixelSnappingEnabled:!0,debugDrawBorder:!1,isDraped:!1,...te},it=fe().vec3f(pe.POSITION).vec3f(pe.NORMAL).vec2f(pe.UV0).vec4u8(pe.COLOR).vec2f(pe.SIZE).vec4f(pe.AUXPOS1).vec4f(pe.AUXPOS2);class ot{constructor(e){this.material=e,this.vertexBufferLayout=it}allocate(e){return this.vertexBufferLayout.createBuffer(e)}elementCount(e){return 6*e.indices[pe.POSITION].length}write(e,t,i,o){ie(t.indices[pe.POSITION],t.vertexAttr[pe.POSITION].data,e.transformation,i.position,o,6),oe(t.indices[pe.NORMAL],t.vertexAttr[pe.NORMAL].data,e.invTranspTransformation,i.normal,o,6);{const e=t.vertexAttr[pe.UV0].data;let r,s,n,a;if(null==e||e.length<4){const e=this.material.params;r=0,s=0,n=e.texCoordScale[0],a=e.texCoordScale[1]}else r=e[0],s=e[1],n=e[2],a=e[3];n=Math.min(1.99999,n+1),a=Math.min(1.99999,a+1);const l=t.indices[pe.POSITION].length,c=i.uv0;let d=o;for(let e=0;e<l;++e)c.set(d,0,r),c.set(d,1,s),d+=1,c.set(d,0,n),c.set(d,1,s),d+=1,c.set(d,0,n),c.set(d,1,a),d+=1,c.set(d,0,n),c.set(d,1,a),d+=1,c.set(d,0,r),c.set(d,1,a),d+=1,c.set(d,0,r),c.set(d,1,s),d+=1}re(t.indices[pe.COLOR],t.vertexAttr[pe.COLOR].data,4,i.color,o,6);{const e=t.indices[pe.SIZE],r=t.vertexAttr[pe.SIZE].data,s=e.length,n=i.size;let a=o;for(let t=0;t<s;++t){const i=r[2*e[t]],o=r[2*e[t]+1];for(let e=0;e<6;++e)n.set(a,0,i),n.set(a,1,o),a+=1}}t.indices[pe.AUXPOS1]&&t.vertexAttr[pe.AUXPOS1]&&se(t.indices[pe.AUXPOS1],t.vertexAttr[pe.AUXPOS1].data,i.auxpos1,o,6),t.indices[pe.AUXPOS2]&&t.vertexAttr[pe.AUXPOS2]&&se(t.indices[pe.AUXPOS2],t.vertexAttr[pe.AUXPOS2].data,i.auxpos2,o,6)}}const rt=i.getLogger("esri.views.3d.layers.graphics.featureExpressionInfoUtils");function st(e){const t=e&&e.expression;if("string"==typeof t){const e=ut(t);if(null!=e)return{cachedResult:e}}return null}async function nt(e,t,i){const o=e&&e.expression;if("string"!=typeof o)return null;const r=ut(o);if(null!=r)return{cachedResult:r};const s=await O(),n=s.arcadeUtils,a=n.createSyntaxTree(o);return n.dependsOnView(a)?(null!=i&&i.error("Expressions containing '$view' are not supported on ElevationInfo"),{cachedResult:0}):{arcade:{func:n.createFunction(a),context:n.createExecContext(null,{sr:t}),modules:s}}}function at(e,t,i){return e.arcadeUtils.createFeature(t.attributes,t.geometry,i)}function lt(e,t){if(null!=e&&!ft(e)){if(!t||!e.arcade)return void rt.errorOncePerTick("Arcade support required but not provided");const i=t;i._geometry&&(i._geometry=ue(i._geometry)),e.arcade.modules.arcadeUtils.updateExecContext(e.arcade.context,t)}}function ct(e,t=!1){let i=e&&e.featureExpressionInfo;const o=i&&i.expression;return t||"0"===o||(i=null),i}const dt={cachedResult:0};function ft(e){return null!=e.cachedResult}function ut(e){return"0"===e?0:null}class pt{constructor(){this._meterUnitOffset=0,this._renderUnitOffset=0,this._unit="meters",this._metersPerElevationInfoUnit=1,this._featureExpressionInfoContext=null,this.centerPointInElevationSR=null,this.mode=null}get featureExpressionInfoContext(){return this._featureExpressionInfoContext}get meterUnitOffset(){return this._meterUnitOffset}get unit(){return this._unit}set unit(e){this._unit=e,this._metersPerElevationInfoUnit=C(e)}reset(){this.mode=null,this._meterUnitOffset=0,this._renderUnitOffset=0,this._featureExpressionInfoContext=null,this.unit="meters"}set offsetMeters(e){this._meterUnitOffset=e,this._renderUnitOffset=0}set offsetElevationInfoUnits(e){this._meterUnitOffset=e*this._metersPerElevationInfoUnit,this._renderUnitOffset=0}addOffsetRenderUnits(e){this._renderUnitOffset+=e}geometryZWithOffset(e,t){const i=this.calculateOffsetRenderUnits(t);return null!=this.featureExpressionInfoContext?i:e+i}calculateOffsetRenderUnits(e){let t=this._meterUnitOffset;const i=this.featureExpressionInfoContext;return null!=i&&(t+=function(e){if(null!=e){if(ft(e))return e.cachedResult;const t=e.arcade;let i=e.arcade.modules.arcadeUtils.executeFunction(t.func,t.context);return"number"!=typeof i&&(e.cachedResult=0,i=0),i}return 0}(i)*this._metersPerElevationInfoUnit),t/e.unitInMeters+this._renderUnitOffset}setFromElevationInfo(e){this.mode=e.mode,this.unit=w(e.unit)?e.unit:"meters",this.offsetElevationInfoUnits=o(e.offset,0)}updateFeatureExpressionInfoContext(e,i,o){if(r(e))return void(this._featureExpressionInfoContext=null);const s=e&&e.arcade;var n;s&&t(i)&&t(o)?(this._featureExpressionInfoContext={cachedResult:(n=e).cachedResult,arcade:n.arcade?{func:n.arcade.func,context:n.arcade.modules.arcadeUtils.createExecContext(null,{sr:n.arcade.context.spatialReference}),modules:n.arcade.modules}:null},lt(this._featureExpressionInfoContext,at(s.modules,i,o))):this._featureExpressionInfoContext=e}static fromElevationInfo(e){const i=new pt;return t(e)&&i.setFromElevationInfo(e),i}}function ht(e,t,i){switch(e){case"circle":return vt(t,i);case"square":return mt(t,i);case"cross":return xt(t,i);case"x":return bt(t,i);case"kite":return gt(t,i);case"triangle":return Ot(t,i);default:return vt(t,i)}}function vt(e,t){const i=e,o=new Uint8Array(4*i*i),r=i/2-.5,s=t/2;for(let t=0;t<i;t++)for(let n=0;n<i;n++){const a=n+i*t,l=n-r,c=t-r;let d=Math.sqrt(l*l+c*c)-s;d=d/e+.5,ve(d,o,4*a)}return o}function mt(e,t){return Pt(e,t,!1)}function gt(e,t){return Pt(e,t,!0)}function xt(e,t){return St(e,t,!1)}function bt(e,t){return St(e,t,!0)}function Ot(e,t){const i=new Uint8Array(4*e*e),o=-.5,r=Math.sqrt(1.25),s=(e-t)/2;for(let n=0;n<e;n++)for(let a=0;a<e;a++){const l=n*e+a,c=(a-s)/t,d=(n-s+.75)/t,f=-(1*c+o*d)/r,u=(1*(c-1)+o*-d)/r,p=-d,h=Math.max(f,u,p);ve(h*t/e+.5,i,4*l)}return i}function Pt(e,t,i){i&&(t/=Math.SQRT2);const o=new Uint8Array(4*e*e);for(let r=0;r<e;r++)for(let s=0;s<e;s++){let n=s-.5*e+.25,a=.5*e-r-.75;const l=r*e+s;if(i){const e=(n+a)/Math.SQRT2;a=(a-n)/Math.SQRT2,n=e}let c=Math.max(Math.abs(n),Math.abs(a))-.5*t;c=c/e+.5,ve(c,o,4*l)}return o}function St(e,t,i){i&&(t*=Math.SQRT2);const o=.5*t,r=new Uint8Array(4*e*e);for(let t=0;t<e;t++)for(let s=0;s<e;s++){let n=s-.5*e,a=.5*e-t-1;const l=t*e+s;if(i){const e=(n+a)/Math.SQRT2;a=(a-n)/Math.SQRT2,n=e}let c;n=Math.abs(n),a=Math.abs(a),c=n>a?n>o?Math.sqrt((n-o)*(n-o)+a*a):a:a>o?Math.sqrt(n*n+(a-o)*(a-o)):n,c=c/e+.5,ve(c,r,4*l)}return r}export{Oe as A,pt as E,Re as H,Se as a,Ot as b,at as c,bt as d,xt as e,gt as f,mt as g,vt as h,ht as i,ct as j,nt as k,st as l,lt as s,dt as z};
