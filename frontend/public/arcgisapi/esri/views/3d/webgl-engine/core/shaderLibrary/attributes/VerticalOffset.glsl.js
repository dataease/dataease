// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../shaderModules/interfaces","../util/ScreenSizePerspective.glsl"],function(f,e,k){function g(b,d){const a=b.vertex.code;d.verticalOffsetEnabled?(b.vertex.uniforms.add("verticalOffset","vec4"),d.screenSizePerspectiveEnabled&&(b.include(k.ScreenSizePerspective),b.vertex.uniforms.add("screenSizePerspectiveAlignment","vec4")),a.add(e.glsl`
    vec3 calculateVerticalOffset(vec3 worldPos, vec3 localOrigin) {
      float viewDistance = length((view * vec4(worldPos, 1.0)).xyz);
      ${1===d.viewingMode?e.glsl`vec3 worldNormal = normalize(worldPos + localOrigin);`:e.glsl`vec3 worldNormal = vec3(0.0, 0.0, 1.0);`}
      ${d.screenSizePerspectiveEnabled?e.glsl`
          float cosAngle = dot(worldNormal, normalize(worldPos - camPos));
          float verticalOffsetScreenHeight = screenSizePerspectiveScaleFloat(verticalOffset.x, abs(cosAngle), viewDistance, screenSizePerspectiveAlignment);`:e.glsl`
          float verticalOffsetScreenHeight = verticalOffset.x;`}
      // Screen sized offset in world space, used for example for line callouts
      float worldOffset = clamp(verticalOffsetScreenHeight * verticalOffset.y * viewDistance, verticalOffset.z, verticalOffset.w);
      return worldNormal * worldOffset;
    }

    vec3 addVerticalOffset(vec3 worldPos, vec3 localOrigin) {
      return worldPos + calculateVerticalOffset(worldPos, localOrigin);
    }
    `)):a.add(e.glsl`
    vec3 addVerticalOffset(vec3 worldPos, vec3 localOrigin) { return worldPos; }
    `)}function h(b,d,a,c=l){c.screenLength=b.screenLength;c.perDistance=Math.tan(.5*d)/(.5*a);c.minWorldLength=b.minWorldLength;c.maxWorldLength=b.maxWorldLength;return c}(function(b){b.bindUniforms=function(d,a,c){a.verticalOffset&&(a=h(a.verticalOffset,c.camera.fovY,c.camera.fullViewport[3]),d.setUniform4f("verticalOffset",a.screenLength*(c.camera.pixelRatio||1),a.perDistance,a.minWorldLength,a.maxWorldLength))}})(g||(g={}));const l={screenLength:0,perDistance:0,minWorldLength:0,maxWorldLength:0};
f.VerticalOffset=g;f.calculateVerticalOffsetFactors=h;Object.defineProperty(f,"__esModule",{value:!0})});