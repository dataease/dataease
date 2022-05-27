// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/output/OutputHighlight.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/ReadShadowMap.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/WaterDistortion.glsl ../views/3d/webgl-engine/core/shaderLibrary/ForwardLinearDepth.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/NormalUtils.glsl ../views/3d/webgl-engine/core/shaderLibrary/shading/Water.glsl".split(" "),
function(f,c,l,m,n,d,p,e,q,g,r,h,t){function k(b){const a=new m.ShaderBuilder;a.include(l.Transform,{linearDepth:!1});a.attributes.add("position","vec3");a.attributes.add("uv0","vec2");a.vertex.uniforms.add("proj","mat4").add("view","mat4").add("localOrigin","vec3");a.vertex.uniforms.add("waterColor","vec4");if(0===b.output||7===b.output)a.include(h.NormalUtils,b),a.include(r.ForwardLinearDepth,b),a.varyings.add("vuv","vec2"),a.varyings.add("vpos","vec3"),a.varyings.add("vnormal","vec3"),a.varyings.add("vtbnMatrix",
"mat3"),a.vertex.code.add(c.glsl`
      void main(void) {
        if (waterColor.a < ${c.glsl.float(e.symbolAlphaCutoff)}) {
          // Discard this vertex
          gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
          return;
        }

        vuv = uv0;
        vpos = position;

        vnormal = getLocalUp(vpos, localOrigin);
        vtbnMatrix = getTBNMatrix(vnormal);

        gl_Position = transformPosition(proj, view, vpos);
        ${0===b.output?"forwardLinearDepth();":""}
      }
    `);7===b.output&&(a.include(d.Slice,b),a.fragment.uniforms.add("waterColor","vec4"),a.fragment.code.add(c.glsl`
        void main() {
          discardBySlice(vpos);

          gl_FragColor = vec4(waterColor.a);
        }
      `));0===b.output&&(a.include(g.WaterDistortion,b),a.include(d.Slice,b),b.receiveShadows&&a.include(q.ReadShadowMap,b),a.include(t.Water,b),a.fragment.uniforms.add("waterColor","vec4").add("lightingMainDirection","vec3").add("lightingMainIntensity","vec3").add("camPos","vec3").add("timeElapsed","float").add("view","mat4"),a.fragment.include(n.ColorConversion),a.fragment.code.add(c.glsl`
      void main() {
        discardBySlice(vpos);
        vec3 localUp = vnormal;
        // the created normal is in tangent space
        vec4 tangentNormalFoam = getSurfaceNormalAndFoam(vuv, timeElapsed);

        // we rotate the normal according to the tangent-bitangent-normal-Matrix
        vec3 n = normalize(vtbnMatrix * tangentNormalFoam.xyz);
        vec3 v = -normalize(vpos - camPos);
        vec3 l = normalize(-lightingMainDirection);
        float shadow = ${b.receiveShadows?c.glsl`1.0 - readShadowMap(vpos, linearDepth)`:"1.0"};
        vec4 vPosView = view*vec4(vpos, 1.0);
        vec4 final = vec4(getSeaColor(n, v, l, waterColor.rgb, lightingMainIntensity, localUp, shadow, tangentNormalFoam.w, vPosView.xyz), waterColor.w);

        // gamma correction
        gl_FragColor = delinearizeGamma(final);
        gl_FragColor = highlightSlice(gl_FragColor, vpos);
        ${b.OITEnabled?"gl_FragColor \x3d premultiplyAlpha(gl_FragColor);":""}
      }
    `));2===b.output&&(a.include(h.NormalUtils,b),a.include(g.WaterDistortion,b),a.include(d.Slice,b),a.varyings.add("vpos","vec3"),a.varyings.add("vuv","vec2"),a.vertex.code.add(c.glsl`
        void main(void) {
          if (waterColor.a < ${c.glsl.float(e.symbolAlphaCutoff)}) {
            // Discard this vertex
            gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
            return;
          }

          vuv = uv0;
          vpos = position;

          gl_Position = transformPosition(proj, view, vpos);
        }
    `),a.fragment.uniforms.add("timeElapsed","float"),a.fragment.code.add(c.glsl`
        void main() {
          discardBySlice(vpos);

          // the created normal is in tangent space and foam
          vec4 tangentNormalFoam = getSurfaceNormalAndFoam(vuv, timeElapsed);
          tangentNormalFoam.xyz = normalize(tangentNormalFoam.xyz);

          gl_FragColor = vec4((tangentNormalFoam.xyz + vec3(1.0)) * 0.5, tangentNormalFoam.w);
        }
    `));5===b.output&&(a.varyings.add("vpos","vec3"),a.vertex.code.add(c.glsl`
        void main(void) {
          if (waterColor.a < ${c.glsl.float(e.symbolAlphaCutoff)}) {
            // Discard this vertex
            gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
            return;
          }

          vpos = position;
          gl_Position = transformPosition(proj, view, vpos);
        }
    `),a.fragment.uniforms.add("waterColor","vec4"),a.fragment.code.add(c.glsl`
        void main() {
          gl_FragColor = waterColor;
        }
    `));4===b.output&&(a.include(p.OutputHighlight),a.varyings.add("vpos","vec3"),a.vertex.code.add(c.glsl`
      void main(void) {
        if (waterColor.a < ${c.glsl.float(e.symbolAlphaCutoff)}) {
          // Discard this vertex
          gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
          return;
        }

        vpos = position;
        gl_Position = transformPosition(proj, view, vpos);
      }
    `),a.include(d.Slice,b),a.fragment.code.add(c.glsl`
      void main() {
        discardBySlice(vpos);
        outputHighlight();
      }
    `));return a}var u=Object.freeze({__proto__:null,build:k});f.WaterSurface=u;f.build=k});