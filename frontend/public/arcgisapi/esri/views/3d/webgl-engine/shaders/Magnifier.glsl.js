// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../core/shaderModules/interfaces","../core/shaderModules/ShaderBuilder"],function(b,c,d){b.build=function(){const a=new d.ShaderBuilder;a.attributes.add("position","vec2");a.vertex.uniforms.add("proj","mat4");a.vertex.uniforms.add("drawPosition","vec4");a.varyings.add("vUV","vec2");a.vertex.code.add(c.glsl`
    void main(void) {
      vUV = position;
      gl_Position = vec4(drawPosition.xy + vec2(position - 0.5) * drawPosition.zw, 0.0, 1.0);
    }
  `);a.fragment.uniforms.add("textureInput","sampler2D");a.fragment.uniforms.add("textureMask","sampler2D");a.fragment.uniforms.add("textureOverlay","sampler2D");a.fragment.uniforms.add("maskEnabled","bool");a.fragment.uniforms.add("overlayEnabled","bool");a.fragment.code.add(c.glsl`
    const float barrelFactor = 1.1;

    vec2 barrel(vec2 uv) {
      vec2 uvn = uv * 2.0 - 1.0;

      if (uvn.x == 0.0 && uvn.y == 0.0) {
        return vec2(0.5, 0.5);
      }

      float theta = atan(uvn.y, uvn.x);
      float r = pow(length(uvn), barrelFactor);
      return r * vec2(cos(theta), sin(theta)) * 0.5 + 0.5;
    }

    void main() {
      float mask = maskEnabled ? texture2D(textureMask, vUV).a : 1.0;
      vec4 inputColor = texture2D(textureInput, barrel(vUV)) * mask;
      vec4 overlayColor = overlayEnabled ? texture2D(textureOverlay, vUV) : vec4(0);

      // Premultiply
      overlayColor.rgb *= overlayColor.a;

      gl_FragColor = overlayColor + (1.0 - overlayColor.a) * inputColor;
    }
  `);return a};Object.defineProperty(b,"__esModule",{value:!0})});