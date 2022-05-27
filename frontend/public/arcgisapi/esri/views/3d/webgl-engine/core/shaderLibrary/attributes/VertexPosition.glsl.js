// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../../../../../chunks/vec3f64 ../../../../../../chunks/mat3f64 ../../../../../../chunks/mat4f64 ../../shaderModules/interfaces ../util/DoublePrecision.glsl ./PositionAttribute.glsl".split(" "),function(f,d,g,k,h,l,m){function e(a,b){a.include(m.PositionAttribute);a.vertex.include(l.DoublePrecision,b);a.varyings.add("vPositionWorldCameraRelative","vec3");a.varyings.add("vPosition_view","vec3");a.vertex.uniforms.add("uTransform_WorldFromModel_RS","mat3");a.vertex.uniforms.add("uTransform_WorldFromModel_TH",
"vec3");a.vertex.uniforms.add("uTransform_WorldFromModel_TL","vec3");a.vertex.uniforms.add("uTransform_WorldFromView_TH","vec3");a.vertex.uniforms.add("uTransform_WorldFromView_TL","vec3");a.vertex.uniforms.add("uTransform_ViewFromCameraRelative_RS","mat3");a.vertex.uniforms.add("uTransform_ProjFromView","mat4");a.vertex.code.add(h.glsl`
    // compute position in world space orientation, but relative to the camera position
    vec3 positionWorldCameraRelative() {
      vec3 rotatedModelPosition = uTransform_WorldFromModel_RS * positionModel();

      vec3 transform_CameraRelativeFromModel = dpAdd(
        uTransform_WorldFromModel_TL,
        uTransform_WorldFromModel_TH,
        -uTransform_WorldFromView_TL,
        -uTransform_WorldFromView_TH
      );

      return transform_CameraRelativeFromModel + rotatedModelPosition;
    }

    // position in view space, that is relative to the camera position and orientation
    vec3 position_view() {
      return uTransform_ViewFromCameraRelative_RS * positionWorldCameraRelative();
    }

    // compute gl_Position and forward related varyings to fragment shader
    void forwardPosition() {
      vPositionWorldCameraRelative = positionWorldCameraRelative();
      vPosition_view = position_view();
      gl_Position = uTransform_ProjFromView * vec4(vPosition_view, 1.0);
    }

    vec3 positionWorld() {
      return uTransform_WorldFromView_TL + vPositionWorldCameraRelative;
    }
  `);a.fragment.uniforms.add("uTransform_WorldFromView_TL","vec3");a.fragment.code.add(h.glsl`
    vec3 positionWorld() {
      return uTransform_WorldFromView_TL + vPositionWorldCameraRelative;
    }
  `)}(function(a){a.ModelTransform=function(){this.worldFromModel_RS=g.create();this.worldFromModel_TH=d.create();this.worldFromModel_TL=d.create()};a.ViewProjectionTransform=function(){this.worldFromView_TH=d.create();this.worldFromView_TL=d.create();this.viewFromCameraRelative_RS=g.create();this.projFromView=k.create()};a.bindModelTransform=function(b,c){b.setUniformMatrix3fv("uTransform_WorldFromModel_RS",c.worldFromModel_RS);b.setUniform3fv("uTransform_WorldFromModel_TH",c.worldFromModel_TH);
b.setUniform3fv("uTransform_WorldFromModel_TL",c.worldFromModel_TL)};a.bindViewProjTransform=function(b,c){b.setUniform3fv("uTransform_WorldFromView_TH",c.worldFromView_TH);b.setUniform3fv("uTransform_WorldFromView_TL",c.worldFromView_TL);b.setUniformMatrix4fv("uTransform_ProjFromView",c.projFromView);b.setUniformMatrix3fv("uTransform_ViewFromCameraRelative_RS",c.viewFromCameraRelative_RS)}})(e||(e={}));f.VertexPosition=e;Object.defineProperty(f,"__esModule",{value:!0})});