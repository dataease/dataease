// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../attributes/VertexPosition.glsl","../attributes/VertexNormal.glsl"],function(f,c,e,g){f.ComputeShadingNormal=function(d,a){const b=d.fragment;0===a.doubleSidedMode?b.code.add(c.glsl`
      vec3 _adjustDoublesided(vec3 normal) {
        return normal;
      }
    `):1===a.doubleSidedMode?(d.include(e.VertexPosition,a),b.uniforms.add("uTransform_ViewFromCameraLocal_T","vec3"),b.code.add(c.glsl`
      vec3 _adjustDoublesided(vec3 normal) {
        vec3 viewDir = vPositionWorldCameraRelative + uTransform_ViewFromCameraLocal_T;
        return dot(normal, viewDir) > 0.0 ? -normal : normal;
      }
    `)):2===a.doubleSidedMode&&b.code.add(c.glsl`
      vec3 _adjustDoublesided(vec3 normal) {
        return gl_FrontFacing ? normal : -normal;
      }
    `);0===a.normalType||1===a.normalType?(d.include(g.VertexNormal,a),b.code.add(c.glsl`
      vec3 shadingNormalWorld() {
        return _adjustDoublesided(normalize(vNormalWorld));
      }

      vec3 shadingNormal_view() {
        vec3 normal = normalize(vNormalView);
        return gl_FrontFacing ? normal : -normal;
      }
    `)):3===a.normalType?(d.extensions.add("GL_OES_standard_derivatives"),d.include(e.VertexPosition,a),b.code.add(c.glsl`
      vec3 shadingNormalWorld() {
        return normalize(cross(
          dFdx(vPositionWorldCameraRelative),
          dFdy(vPositionWorldCameraRelative)
        ));
      }

      vec3 shadingNormal_view() {
        return normalize(cross(dFdx(vPosition_view),dFdy(vPosition_view)));
      }
    `)):2===a.normalType&&(1===a.viewingMode?(d.include(e.VertexPosition,a),b.code.add(c.glsl`
        vec3 shadingNormalWorld() {
          return normalize(positionWorld());
        }
      `)):2===a.viewingMode&&b.code.add(c.glsl`
        vec3 shadingNormalWorld() {
          return vec3(0.0, 0.0, 1.0);
        }
      `),d.extensions.add("GL_OES_standard_derivatives"),b.code.add(c.glsl`
      vec3 shadingNormal_view() {
        return normalize(cross(dFdx(vPosition_view),dFdy(vPosition_view))).xyz;
      }
    `))};Object.defineProperty(f,"__esModule",{value:!0})});