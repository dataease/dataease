// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ./vec2f64 ./vec4f64 ./vec2 ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/hud/AlignPixel.glsl ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/ScreenSizePerspective.glsl ../views/3d/webgl-engine/core/shaderLibrary/hud/HUD.glsl ../views/3d/webgl-engine/core/shaderLibrary/hud/HUDOcclusionPass.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputHighlight.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/VisualVariables.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/RgbaFloatEncoding.glsl".split(" "),
function(f,h,m,n,c,p,q,r,t,u,v,w,x,y,g,z){function l(b){const a=new p.ShaderBuilder;var d=b.signedDistanceFieldEnabled;a.include(r.AlignPixel);a.include(v.HUD,b);a.include(t.Slice,b);if(6===b.output)return a.include(w.HUDOcclusionPass,b),a;a.include(u.ScreenSizePerspective);a.fragment.include(z.RgbaFloatEncoding);a.fragment.include(q.ColorConversion);a.include(y.VisualVariables,b);a.varyings.add("vcolor","vec4");a.varyings.add("vtc","vec2");a.varyings.add("vsize","vec2");b.binaryHighlightOcclusionEnabled&&
a.varyings.add("voccluded","float");a.vertex.uniforms.add("screenOffset","vec2").add("anchorPos","vec2").add("textureCoordinateScaleFactor","vec2").add("materialColor","vec4");d&&a.vertex.uniforms.add("outlineColor","vec4");b.screenSizePerspectiveEnabled&&a.vertex.uniforms.add("screenSizePerspective","vec4");(b.debugDrawBorder||b.binaryHighlightOcclusionEnabled)&&a.varyings.add("debugBorderCoords","vec4");a.attributes.add("uv0","vec2");a.attributes.add("color","vec4");a.attributes.add("size","vec2");
a.attributes.add("auxpos2","vec4");a.vertex.code.add(c.glsl`
    void main(void) {
      ProjectHUDAux projectAux;
      vec4 posProj = projectPositionHUD(projectAux);

      if (rejectBySlice(projectAux.posModel)) {
        // Project outside of clip plane
        gl_Position = vec4(1e038, 1e038, 1e038, 1.0);
        return;
      }
      vec2 inputSize;
      ${b.screenSizePerspectiveEnabled?c.glsl`
      inputSize = screenSizePerspectiveScaleVec2(size, projectAux.absCosAngle, projectAux.distanceToCamera, screenSizePerspective);
      vec2 screenOffsetScaled = screenSizePerspectiveScaleVec2(screenOffset, projectAux.absCosAngle, projectAux.distanceToCamera, screenSizePerspectiveAlignment);
         `:c.glsl`
      inputSize = size;
      vec2 screenOffsetScaled = screenOffset;`}

      ${b.vvSize?"inputSize *\x3d vvScale(auxpos2).xx;":""}

      vec2 combinedSize = inputSize * pixelRatio;
      vec4 quadOffset = vec4(0.0);

      ${b.occlusionTestEnabled||b.binaryHighlightOcclusionEnabled?"bool visible \x3d testVisibilityHUD(posProj);":""}

      ${b.binaryHighlightOcclusionEnabled?"voccluded \x3d visible ? 0.0 : 1.0;":""}
    `);var e=c.glsl`
      vec2 uv01 = floor(uv0);
      vec2 uv = uv0 - uv01;
      quadOffset.xy = ((uv01 - anchorPos) * 2.0 * combinedSize + screenOffsetScaled) / viewport.zw * posProj.w;
  `;const A=b.pixelSnappingEnabled?d?c.glsl`
  posProj = alignToPixelOrigin(posProj, viewport.zw) + quadOffset;`:c.glsl`
  posProj += quadOffset;
  if (inputSize.x == size.x) {
    posProj = alignToPixelOrigin(posProj, viewport.zw);
  }`:c.glsl`posProj += quadOffset;`;a.vertex.code.add(c.glsl`
      ${b.occlusionTestEnabled?"if (visible) {":""}
      ${e}
      ${b.vvColor?"vcolor \x3d vvGetColor(auxpos2, vvColorValues, vvColorColors) * materialColor;":"vcolor \x3d color / 255.0 * materialColor;"}

      bool alphaDiscard = vcolor.a < ${c.glsl.float(g.symbolAlphaCutoff)};
      ${d?`alphaDiscard = alphaDiscard && outlineColor.a < ${c.glsl.float(g.symbolAlphaCutoff)};`:""}
      if (alphaDiscard) {
        // "early discard" if both symbol color (= fill) and outline color (if applicable) are transparent
        gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
        return;
      } else {
        ${A}
        gl_Position = posProj;
      }

      vtc = uv * textureCoordinateScaleFactor;

      ${b.debugDrawBorder?"debugBorderCoords \x3d vec4(uv01, 1.5 / combinedSize);":""}
      vsize = inputSize;
      ${b.occlusionTestEnabled?c.glsl`} else { vtc = vec2(0.0);
        ${b.debugDrawBorder?"debugBorderCoords \x3d vec4(0.5, 0.5, 1.5 / combinedSize);}":"}"}`:""}
    }
    `);a.fragment.uniforms.add("tex","sampler2D");d&&(a.fragment.uniforms.add("outlineColor","vec4"),a.fragment.uniforms.add("outlineSize","float"));e=b.debugDrawBorder?c.glsl`(isBorder > 0.0 ? 0.0 : ${c.glsl.float(g.defaultMaskAlphaCutoff)})`:c.glsl.float(g.defaultMaskAlphaCutoff);d=c.glsl`
    ${b.debugDrawBorder?c.glsl`
      float isBorder = float(any(lessThan(debugBorderCoords.xy, debugBorderCoords.zw)) || any(greaterThan(debugBorderCoords.xy, 1.0 - debugBorderCoords.zw)));`:""}

    ${d?c.glsl`
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
          outlineAlphaFactor + fillAlphaFactor < ${e} ||
          fillPixelColor.a + outlinePixelColor.a < ${c.glsl.float(g.symbolAlphaCutoff)}
        ) {
          discard;
        }

        // perform un-premultiplied over operator (see https://en.wikipedia.org/wiki/Alpha_compositing#Description)
        float compositeAlpha = outlinePixelColor.a + fillPixelColor.a * (1.0 - outlinePixelColor.a);
        vec3 compositeColor = vec3(outlinePixelColor) * outlinePixelColor.a +
          vec3(fillPixelColor) * fillPixelColor.a * (1.0 - outlinePixelColor.a);

        gl_FragColor = vec4(compositeColor, compositeAlpha);
      } else {
        if (fillAlphaFactor < ${e}) {
          discard;
        }

        gl_FragColor = premultiplyAlpha(fillPixelColor);
      }

      // visualize SDF:
      // gl_FragColor = vec4(clamp(-dist/vsize.x*2.0, 0.0, 1.0), clamp(dist/vsize.x*2.0, 0.0, 1.0), 0.0, 1.0);
      `:c.glsl`
          vec4 texColor = texture2D(tex, vtc, -0.5);
          if (texColor.a < ${e}) {
            discard;
          }
          gl_FragColor = texColor * premultiplyAlpha(vcolor);
          `}

    ${b.debugDrawBorder?c.glsl`gl_FragColor = mix(gl_FragColor, vec4(1.0, 0.0, 1.0, 1.0), isBorder);`:""}
  `;7===b.output&&a.fragment.code.add(c.glsl`
      void main() {
        ${d}
        gl_FragColor = vec4(gl_FragColor.a);
      }
      `);0===b.output&&a.fragment.code.add(c.glsl`
    void main() {
      ${d}
      ${b.FrontFacePass?"gl_FragColor.rgb /\x3d gl_FragColor.a;":""}
    }
    `);4===b.output&&(a.include(x.OutputHighlight),a.fragment.code.add(c.glsl`
    void main() {
      ${d}
      ${b.binaryHighlightOcclusionEnabled?c.glsl`
          if (voccluded == 1.0) {
            gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);
          } else {
            gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
          }`:"outputHighlight();"}
    }
    `));return a}function k(b,a=B){if(b.textureIsSignedDistanceField){var d=b.anchorPos;b=b.distanceFieldBoundingBox;a[0]=d[0]*(b[2]-b[0])+b[0];a[1]=d[1]*(b[3]-b[1])+b[1]}else n.copy(a,b.anchorPos);return a}(function(b){b.bindUniforms=function(a,d,e){a.setUniform4fv("materialColor",d.color);d.textureIsSignedDistanceField&&(0>=d.outlineColor[3]||0>=d.outlineSize?(a.setUniform4fv("outlineColor",m.ZEROS),a.setUniform1f("outlineSize",0)):(a.setUniform4fv("outlineColor",d.outlineColor),a.setUniform1f("outlineSize",
d.outlineSize)));a.setUniform2f("screenOffset",2*d.screenOffset[0]*e,2*d.screenOffset[1]*e);a.setUniform2fv("anchorPos",k(d))}})(f.HUDMaterial||(f.HUDMaterial={}));const B=h.create();h=Object.freeze({__proto__:null,build:l,get HUDMaterial(){return f.HUDMaterial},calculateAnchorPosForRendering:k});f.HUDMaterialShader=h;f.build=l;f.calculateAnchorPosForRendering=k});