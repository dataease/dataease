// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputHighlight.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl ../views/3d/webgl-engine/core/shaderLibrary/attributes/VertexColor.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputDepth.glsl".split(" "),
function(f,b,h,k,l,m,n,p,q,r){function g(c){const a=new k.ShaderBuilder;c.draped||a.extensions.add("GL_OES_standard_derivatives");const d=1===c.output;a.include(h.Transform,{linearDepth:d});a.include(q.VertexColor,c);a.vertex.uniforms.add("proj","mat4");a.vertex.uniforms.add("view","mat4");d&&(a.include(r.OutputDepth,c),a.vertex.uniforms.add("nearFar","vec2"),a.varyings.add("linearDepth","float"));c.draped?a.vertex.uniforms.add("worldToScreenRatio","float"):(a.vertex.uniforms.add("worldToScreenPerDistanceRatio",
"float"),a.vertex.uniforms.add("camPos","vec3"),a.attributes.add("bound1","vec3"),a.attributes.add("bound2","vec3"),a.attributes.add("bound3","vec3"));a.attributes.add("position","vec3");a.attributes.add("uvMapSpace","vec4");a.varyings.add("vpos","vec3");a.varyings.add("vuv","vec2");const e=3===c.style||4===c.style||5===c.style;e&&a.vertex.code.add(b.glsl`
      const mat2 rotate45 = mat2(${b.glsl.float(.70710678118)}, ${b.glsl.float(-.70710678118)},
                                 ${b.glsl.float(.70710678118)}, ${b.glsl.float(.70710678118)});
    `);c.draped||(a.vertex.code.add(b.glsl`
      vec3 projectPointToLineSegment(vec3 center, vec3 halfVector, vec3 point) {
        float projectedLength = dot(halfVector, point - center) / dot(halfVector, halfVector);
        return center + halfVector * clamp(projectedLength, -1.0, 1.0);
      }
    `),a.vertex.code.add(b.glsl`
      vec3 intersectRayPlane(vec3 rayDir, vec3 rayOrigin, vec3 planeNormal, vec3 planePoint) {
        float d = dot(planeNormal, planePoint);
        float t = (d - dot(planeNormal, rayOrigin)) / dot(planeNormal, rayDir);

        return rayOrigin + t * rayDir;
      }
    `),a.vertex.code.add(b.glsl`
      float boundingRectDistanceToCamera() {
        vec3 halfU = (bound2 - bound1) * 0.5;
        vec3 halfV = (bound3 - bound1) * 0.5;
        vec3 center = bound1 + halfU + halfV;
        vec3 n = normalize(cross(halfU, halfV));

        vec3 viewDir = - vec3(view[0][2], view[1][2], view[2][2]);

        float viewAngle = dot(viewDir, n);
        float minViewAngle = ${b.glsl.float(.08715574274)};

        if (abs(viewAngle) < minViewAngle) {
          // view direction is (almost) parallel to plane -> clamp it to min angle
          float normalComponent = sign(viewAngle) * minViewAngle - viewAngle;
          viewDir = normalize(viewDir + normalComponent * n);
        }

        // intersect view direction with infinite plane that contains bounding rect
        vec3 planeProjected = intersectRayPlane(viewDir, camPos, n, center);

        // clip to bounds by projecting to u and v line segments individually
        vec3 uProjected = projectPointToLineSegment(center, halfU, planeProjected);
        vec3 vProjected = projectPointToLineSegment(center, halfV, planeProjected);

        // use to calculate the closest point to camera on bounding rect
        vec3 closestPoint = uProjected + vProjected - center;

        return length(closestPoint - camPos);
      }
    `));a.vertex.code.add(b.glsl`
    vec2 scaledUV() {
      vec2 uv = uvMapSpace.xy ${e?" * rotate45":""};
      vec2 uvCellOrigin = uvMapSpace.zw ${e?" * rotate45":""};

      ${c.draped?b.glsl`
            float ratio = worldToScreenRatio;
          `:b.glsl`
            float distanceToCamera = boundingRectDistanceToCamera();
            float ratio = worldToScreenPerDistanceRatio / distanceToCamera;

            // Logarithmically discretize ratio to avoid jittering
            float step = 0.1;
            ratio = log(ratio);
            ratio = ceil(ratio / step) * step;
            ratio = exp(ratio);
          `}

      vec2 uvOffset = mod(uvCellOrigin * ratio, ${b.glsl.float(c.patternSpacing)});
      return uvOffset + (uv * ratio);
    }
  `);a.vertex.code.add(b.glsl`
    void main(void) {
      vuv = scaledUV();
      vpos = position;
      forwardNormalizedVertexColor();
      gl_Position = ${d?b.glsl`transformPositionWithDepth(proj, view, vpos, nearFar, linearDepth);`:b.glsl`transformPosition(proj, view, vpos);`}
    }
  `);a.include(m.Slice,c);a.fragment.include(l.ColorConversion);a.fragment.uniforms.add("matColor","vec4");c.draped&&a.fragment.uniforms.add("texelSize","float");4===c.output&&a.include(n.OutputHighlight);4!==c.output&&(a.fragment.code.add(b.glsl`
      const float lineWidth = ${b.glsl.float(c.lineWidth)};
      const float spacing = ${b.glsl.float(c.patternSpacing)};
      const float spacingINV = ${b.glsl.float(1/c.patternSpacing)};

      float coverage(float p, float txlSize) {
        p = mod(p, spacing);

        float halfTxlSize = txlSize / 2.0;

        float start = p - halfTxlSize;
        float end = p + halfTxlSize;

        float coverage = (ceil(end * spacingINV) - floor(start * spacingINV)) * lineWidth;
        coverage -= min(lineWidth, mod(start, spacing));
        coverage -= max(lineWidth - mod(end, spacing), 0.0);

        return coverage / txlSize;
      }
    `),c.draped||a.fragment.code.add(b.glsl`
        const int maxSamples = 5;

        float sample(float p) {
          vec2 dxdy = abs(vec2(dFdx(p), dFdy(p)));
          float fwidth = dxdy.x + dxdy.y;

          ivec2 samples = 1 + ivec2(clamp(dxdy, 0.0, float(maxSamples - 1)));
          vec2 invSamples = 1.0 / vec2(samples);

          float accumulator = 0.0;

          for (int j = 0; j < maxSamples; j++) {
            if(j >= samples.y) {
              break;
            }

            for (int i = 0; i < maxSamples; i++) {
              if(i >= samples.x) {
                break;
              }

              vec2 step = vec2(i,j) * invSamples - 0.5;
              accumulator += coverage(p + step.x * dxdy.x + step.y * dxdy.y, fwidth);
            }
          }

          accumulator /= float(samples.x * samples.y);
          return accumulator;
        }
      `));a.fragment.code.add(b.glsl`
    void main() {
      discardBySlice(vpos);
      vec4 color = ${c.attributeColor?"vColor * matColor;":"matColor;"}
      color = highlightSlice(color, vpos);

      ${4!==c.output?b.glsl`color.a *= ${t(c)};`:""}

      if (color.a < ${b.glsl.float(p.symbolAlphaCutoff)}) {
        discard;
      }

      ${7===c.output?b.glsl`gl_FragColor = vec4(color.a);`:""}

      ${0===c.output?b.glsl`gl_FragColor = color; ${c.OITEnabled?"gl_FragColor \x3d premultiplyAlpha(gl_FragColor);":""}`:""}
      ${4===c.output?b.glsl`outputHighlight();`:""}
      ${1===c.output?b.glsl`outputDepth(linearDepth);`:""};
    }
  `);return a}function t(c){function a(d){return c.draped?b.glsl`coverage(vuv.${d}, texelSize)`:b.glsl`sample(vuv.${d})`}switch(c.style){case 3:case 0:return a("y");case 4:case 1:return a("x");case 5:case 2:return b.glsl`
        1.0 - (1.0 - ${a("x")}) * (1.0 - ${a("y")})
      `;default:return"0.0"}}var u=Object.freeze({__proto__:null,build:g});f.PatternShader=u;f.build=g});