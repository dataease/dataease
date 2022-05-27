// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder","../views/3d/webgl-engine/core/shaderLibrary/ScreenSpacePass"],function(b,d,e,f){function c(){const a=new e.ShaderBuilder;a.include(f.ScreenSpacePass);a.fragment.uniforms.add("tex","sampler2D");a.fragment.uniforms.add("color","vec4");a.fragment.code.add(d.glsl`
    void main() {
      vec4 texColor = texture2D(tex, uv);
      gl_FragColor = texColor * color;
    }
  `);return a}var g=Object.freeze({__proto__:null,build:c});b.TextureOnlyShader=g;b.build=c});