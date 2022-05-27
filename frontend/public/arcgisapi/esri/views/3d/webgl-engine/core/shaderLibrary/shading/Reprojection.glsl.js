// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces"],function(e,f){function c(a){a.fragment.uniforms.add("lastFrameColorMap","sampler2D");a.fragment.uniforms.add("reprojectionMat","mat4");a.fragment.uniforms.add("rpProjectionMat","mat4");a.fragment.code.add(f.glsl`
  vec2 reprojectionCoordinate(vec3 projectionCoordinate)
  {
    vec4 zw = rpProjectionMat * vec4(0.0, 0.0, -projectionCoordinate.z, 1.0);
    vec4 reprojectedCoord = reprojectionMat * vec4(zw.w * (projectionCoordinate.xy * 2.0 - 1.0), zw.z, zw.w);
    reprojectedCoord.xy /= reprojectedCoord.w;
    return reprojectedCoord.xy*0.5 + 0.5;
  }
  `)}(function(a){a.bindUniforms=function(d,g,b){d.setUniform1i("lastFrameColorMap",b.lastFrameColorTextureID);g.bindTexture(b.lastFrameColorTexture,b.lastFrameColorTextureID);d.setUniformMatrix4fv("reprojectionMat",b.reprojectionMat);d.setUniformMatrix4fv("rpProjectionMat",b.camera.projectionMatrix)}})(c||(c={}));e.Reprojection=c;Object.defineProperty(e,"__esModule",{value:!0})});