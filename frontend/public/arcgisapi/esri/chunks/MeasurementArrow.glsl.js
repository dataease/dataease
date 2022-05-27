// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../views/3d/webgl-engine/core/shaderModules/interfaces","../views/3d/webgl-engine/core/shaderModules/ShaderBuilder"],function(c,b,e){function d(){const a=new e.ShaderBuilder;a.vertex.uniforms.add("proj","mat4").add("view","mat4").add("width","float");a.attributes.add("position","vec3");a.attributes.add("normal","vec3");a.attributes.add("uv0","vec2");a.attributes.add("auxpos1","float");a.varyings.add("vtc","vec2");a.varyings.add("vlength","float");a.varyings.add("vradius","float");
a.vertex.code.add(b.glsl`
    void main(void) {
      vec3 bitangent = normal;

      vtc = uv0;
      vlength = auxpos1;
      vradius = 0.5 * width;

      vec4 pos = view * vec4(position + vradius * bitangent * uv0.y, 1.0);
      gl_Position = proj * pos;
    }
  `);a.fragment.uniforms.add("outlineSize","float").add("outlineColor","vec4").add("stripeLength","float").add("stripeEvenColor","vec4").add("stripeOddColor","vec4");a.fragment.code.add(b.glsl`
    const float INV_SQRT2 = ${b.glsl.float(1/Math.sqrt(2))};

    vec4 arrowColor(vec2 tc, float len) {
      float d = INV_SQRT2 * (tc.x - abs(tc.y));
      d = min(d, INV_SQRT2 * (len - tc.x - abs(tc.y)));
      d = min(d, 1.0 - abs(tc.y));

      if (d < 0.0) {
        return vec4(0.0);
      } else if (d < outlineSize) {
        return outlineColor;
      } else {
        return fract(0.5 / stripeLength * tc.x * vradius) >= 0.5 ? stripeOddColor : stripeEvenColor;
      }
    }

    void main(void) {
      vec2 ntc = vec2(vtc.x / vradius, vtc.y);
      vec4 color = arrowColor(ntc, vlength / vradius);
      if (color.a == 0.0) {
        discard;
      }
      gl_FragColor = color;
    }
  `);return a}var f=Object.freeze({__proto__:null,build:d});c.MeasurementArrowShader=f;c.build=d});