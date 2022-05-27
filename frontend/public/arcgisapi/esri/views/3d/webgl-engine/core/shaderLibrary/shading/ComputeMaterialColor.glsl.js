// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../attributes/VertexColor.glsl","../util/MixExternalColor.glsl"],function(c,b,e,f){c.ComputeMaterialColor=function(a,d){a.include(e.VertexColor,d);a.fragment.include(f.MixExternalColor);a=a.fragment;a.uniforms.add("uBaseColor","vec4");a.uniforms.add("uObjectOpacity","float");d.attributeColor?a.code.add(b.glsl`
      vec3 _baseColor() {
        // combine the old material parameters into a single one
        return uBaseColor.rgb * vColor.rgb;
      }

      float _baseOpacity() {
        return uBaseColor.a * vColor.a;
      }
    `):a.code.add(b.glsl`
      vec3 _baseColor() {
        // combine the old material parameters into a single one
        return uBaseColor.rgb;
      }

      float _baseOpacity() {
        return uBaseColor.a;
      }
    `);a.code.add(b.glsl`
    vec4 computeMaterialColor(vec4 textureColor, vec4 externalColor, int externalColorMixMode) {
      vec3 baseColor = _baseColor();
      float baseOpacity = _baseOpacity();

      vec3 color = mixExternalColor(
        baseColor,
        textureColor.rgb,
        externalColor.rgb,
        externalColorMixMode
      );
      float opacity = uObjectOpacity * mixExternalOpacity(
        baseOpacity,
        textureColor.a,
        externalColor.a,
        externalColorMixMode
      );

      return vec4(color, opacity);
    }
  `)};Object.defineProperty(c,"__esModule",{value:!0})});