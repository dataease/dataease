// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../util/DecodeNormal.glsl"],function(d,b,e){d.NormalAttribute=function(a,c){0===c.normalType&&(a.attributes.add("normal","vec3"),a.vertex.code.add(b.glsl`
      vec3 normalModel() {
        return normal;
      }
    `));1===c.normalType&&(a.include(e.DecodeNormal),a.attributes.add("normalCompressed","vec2"),a.vertex.code.add(b.glsl`
      vec3 normalModel() {
        return decodeNormal(normalCompressed);
      }
    `));3===c.normalType&&(a.extensions.add("GL_OES_standard_derivatives"),a.fragment.code.add(b.glsl`
      vec3 screenDerivativeNormal(vec3 positionView) {
        return normalize(cross(dFdx(positionView), dFdy(positionView)));
      }
    `))};Object.defineProperty(d,"__esModule",{value:!0})});