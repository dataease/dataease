// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../shaderModules/interfaces"],function(b,e){(function(c){c.builder=function(a,d){a.vertex.uniforms.add("camPos","vec3").add("perScreenPixelRatio","float").add("screenSize","float");a.vertex.code.add(e.glsl`
    float computeRenderPixelSizeAt( vec3 pWorld ){
      vec3 viewForward = - vec3(view[0][2], view[1][2], view[2][2]);
      float viewDirectionDistance = abs(dot(viewForward, pWorld - camPos));
      return viewDirectionDistance*perScreenPixelRatio;
    }

    vec3 screenSizeScaling(vec3 position, vec3 anchor){
      return position * screenSize * computeRenderPixelSizeAt(anchor) + anchor;
    }
  `)};c.bindPassUniforms=function(a,d,f){a.setUniform1f("perScreenPixelRatio",f.camera.perScreenPixelRatio);a.setUniform1f("screenSize",d.screenSize)}})(b.ScreenSizeScaling||(b.ScreenSizeScaling={}));Object.defineProperty(b,"__esModule",{value:!0})});