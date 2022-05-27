// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder","../views/3d/webgl-engine/core/shaderLibrary/util/ColorConversion.glsl"],function(b,c,e,f){function d(g){const a=new e.ShaderBuilder;a.extensions.add("GL_OES_standard_derivatives");a.attributes.add("position","vec3");a.attributes.add("uv0","vec2");a.vertex.uniforms.add("proj","mat4").add("view","mat4");a.varyings.add("vUV","vec2");a.vertex.code.add(c.glsl`
    void main(void) {
      vUV = uv0;
      gl_Position = proj * view * vec4(position.xyz, 1.0);
    }
  `);a.fragment.uniforms.add("size","vec2").add("color1","vec4").add("color2","vec4");a.fragment.include(f.ColorConversion);a.fragment.code.add(c.glsl`
    void main() {
      vec2 uvScaled = vUV / (2.0 * size);

      vec2 uv = fract(uvScaled - 0.25);
      vec2 ab = clamp((abs(uv - 0.5) - 0.25) / fwidth(uvScaled), -0.5, 0.5);
      float fade = smoothstep(0.25, 0.5, max(fwidth(uvScaled.x), fwidth(uvScaled.y)));
      float t = mix(abs(ab.x + ab.y), 0.5, fade);

      gl_FragColor = mix(color2, color1, t);
      ${g.OITEnabled?"gl_FragColor \x3d premultiplyAlpha(gl_FragColor);":""}
    }
  `);return a}var h=Object.freeze({__proto__:null,build:d});b.CheckerBoardShader=h;b.build=d});