// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","./NormalAttribute.glsl","./VertexPosition.glsl"],function(e,c,f,g){function d(a,b){0===b.normalType||1===b.normalType?(a.include(f.NormalAttribute,b),a.varyings.add("vNormalWorld","vec3"),a.varyings.add("vNormalView","vec3"),a.vertex.uniforms.add("uTransformNormal_GlobalFromModel","mat3"),a.vertex.uniforms.add("uTransformNormal_ViewFromGlobal","mat3"),a.vertex.code.add(c.glsl`
      void forwardNormal() {
        vNormalWorld = uTransformNormal_GlobalFromModel * normalModel();
        vNormalView = uTransformNormal_ViewFromGlobal * vNormalWorld;
      }
    `)):2===b.normalType?(a.include(g.VertexPosition,b),a.varyings.add("vNormalWorld","vec3"),a.vertex.code.add(c.glsl`
    void forwardNormal() {
      vNormalWorld = ${1===b.viewingMode?c.glsl`normalize(vPositionWorldCameraRelative);`:c.glsl`vec3(0.0, 0.0, 1.0);`}
    }
    `)):a.vertex.code.add(c.glsl`
      void forwardNormal() {}
    `)}(function(a){a.bindUniforms=function(b,h){b.setUniformMatrix4fv("viewNormal",h)}})(d||(d={}));e.VertexNormal=d;Object.defineProperty(e,"__esModule",{value:!0})});