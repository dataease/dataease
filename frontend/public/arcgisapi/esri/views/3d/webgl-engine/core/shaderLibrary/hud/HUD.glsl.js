// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../util/ScreenSizePerspective.glsl"],function(f,c,g){function e(a,b){a.include(g.ScreenSizePerspective);a.attributes.add("position","vec3");a.attributes.add("normal","vec3");a.attributes.add("auxpos1","vec4");a.vertex.uniforms.add("proj","mat4");a.vertex.uniforms.add("view","mat4");a.vertex.uniforms.add("viewNormal","mat4");a.vertex.uniforms.add("viewport","vec4");a.vertex.uniforms.add("camPos","vec3");a.vertex.uniforms.add("polygonOffset","float");
a.vertex.uniforms.add("cameraGroundRelative","float");a.vertex.uniforms.add("pixelRatio","float");a.vertex.uniforms.add("perDistancePixelRatio","float");a.vertex.uniforms.add("uRenderTransparentlyOccludedHUD","float");b.verticalOffsetEnabled&&a.vertex.uniforms.add("verticalOffset","vec4");b.screenSizePerspectiveEnabled&&a.vertex.uniforms.add("screenSizePerspectiveAlignment","vec4");a.vertex.uniforms.add("hudVisibilityTexture","sampler2D");a.vertex.defines.addFloat("SMALL_OFFSET_ANGLE",.984807753012208);
a.vertex.code.add(c.glsl`
    struct ProjectHUDAux {
      vec3 posModel;
      vec3 posView;
      vec3 vnormal;

      float distanceToCamera;
      float absCosAngle;
    };
  `);a.vertex.code.add(c.glsl`
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
  `);b.isDraped||a.vertex.code.add(c.glsl`
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
  `);a.vertex.code.add(c.glsl`
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
      ${b.isDraped?"":"applyHUDVerticalGroundOffset(aux.vnormal, aux.posModel, aux.posView);"}

      // Screen sized offset in world space, used for example for line callouts
      // Note: keep this implementation in sync with the CPU implementation, see
      //   - MaterialUtil.verticalOffsetAtDistance
      //   - HUDMaterial.applyVerticalOffsetTransformation

      aux.distanceToCamera = length(aux.posView);

      vec3 viewDirObjSpace = normalize(camPos - aux.posModel);
      float cosAngle = dot(aux.vnormal, viewDirObjSpace);

      aux.absCosAngle = abs(cosAngle);

      ${b.screenSizePerspectiveEnabled?b.verticalOffsetEnabled||1===b.screenCenterOffsetUnitsEnabled?"vec4 perspectiveFactor \x3d screenSizePerspectiveScaleFactor(aux.absCosAngle, aux.distanceToCamera, screenSizePerspectiveAlignment);":"":""}

      ${b.verticalOffsetEnabled?b.screenSizePerspectiveEnabled?"float verticalOffsetScreenHeight \x3d applyScreenSizePerspectiveScaleFactorFloat(verticalOffset.x, perspectiveFactor);":"float verticalOffsetScreenHeight \x3d verticalOffset.x;":""}

      ${b.verticalOffsetEnabled?c.glsl`
            float worldOffset = clamp(verticalOffsetScreenHeight * verticalOffset.y * aux.distanceToCamera, verticalOffset.z, verticalOffset.w);
            vec3 modelOffset = aux.vnormal * worldOffset;
            aux.posModel += modelOffset;
            vec3 viewOffset = (viewNormal * vec4(modelOffset, 1.0)).xyz;
            aux.posView += viewOffset;
            // Since we elevate the object, we need to take that into account
            // in the distance to ground
            pointGroundDistance += worldOffset;`:""}

      float groundRelative = applyHUDViewDependentPolygonOffset(pointGroundDistance, aux.absCosAngle, aux.posView);

      ${1!==b.screenCenterOffsetUnitsEnabled?c.glsl`
            // Apply x/y in view space, but z in screen space (i.e. along posView direction)
            aux.posView += vec3(centerOffset.x, centerOffset.y, 0.0);

            // Same material all have same z != 0.0 condition so should not lead to
            // branch fragmentation and will save a normalization if it's not needed
            if (centerOffset.z != 0.0) {
              aux.posView -= normalize(aux.posView) * centerOffset.z;
            }
          `:""}

      vec4 posProj = proj * vec4(aux.posView, 1.0);

      ${1===b.screenCenterOffsetUnitsEnabled?b.screenSizePerspectiveEnabled?"float centerOffsetY \x3d applyScreenSizePerspectiveScaleFactorFloat(centerOffset.y, perspectiveFactor);":"float centerOffsetY \x3d centerOffset.y;":""}

      ${1===b.screenCenterOffsetUnitsEnabled?"posProj.xy +\x3d vec2(centerOffset.x, centerOffsetY) * pixelRatio * 2.0 / viewport.zw * posProj.w;":""}

      // constant part of polygon offset emulation
      posProj.z -= groundRelative * polygonOffset * posProj.w;
      return posProj;
    }
  `);a.vertex.code.add(c.glsl`
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
  `)}(function(a){a.bindUniforms=function(b,d){b.setUniform1f("uRenderTransparentlyOccludedHUD",0===d.renderTransparentlyOccludedHUD?1:1===d.renderTransparentlyOccludedHUD?0:.75)};a.bindVisibilityTexture=function(b,d,h){d.setUniform1i("hudVisibilityTexture",4);b.bindTexture(h.hudVisibilityTexture,4);b.setActiveTexture(0)}})(e||(e={}));f.HUD=e;Object.defineProperty(f,"__esModule",{value:!0})});