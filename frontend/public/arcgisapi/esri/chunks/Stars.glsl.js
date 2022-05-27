// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder","../views/3d/webgl-engine/core/shaderLibrary/hud/AlignPixel.glsl"],function(b,c,e,f){function d(){const a=new e.ShaderBuilder;a.attributes.add("position","vec3");a.attributes.add("color","vec4");a.attributes.add("size","float");a.varyings.add("vcolor","vec4");a.varyings.add("vsize","float");a.vertex.uniforms.add("transform","mat4").add("viewport","vec4").add("pixelRatio",
"float");a.include(f.AlignPixel);a.vertex.code.add(c.glsl`
    void main(void) {
      vec4 posProj = transform * vec4(position, 0);
      gl_Position = alignToPixelCenter(posProj, viewport.zw);
      vcolor = color / 1.2;
      vsize = size * 5.0 * pixelRatio;
      gl_PointSize = vsize;
    }
  `);a.fragment.code.add(c.glsl`
    void main() {
      float cap = 0.7;
      float scale = 1.0 / cap;
      float helper = clamp(length(abs(gl_PointCoord - vec2(0.5))), 0.0, cap);
      float alpha = clamp((cap - helper) * scale, 0.0, 1.0);
      float intensity = alpha * alpha * alpha;
      if (vsize < 3.0) {
        intensity *= 0.5;
      }
      gl_FragColor = vec4(vcolor.xyz, intensity);
    }
  `);return a}var g=Object.freeze({__proto__:null,build:d});b.StarsShader=g;b.build=d});