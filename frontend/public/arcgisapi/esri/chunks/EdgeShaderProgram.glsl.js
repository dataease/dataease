// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/AdjustProjectedPosition.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/DiscardByCoverage.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/DiscardNonSilhouetteEdges.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/DiscardShortEdges.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/EdgeUtil.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/UnpackAttributes.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/LineAmplitude.glsl ../views/3d/webgl-engine/shaders/sources/edgeRenderer/LineOffset.glsl".split(" "),
function(e,d,h,k,l,m,n,p,q,r,t,u){function f(b){const a=new h.ShaderBuilder,c=a.vertex,v=a.fragment;b.legacy&&c.uniforms.add("uModel","mat4");b.antialiasing&&(c.code.add(d.glsl`
      #define ANTIALIASING 1
    `),v.code.add(d.glsl`
      #define ANTIALIASING 1
    `));a.include(l.AdjustProjectedPosition,b);a.include(t.LineAmplitude,b);a.include(q.EdgeUtil,b);a.include(r.UnpackAttributes,b);a.include(u.LineOffset,b);a.include(k.Slice,b);a.include(n.DiscardNonSilhouetteEdges,b);a.include(m.DiscardByCoverage,b);a.include(p.DiscardShortEdges,b);a.varyings.add("vColor","vec4");a.varyings.add("vRadius","float");a.varyings.add("vPosition","vec3");a.varyings.add("vWorldPosition","vec3");a.varyings.add("vLineLengthPixels","float");a.varyings.add("vSizeFalloffFactor",
"float");c.uniforms.add("uPixelToNDC","vec2");c.uniforms.add("uNDCToPixel","vec2");c.uniforms.add("uPixelRatio","float");a.attributes.add("position0","vec3");a.attributes.add("position1","vec3");a.attributes.add("variantOffset","float");a.attributes.add("variantStroke","float");a.attributes.add("variantExtension","float");c.code.add(d.glsl`
    const float opaqueCutoff = 1.0 / 255.0;

    void calculateGeometricOutputs(vec3 viewPosV0, vec3 viewPosV1, vec3 worldPosV0, vec3 worldPosV1, vec3 worldNormal, UnpackedAttributes unpackedAttributes) {
      vec2 sideness = unpackedAttributes.sideness;
      vec2 sidenessNorm = unpackedAttributes.sidenessNorm;

      vWorldPosition = mix(worldPosV0, worldPosV1, sidenessNorm.y).xyz;

      vec3 viewPos = mix(viewPosV0, viewPosV1, sidenessNorm.y);

      vec4 projPosV0 = projFromViewPosition(viewPosV0);
      vec4 projPosV1 = projFromViewPosition(viewPosV1);
      vec4 projPos = projFromViewPosition(viewPos);

      vec3 screenSpaceLineNDC = (projPosV1.xyz / projPosV1.w - projPosV0.xyz / projPosV0.w);
      vec2 screenSpaceLinePixels = screenSpaceLineNDC.xy * uNDCToPixel;
      float lineLengthPixels = length(screenSpaceLinePixels);

      float dzPerPixel = screenSpaceLineNDC.z / lineLengthPixels;
      vec2 screenSpaceDirection = screenSpaceLinePixels / lineLengthPixels;
      vec2 perpendicularScreenSpaceDirection = vec2(screenSpaceDirection.y, -screenSpaceDirection.x) * sideness.x;

      float falloffFactor = distanceBasedPerspectiveFactor(-viewPos.z) * uPixelRatio;
      float lineWidthPixels = unpackedAttributes.lineWidthPixels * falloffFactor;

      float extensionLengthPixels = calculateExtensionLength(unpackedAttributes.extensionLengthPixels, lineLengthPixels) * falloffFactor;
      float lineAmplitudePixels = calculateLineAmplitude(unpackedAttributes) * uPixelRatio;

      vSizeFalloffFactor = falloffFactor;

      float lineWidthAndAmplitudePixels = lineWidthPixels + lineAmplitudePixels + lineAmplitudePixels;
      float extendedLineLengthPixels = lineLengthPixels + extensionLengthPixels + extensionLengthPixels;

    #ifdef ANTIALIASING
      const float aaPaddingPixels = 1.0;

      // Line size with padding
      float halfAAPaddedLineWidthAndAmplitudePixels = lineWidthAndAmplitudePixels * 0.5 + aaPaddingPixels;
      float aaPaddedRoundedCapSizePixels = lineWidthPixels * 0.5 + aaPaddingPixels;
    #else /* ANTIALIASING */

      // Even if there is no AA, we still want to do proper <1px rendering,
      // so we effectively clamp the pixel sizes to minimum of 1px and compute
      // coverage in the fragment shader
      float halfAAPaddedLineWidthAndAmplitudePixels = max(lineWidthAndAmplitudePixels, 1.0) * 0.5;
      float aaPaddedRoundedCapSizePixels = max(lineWidthPixels, 1.0) * 0.5;
    #endif /* ANTIALIASING */

      // Half line width in NDC including padding for anti aliasing
      vec2 halfAAPaddedLineWidthAndAmplitudeNDC = halfAAPaddedLineWidthAndAmplitudePixels * uPixelToNDC;
      vec2 aaPaddedRoundedCapSizeNDC = aaPaddedRoundedCapSizePixels * uPixelToNDC;
      vec2 extensionLengthNDC = extensionLengthPixels * uPixelToNDC;

      // Compute screen space position of vertex, offsetting for line size and end caps
      vec2 ndcOffset = (
          screenSpaceDirection * sideness.y * (aaPaddedRoundedCapSizeNDC + extensionLengthNDC)
        + perpendicularScreenSpaceDirection * halfAAPaddedLineWidthAndAmplitudeNDC
      );

      projPos.xy += ndcOffset * projPos.w;
      projPos.z += (dzPerPixel * (aaPaddedRoundedCapSizePixels + extensionLengthPixels)) * sideness.y * projPos.w;

      projPos = adjustProjectedPosition(projPos, worldNormal, 1.0 + max((lineWidthAndAmplitudePixels - 1.0) * 0.5, 0.0));

      // Line length with end caps
      float aaPaddedLineWithCapsLengthPixels = extendedLineLengthPixels + aaPaddedRoundedCapSizePixels + aaPaddedRoundedCapSizePixels;

      float pixelPositionAlongLine = aaPaddedLineWithCapsLengthPixels * sidenessNorm.y - aaPaddedRoundedCapSizePixels;

      // Position in pixels with origin at first vertex of line segment
      vPosition = vec3(
        halfAAPaddedLineWidthAndAmplitudePixels * sideness.x,
        pixelPositionAlongLine,
        pixelPositionAlongLine / extendedLineLengthPixels
      );

      // The line width radius in pixels
      vRadius = lineWidthPixels * 0.5;
      vLineLengthPixels = extendedLineLengthPixels;

      // discard edges below a certain length threshold
      discardShortEdges(unpackedAttributes, lineLengthPixels);

      gl_Position = projPos;
    }

    void main() {
      ComponentData component = readComponentData();
      UnpackedAttributes unpackedAttributes = unpackAttributes(component);

      vec3 worldPosV0 = worldFromModelPosition(position0);
      vec3 worldPosV1 = worldFromModelPosition(position1);
      vec3 viewPosV0 = viewFromModelPosition(position0);
      vec3 viewPosV1 = viewFromModelPosition(position1);

      // Component color
      vColor = component.color;

      // Discard fully transparent edges
      if (vColor.a < opaqueCutoff) {
        gl_Position = vec4(10.0, 10.0, 10.0, 1.0);
        return;
      }

      if (discardNonSilhouetteEdges(viewPosV0, worldPosV0)) {
        return;
      }

      // General geometric computation for all types of edges
      calculateGeometricOutputs(viewPosV0, viewPosV1, worldPosV0, worldPosV1, worldNormal(), unpackedAttributes);

      // Specific computation for different edge styles
      calculateStyleOutputs(unpackedAttributes);
    }
  `);a.fragment.code.add(d.glsl`
    vec2 lineWithCapsDistance(float radius, vec2 position, float lineLength) {
      float lineOffset = calculateLineOffset();
      float positionX = position.x - lineOffset;

      if (radius < 1.0) {
        // Handle this specifically for subpixel sizes:
        // 1. Compute correct coverage (note coverage is computed by
        //    0.5 - dist, so we make sure that that will lead to correct
        //    subpixel coverage
        // 2. Ignore rounded caps
        float coverageX = clamp(min(radius, positionX + 0.5) - max(-radius, positionX - 0.5), 0.0, 1.0);
        float coverageY = clamp(min(lineLength, position.y + 0.5) - max(0.0, position.y - 0.5), 0.0, 1.0);

        float coverage = min(coverageX, coverageY);

        return vec2(0.5 - coverage, 0.0);
      }
      else {
        // Between -radius -> 0 for start cap, 0 for line, 0 -> radius
        float positionOnCap = position.y - clamp(position.y, 0.0, lineLength);

        vec2 lineToPosition = vec2(positionX, positionOnCap);
        return vec2(length(lineToPosition) - radius, positionOnCap / radius);
      }
    }

    void main() {
      float radius = vRadius * calculateLinePressure();

      vec2 distance = lineWithCapsDistance(radius, vPosition.xy, vLineLengthPixels);
      float coverage = clamp(0.5 - distance.x, 0.0, 1.0);

      discardByCoverage(radius, coverage);
      discardBySlice(vWorldPosition);

      float alpha = vColor.a * coverage;

      gl_FragColor = vec4(vColor.rgb, alpha);

    }
  `);return a}const g={position0:0,position1:1,componentIndex:2,packedAttributes:3,variantOffset:4,variantStroke:5,variantExtension:6,normal:7,normalA:7,normalB:8,sideness:9};var w=Object.freeze({__proto__:null,attributeLocations:g,build:f});e.EdgeShaderProgram=w;e.attributeLocations=g;e.build=f});