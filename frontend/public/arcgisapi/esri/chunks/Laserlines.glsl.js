// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../core/mathUtils ../views/3d/webgl-engine/core/shaderModules/interfaces ../views/3d/webgl-engine/core/shaderModules/ShaderBuilder ../views/3d/webgl-engine/core/shaderLibrary/ScreenSpacePass ../views/3d/webgl-engine/core/shaderLibrary/Laserline.glsl".split(" "),function(e,d,c,g,h,k){function f(b){const a=new g.ShaderBuilder;a.extensions.add("GL_OES_standard_derivatives");a.include(h.ScreenSpacePass);a.include(k.Laserline,b);a.fragment.uniforms.add("angleCutoff","vec2");a.fragment.uniforms.add("globalAlpha",
"float");b.heightManifoldEnabled&&a.fragment.uniforms.add("heightPlane","vec4");b.pointDistanceEnabled&&a.fragment.uniforms.add("pointDistanceSphere","vec4");b.lineVerticalPlaneEnabled&&a.fragment.uniforms.add("lineVerticalPlane","vec4").add("lineVerticalStart","vec3").add("lineVerticalEnd","vec3");(b.heightManifoldEnabled||b.pointDistanceEnabled||b.lineVerticalPlaneEnabled)&&a.fragment.uniforms.add("maxPixelDistance","float");b.intersectsLineEnabled&&a.fragment.uniforms.add("intersectsLineStart",
"vec3").add("intersectsLineEnd","vec3").add("intersectsLineDirection","vec3").add("intersectsLineRadius","float").add("perScreenPixelRatio","float");(b.lineVerticalPlaneEnabled||b.heightManifoldEnabled)&&a.fragment.code.add(c.glsl`
      float planeDistancePixels(vec4 plane, vec3 pos) {
        float dist = dot(plane.xyz, pos) + plane.w;
        float width = fwidth(dist);
        dist /= min(width, maxPixelDistance);
        return abs(dist);
      }`);b.pointDistanceEnabled&&a.fragment.code.add(c.glsl`
    float sphereDistancePixels(vec4 sphere, vec3 pos) {
      float dist = distance(sphere.xyz, pos) - sphere.w;
      float width = fwidth(dist);
      dist /= min(width, maxPixelDistance);
      return abs(dist);
    }
    `);b.intersectsLineEnabled&&a.fragment.code.add(c.glsl`
    float lineDistancePixels(vec3 start, vec3 dir, float radius, vec3 pos) {
      float dist = length(cross(dir, pos - start)) / (length(pos) * perScreenPixelRatio);
      return abs(dist) - radius;
    }
    `);(b.lineVerticalPlaneEnabled||b.intersectsLineEnabled)&&a.fragment.code.add(c.glsl`
    bool pointIsWithinLine(vec3 pos, vec3 start, vec3 end) {
      vec3 dir = end - start;
      float t2 = dot(dir, pos - start);
      float l2 = dot(dir, dir);
      return t2 >= 0.0 && t2 <= l2;
    }
    `);a.fragment.code.add(c.glsl`
  void main() {
    vec3 pos;
    vec3 normal;
    float depthDiscontinuityAlpha;

    if (!laserlineReconstructFromDepth(pos, normal, depthDiscontinuityAlpha)) {
      discard;
    }

    vec4 color = vec4(0, 0, 0, 0);
  `);b.heightManifoldEnabled&&a.fragment.code.add(c.glsl`
    {
      float heightManifoldAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, abs(dot(normal, heightPlane.xyz)));
      vec4 heightManifoldColor = laserlineProfile(planeDistancePixels(heightPlane, pos));
      color = max(color, heightManifoldColor * heightManifoldAlpha);
    }
    `);b.pointDistanceEnabled&&a.fragment.code.add(c.glsl`
    {
      // distance to sphere
      float pointDistanceSphereDistance = sphereDistancePixels(pointDistanceSphere, pos);
      vec4 pointDistanceSphereColor = laserlineProfile(pointDistanceSphereDistance);
      float pointDistanceSphereAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, abs(dot(normal, normalize(pos - pointDistanceSphere.xyz))));

      color = max(color, pointDistanceSphereColor * pointDistanceSphereAlpha);
    }
    `);b.lineVerticalPlaneEnabled&&a.fragment.code.add(c.glsl`
    {
      if (pointIsWithinLine(pos, lineVerticalStart, lineVerticalEnd)) {
        float lineVerticalDistance = planeDistancePixels(lineVerticalPlane, pos);

        vec4 lineVerticalColor = laserlineProfile(lineVerticalDistance);
        float lineVerticalAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, abs(dot(normal, lineVerticalPlane.xyz)));

        color = max(color, lineVerticalColor * lineVerticalAlpha);
      }
    }
    `);b.intersectsLineEnabled&&a.fragment.code.add(c.glsl`
    {
      if (pointIsWithinLine(pos, intersectsLineStart, intersectsLineEnd)) {
        float intersectsLineDistance = lineDistancePixels(intersectsLineStart, intersectsLineDirection, intersectsLineRadius, pos);
        vec4 intersectsLineColor = laserlineProfile(intersectsLineDistance);
        float intersectsLineAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, 1.0 - abs(dot(normal, intersectsLineDirection)));

        color = max(color, intersectsLineColor * intersectsLineAlpha);
      }
    }
    `);a.fragment.code.add(c.glsl`
    gl_FragColor = laserlineOutput(color * depthDiscontinuityAlpha);
  }
  `);return a}d=d.deg2rad(6);var l=Object.freeze({__proto__:null,defaultAngleCutoff:d,build:f});e.LaserlinesShader=l;e.build=f;e.defaultAngleCutoff=d});