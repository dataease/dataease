// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder"],function(b,c,e){function d(){const a=new e.ShaderBuilder;a.extensions.add("GL_OES_standard_derivatives");a.vertex.uniforms.add("proj","mat4").add("view","mat4");a.attributes.add("position","vec3");a.attributes.add("uv0","vec2");a.varyings.add("vUV","vec2");a.vertex.code.add(c.glsl`
    void main(void) {
      vUV = uv0;
      gl_Position = proj * view * vec4(position, 1.0);
    }
  `);a.fragment.uniforms.add("backgroundColor","vec4").add("gridColor","vec4").add("ratio","float").add("gridWidth","float");a.fragment.code.add(c.glsl`
    void main() {
      const float LINE_WIDTH = 1.0;

      vec2 uvScaled = vUV * gridWidth;
      vec2 gridUV = (fract(uvScaled + 0.5) - 0.5) / (LINE_WIDTH * fwidth(uvScaled));
      vec2 grid = (1.0 - step(0.5, gridUV)) * step(-0.5, gridUV);

      // mask aliasing along edges
      grid.x *= step(0.5, uvScaled.x) * step(uvScaled.x, gridWidth - 0.5);
      grid.y *= step(0.5, uvScaled.y) * step(uvScaled.y, gridWidth - 0.5);

      float gridFade = max(grid.x, grid.y);

      float gridAlpha = gridColor.a * gridFade;

      // premultiply alpha in output
      gl_FragColor =
        vec4(backgroundColor.rgb * backgroundColor.a, backgroundColor.a) * (1.0 - gridAlpha) +
        vec4(gridColor.rgb, 1.0) * gridAlpha;
    }
  `);return a}var f=Object.freeze({__proto__:null,build:d});b.SlicePlaneMaterialShader=f;b.build=d});