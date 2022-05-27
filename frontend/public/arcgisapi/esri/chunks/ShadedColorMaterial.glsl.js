// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl ../views/3d/webgl-engine/core/shaderLibrary/ScreenSizeScaling.glsl".split(" "),function(d,b,f,g,h,k,l,m){function e(c){const a=
new g.ShaderBuilder;a.include(f.Transform,{linearDepth:!1});a.include(m.ScreenSizeScaling.builder,{});a.include(k.Slice,c);a.fragment.include(h.ColorConversion);a.vertex.uniforms.add("proj","mat4").add("view","mat4");a.fragment.uniforms.add("color","vec4");a.attributes.add("position","vec3");a.varyings.add("vWorldPosition","vec3");c.screenSizeEnabled&&a.attributes.add("offset","vec3");c.shadingEnabled&&(a.vertex.uniforms.add("viewNormal","mat4"),a.fragment.uniforms.add("shadedColor","vec4").add("shadingDirection",
"vec3"),a.attributes.add("normal","vec3"),a.varyings.add("vViewNormal","vec3"));a.vertex.code.add(b.glsl`
    void main(void) {
      vWorldPosition = ${c.screenSizeEnabled?"screenSizeScaling(offset, position)":"position"};
  `);c.shadingEnabled&&a.vertex.code.add(b.glsl`
      vec3 worldNormal = normal;
      vViewNormal = (viewNormal * vec4(worldNormal, 1)).xyz;
    `);a.vertex.code.add(b.glsl`
    gl_Position = transformPosition(proj, view, vWorldPosition);
  }
  `);a.fragment.code.add(b.glsl`
    void main() {
      discardBySlice(vWorldPosition);
    `);c.shadingEnabled?a.fragment.code.add(b.glsl`
      vec3 viewNormalNorm = normalize(vViewNormal);
      float shadingFactor = 1.0 - clamp(-dot(viewNormalNorm, shadingDirection), 0.0, 1.0);
      vec4 finalColor = mix(color, shadedColor, shadingFactor);
    `):a.fragment.code.add(b.glsl`
      vec4 finalColor = color;
    `);a.fragment.code.add(b.glsl`
      if (finalColor.a < ${b.glsl.float(l.symbolAlphaCutoff)}) {
        discard;
      }
      ${7===c.output?b.glsl`gl_FragColor = vec4(finalColor.a);`:""}

      ${0===c.output?b.glsl`gl_FragColor = highlightSlice(finalColor, vWorldPosition); ${c.OITEnabled?"gl_FragColor \x3d premultiplyAlpha(gl_FragColor);":""}`:""}
    }
    `);return a}var n=Object.freeze({__proto__:null,build:e});d.ShadedColorMaterialShader=n;d.build=e});