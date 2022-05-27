// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","./Projection.glsl"],function(b,c,d){b.Common=function(a){a.include(d.Projection);a.fragment.uniforms.add("u_image","sampler2D");a.fragment.uniforms.add("u_isFloatTexture","bool");a.fragment.uniforms.add("u_flipY","bool");a.fragment.uniforms.add("u_applyTransform","bool");a.fragment.uniforms.add("u_opacity","float");a.fragment.code.add(c.glsl`
    vec2 getPixelLocation(vec2 coords) {
      vec2 targetLocation = u_flipY ? vec2(coords.s, 1.0 - coords.t) : coords;
      if (!u_applyTransform) {
        return targetLocation;
      }
      return projectPixelLocation(targetLocation);
    }

    bool isOutside(vec2 coords){
      if (coords.t>1.00001 ||coords.t<-0.00001 || coords.s>1.00001 ||coords.s<-0.00001) {
        return true;
      } else {
        return false;
      }
    }

    vec4 getPixel(vec2 pixelLocation) {
      return texture2D(u_image, pixelLocation);
    }
  `)};Object.defineProperty(b,"__esModule",{value:!0})});