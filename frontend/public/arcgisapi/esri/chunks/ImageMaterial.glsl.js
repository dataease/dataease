// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderLibrary/Transform.glsl ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl ../views/3d/webgl-engine/core/shaderLibrary/Slice.glsl ../views/3d/webgl-engine/core/shaderLibrary/util/AlphaDiscard.glsl".split(" "),function(d,b,g,h,k,l,e){function f(c){const a=new h.ShaderBuilder;a.include(g.Transform,{linearDepth:!1});a.vertex.uniforms.add("proj",
"mat4").add("view","mat4");a.attributes.add("position","vec3");a.attributes.add("uv0","vec2");a.varyings.add("vpos","vec3");a.vertex.uniforms.add("textureCoordinateScaleFactor","vec2");a.vertex.code.add(b.glsl`
    void main(void) {
      vpos = position;
      vTexCoord = uv0 * textureCoordinateScaleFactor;
      gl_Position = transformPosition(proj, view, vpos);
    }
  `);a.include(l.Slice,c);a.fragment.uniforms.add("tex","sampler2D");a.fragment.uniforms.add("opacity","float");a.varyings.add("vTexCoord","vec2");7===c.output?a.fragment.code.add(b.glsl`
    void main() {
      discardBySlice(vpos);

      float alpha = texture2D(tex, vTexCoord).a * opacity;
      if (alpha  < ${b.glsl.float(e.defaultMaskAlphaCutoff)}) {
        discard;
      }

      gl_FragColor = vec4(alpha);
    }
    `):(a.fragment.include(k.ColorConversion),a.fragment.code.add(b.glsl`
    void main() {
      discardBySlice(vpos);
      gl_FragColor = texture2D(tex, vTexCoord) * opacity;

      if (gl_FragColor.a < ${b.glsl.float(e.defaultMaskAlphaCutoff)}) {
        discard;
      }

      gl_FragColor = highlightSlice(gl_FragColor, vpos);
      ${c.OITEnabled?"gl_FragColor \x3d premultiplyAlpha(gl_FragColor);":""}
    }
    `));return a}var m=Object.freeze({__proto__:null,build:f});d.ImageMaterialShader=m;d.build=f});