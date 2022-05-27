// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(g,e){function f(b,d){d.vvInstancingEnabled&&(d.vvSize||d.vvColor)&&b.attributes.add("instanceFeatureAttribute","vec4");d.vvSize?(b.vertex.uniforms.add("vvSizeMinSize","vec3"),b.vertex.uniforms.add("vvSizeMaxSize","vec3"),b.vertex.uniforms.add("vvSizeOffset","vec3"),b.vertex.uniforms.add("vvSizeFactor","vec3"),b.vertex.uniforms.add("vvSymbolRotationMatrix","mat3"),b.vertex.uniforms.add("vvSymbolAnchor","vec3"),b.vertex.code.add(e.glsl`
      vec3 vvScale(vec4 _featureAttribute) {
        return clamp(vvSizeOffset + _featureAttribute.x * vvSizeFactor, vvSizeMinSize, vvSizeMaxSize);
      }

      vec4 vvTransformPosition(vec3 position, vec4 _featureAttribute) {
        return vec4(vvSymbolRotationMatrix * ( vvScale(_featureAttribute) * (position + vvSymbolAnchor)), 1.0);
      }
    `),b.vertex.code.add(e.glsl`
      const float eps = 1.192092896e-07;
      vec4 vvTransformNormal(vec3 _normal, vec4 _featureAttribute) {
        vec3 vvScale = clamp(vvSizeOffset + _featureAttribute.x * vvSizeFactor, vvSizeMinSize + eps, vvSizeMaxSize);
        return vec4(vvSymbolRotationMatrix * _normal / vvScale, 1.0);
      }

      ${d.vvInstancingEnabled?e.glsl`
      vec4 vvLocalNormal(vec3 _normal) {
        return vvTransformNormal(_normal, instanceFeatureAttribute);
      }

      vec4 localPosition() {
        return vvTransformPosition(position, instanceFeatureAttribute);
      }`:""}
    `)):b.vertex.code.add(e.glsl`
      vec4 localPosition() { return vec4(position, 1.0); }

      vec4 vvLocalNormal(vec3 _normal) { return vec4(_normal, 1.0); }
    `);d.vvColor?(b.vertex.defines.addInt("VV_COLOR_N",8),b.vertex.code.add(e.glsl`
      uniform float vvColorValues[VV_COLOR_N];
      uniform vec4 vvColorColors[VV_COLOR_N];

      vec4 vvGetColor(vec4 featureAttribute, float values[VV_COLOR_N], vec4 colors[VV_COLOR_N]) {
        float value = featureAttribute.y;
        if (value <= values[0]) {
          return colors[0];
        }

        for (int i = 1; i < VV_COLOR_N; ++i) {
          if (values[i] >= value) {
            float f = (value - values[i-1]) / (values[i] - values[i-1]);
            return mix(colors[i-1], colors[i], f);
          }
        }
        return colors[VV_COLOR_N - 1];
      }

      ${d.vvInstancingEnabled?e.glsl`
      vec4 vvColor() {
        return vvGetColor(instanceFeatureAttribute, vvColorValues, vvColorColors);
      }`:""}
    `)):b.vertex.code.add(e.glsl`
      vec4 vvColor() { return vec4(1.0); }
    `)}(function(b){function d(c,a){a.vvSizeEnabled&&(c.setUniform3fv("vvSizeMinSize",a.vvSizeMinSize),c.setUniform3fv("vvSizeMaxSize",a.vvSizeMaxSize),c.setUniform3fv("vvSizeOffset",a.vvSizeOffset),c.setUniform3fv("vvSizeFactor",a.vvSizeFactor));a.vvColorEnabled&&(c.setUniform1fv("vvColorValues",a.vvColorValues),c.setUniform4fv("vvColorColors",a.vvColorColors))}b.bindUniforms=d;b.bindUniformsWithOpacity=function(c,a){d(c,a);a.vvOpacityEnabled&&(c.setUniform1fv("vvOpacityValues",a.vvOpacityValues),
c.setUniform1fv("vvOpacityOpacities",a.vvOpacityOpacities))};b.bindUniformsForSymbols=function(c,a){d(c,a);a.vvSizeEnabled&&(c.setUniform3fv("vvSymbolAnchor",a.vvSymbolAnchor),c.setUniformMatrix3fv("vvSymbolRotationMatrix",a.vvSymbolRotationMatrix))}})(f||(f={}));g.VisualVariables=f;Object.defineProperty(g,"__esModule",{value:!0})});