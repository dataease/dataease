// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder"],function(b,c,e){function d(){const a=new e.ShaderBuilder;a.attributes.add("position","vec2");a.attributes.add("uv0","vec2");a.vertex.uniforms.add("scale","float");a.vertex.uniforms.add("offset","vec2");a.varyings.add("uv","vec2");a.vertex.code.add(c.glsl`
    void main(void) {
      gl_Position = vec4(position, 0.0, 1.0);
      uv = uv0 * scale + offset;
    }
  `);a.fragment.uniforms.add("tex","sampler2D");a.fragment.uniforms.add("opacity","float");a.fragment.code.add(c.glsl`
    void main() {
      vec4 color = texture2D(tex, uv);

      // Note: output in pre-multiplied alpha for correct alpha compositing
      gl_FragColor = vec4(color.xyz, 1.0) * color.a * opacity;
    }
  `);return a}var f=Object.freeze({__proto__:null,build:d});b.BlendLayersShader=f;b.build=d});