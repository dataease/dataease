/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import"./object.js";import{i as t,b as i}from"./Logger.js";import{d as n}from"./mathUtils2.js";import{c as r}from"./vec3f64.js";import{f as a,c as s,g as o,i as l,k as c,n as d,e as h,a as f}from"./vec3.js";import{t as p}from"./mat4.js";import{c as u}from"./vec4f64.js";import{c as m}from"./quatf64.js";import{t as g}from"./vec4.js";import{c as _}from"./vec2f64.js";import{g as b,S as v,p as x,a as P,b as V,R as D,D as E,af as w,ag as C}from"./PiUtils.glsl.js";import{P as L}from"./Program.js";import{m as S,c as A,d as M}from"./isWebGL2Context.js";import{g as y}from"./glUtil.js";import{n as U}from"./InterleavedLayout.js";import{N as R,u as q,p as j,s as T,r as z,w as O}from"./geometryUtils.js";import{i as W}from"./Util.js";import{c as F}from"./Geometry.js";import{V as B,B as I}from"./VertexArrayObject.js";import"./RenderingContext.js";import{R as H,C as N,S as G}from"./CameraSpace.glsl.js";function k(e,t){e.extensions.add("GL_OES_standard_derivatives"),e.include(H),e.include(N),e.fragment.uniforms.add("glowColor","vec3"),e.fragment.uniforms.add("glowWidth","float"),e.fragment.uniforms.add("glowFalloff","float"),e.fragment.uniforms.add("innerColor","vec3"),e.fragment.uniforms.add("innerWidth","float"),e.fragment.uniforms.add("depthMap","sampler2D"),e.fragment.uniforms.add("nearFar","vec2"),e.fragment.uniforms.add("frameColor","sampler2D"),t.contrastControlEnabled&&e.fragment.uniforms.add("globalAlphaContrastBoost","float"),e.fragment.code.add(b`
  vec4 blendPremultiplied(vec4 source, vec4 dest) {
    float oneMinusSourceAlpha = 1.0 - source.a;

    return vec4(
      source.rgb + dest.rgb * oneMinusSourceAlpha,
      source.a + dest.a * oneMinusSourceAlpha
    );
  }
  `),e.fragment.code.add(b`
  vec4 premultipliedColor(vec3 rgb, float alpha) {
    return vec4(rgb * alpha, alpha);
  }
  `),e.fragment.code.add(b`
  vec4 laserlineProfile(float dist) {
    if (dist > glowWidth) {
      return vec4(0.0);
    }

    float innerAlpha = (1.0 - smoothstep(0.0, innerWidth, dist));
    float glowAlpha = pow(max(0.0, 1.0 - dist / glowWidth), glowFalloff);

    return blendPremultiplied(
      premultipliedColor(innerColor, innerAlpha),
      premultipliedColor(glowColor, glowAlpha)
    );
  }
  `),e.fragment.code.add(b`
  bool laserlineReconstructFromDepth(out vec3 pos, out vec3 normal, out float depthDiscontinuityAlpha) {
    float depth = linearDepthFromTexture(depthMap, uv, nearFar);

    if (-depth == nearFar[0]) {
      return false;
    }

    pos = reconstructPosition(gl_FragCoord.xy, depth);
    normal = normalize(cross(dFdx(pos), dFdy(pos)));

    float ddepth = fwidth(depth);
    depthDiscontinuityAlpha = 1.0 - smoothstep(0.0, 0.01, -ddepth / depth);

    return true;
  }
  `),t.contrastControlEnabled?e.fragment.code.add(b`
    float rgbToLuminance(vec3 color) {
      return dot(vec3(0.2126, 0.7152, 0.0722), color);
    }

    vec4 laserlineOutput(vec4 color) {
      float backgroundLuminance = rgbToLuminance(texture2D(frameColor, uv).rgb);
      float alpha = clamp(globalAlpha * max(backgroundLuminance * globalAlphaContrastBoost, 1.0), 0.0, 1.0);

      return color * alpha;
    }
    `):e.fragment.code.add(b`
    vec4 laserlineOutput(vec4 color) {
      return color * globalAlpha;
    }
    `)}var X=Object.freeze({__proto__:null,build:function(e){const t=new v;return t.include(k,e),t.vertex.uniforms.add("uModelViewMatrix","mat4"),t.vertex.uniforms.add("uProjectionMatrix","mat4"),t.attributes.add("start","vec3"),t.attributes.add("end","vec3"),t.attributes.add("up","vec3"),t.attributes.add("extrude","vec2"),t.varyings.add("uv","vec2"),t.varyings.add("vViewStart","vec3"),t.varyings.add("vViewEnd","vec3"),t.varyings.add("vViewPlane","vec4"),t.vertex.uniforms.add("glowWidth","float"),t.vertex.uniforms.add("pixelToNDC","vec2"),t.vertex.code.add(b`
  void main() {
    vec3 pos = mix(start, end, extrude.x);

    vec4 viewPos = uModelViewMatrix * vec4(pos, 1);
    vec4 projPos = uProjectionMatrix * viewPos;
    vec2 ndcPos = projPos.xy / projPos.w;

    vec3 viewUp = (uModelViewMatrix * vec4(extrude.y * up, 0)).xyz;
    vec4 projPosUp = uProjectionMatrix * vec4(viewPos.xyz + viewUp, 1);
    vec2 projExtrudeDir = normalize(projPosUp.xy / projPosUp.w - ndcPos);

    vec2 lxy = abs(sign(projExtrudeDir) - ndcPos);
    ndcPos += length(lxy) * projExtrudeDir;

    vec3 worldPlaneNormal = normalize(cross(up, normalize(end - start)));
    vec3 viewPlaneNormal = (uModelViewMatrix * vec4(worldPlaneNormal, 0)).xyz;

    vViewStart = (uModelViewMatrix * vec4(start, 1)).xyz;
    vViewEnd = (uModelViewMatrix * vec4(end, 1)).xyz;
    vViewPlane = vec4(viewPlaneNormal, -dot(viewPlaneNormal, vViewStart));

    // Add enough padding in the X screen space direction for glow
    float xPaddingPixels = sign(dot(viewPlaneNormal, viewPos.xyz)) * (extrude.x * 2.0 - 1.0) * glowWidth;
    ndcPos.x += xPaddingPixels * pixelToNDC.x;

    uv = ndcPos * 0.5 + 0.5;
    gl_Position = vec4(ndcPos, 0, 1);
  }
  `),t.fragment.uniforms.add("globalAlpha","float"),t.fragment.uniforms.add("perScreenPixelRatio","float"),t.fragment.code.add(b`
  float planeDistancePixels(vec4 plane, vec3 pos, vec3 start, vec3 end) {
    vec3 origin = mix(start, end, 0.5);
    vec3 basis = end - origin;
    vec3 posAtOrigin = pos - origin;

    float x = dot(normalize(basis), posAtOrigin);
    float y = dot(plane.xyz, posAtOrigin);

    float dx = max(abs(x) - length(basis), 0.0);
    float dy = y;

    float dist = length(vec2(dx, dy));

    float width = fwidth(y);
    float maxPixelDistance = length(pos) * perScreenPixelRatio * 2.0;
    float pixelDist = dist / min(width, maxPixelDistance);
    return abs(pixelDist);
  }

  void main() {
    vec3 pos;
    vec3 normal;
    float depthDiscontinuityAlpha;

    if (!laserlineReconstructFromDepth(pos, normal, depthDiscontinuityAlpha)) {
      discard;
    }

    float distance = planeDistancePixels(vViewPlane, pos, vViewStart, vViewEnd);

    vec4 color = laserlineProfile(distance);
    float alpha = 1.0 - smoothstep(0.995, 0.999, abs(dot(normal, vViewPlane.xyz)));

    gl_FragColor = laserlineOutput(color * alpha * depthDiscontinuityAlpha);
  }
  `),t}});class J extends V{initializeProgram(e){const t=J.shader.get(),i=this.configuration,n=t.build(i);return new L(e.rctx,n.generateSource("vertex"),n.generateSource("fragment"),J.attributeLocations)}bind(e,t,i){this.program.setUniform3fv("innerColor",e.innerColor),this.program.setUniform1f("innerWidth",e.innerWidth*i.pixelRatio),this.program.setUniform3fv("glowColor",e.glowColor),this.program.setUniform1f("glowWidth",e.glowWidth*i.pixelRatio),this.program.setUniform1f("glowFalloff",e.glowFalloff),this.program.setUniform1f("globalAlpha",e.globalAlpha),this.program.setUniform1f("perScreenPixelRatio",i.perScreenPixelRatio),this.program.setUniform2f("pixelToNDC",2/i.fullWidth,2/i.fullHeight),this.program.setUniformMatrix4fv("uProjectionMatrix",i.projectionMatrix),p(Q,i.viewMatrix,t),this.program.setUniformMatrix4fv("uModelViewMatrix",Q),this.configuration.contrastControlEnabled&&this.program.setUniform1f("globalAlphaContrastBoost",null!=e.globalAlphaContrastBoost?e.globalAlphaContrastBoost:1)}initializePipeline(){return S({blending:A(1,771),colorWrite:M})}bindPipelineState(e){e.setPipelineState(this.pipeline)}}J.shader=new D(X,(()=>Promise.resolve().then((function(){return X})))),J.attributeLocations={start:0,end:1,up:2,extrude:3};class K extends P{constructor(){super(...arguments),this.contrastControlEnabled=!1}}e([x()],K.prototype,"contrastControlEnabled",void 0);const Q=m();class Y{constructor(e){this._renderCoordsHelper=e,this._buffers=null,this._origin=r(),this._dirty=!1,this._count=0,this._vao=null}set vertices(e){const t=new Float64Array(3*e.length);let i=0;for(const n of e)t[i++]=n[0],t[i++]=n[1],t[i++]=n[2];this.buffers=[t]}set buffers(e){if(this._buffers=e,this._buffers.length>0){const e=this._buffers[0],t=3*Math.floor(e.length/3/2);a(this._origin,e[t+0],e[t+1],e[t+2])}else a(this._origin,0,0,0);this._dirty=!0}get origin(){return this._origin}draw(e){const i=this.ensureVAO(e);t(i)&&(e.bindVAO(i),e.drawArrays(4,0,this._count))}dispose(){t(this._vao)&&this._vao.dispose()}ensureVAO(e){return i(this._buffers)?null:(i(this._vao)&&(this._vao=this.createVAO(e,this._buffers)),this.ensureVertexData(this._vao,this._buffers),this._vao)}createVAO(e,t){const i=this.createDataBuffer(t);return this._dirty=!1,new B(e,J.attributeLocations,{data:y(ee)},{data:I.createVertex(e,35044,i)})}ensureVertexData(e,t){if(!this._dirty)return;const i=this.createDataBuffer(t);e.vertexBuffers.data.setData(i),this._dirty=!1}numberOfRenderVertices(e){return 3*(2*(e.length/3-1))}createDataBuffer(e){const t=e.reduce(((e,t)=>e+this.numberOfRenderVertices(t)),0);this._count=t;const i=ee.createBuffer(t),n=this._origin;let r=0,o=0;for(const t of e){for(let e=0;e<t.length;e+=3){const l=a($,t[e+0],t[e+1],t[e+2]);0===e?o=this._renderCoordsHelper.getAltitude(l):this._renderCoordsHelper.setAltitude(o,l);const c=this._renderCoordsHelper.worldUpAtPosition(l,Z),d=r+2*e,h=s($,l,n);if(e<t.length-3){i.up.setVec(d,c),i.up.setVec(d+3,c),i.up.setVec(d+5,c);for(let e=0;e<6;e++)i.start.setVec(d+e,h);i.extrude.setValues(d+0,0,-1),i.extrude.setValues(d+1,1,-1),i.extrude.setValues(d+2,1,1),i.extrude.setValues(d+3,0,-1),i.extrude.setValues(d+4,1,1),i.extrude.setValues(d+5,0,1)}if(e>0){i.up.setVec(d-2,c),i.up.setVec(d-4,c),i.up.setVec(d-5,c);for(let e=-6;e<0;e++)i.end.setVec(d+e,h)}}r+=this.numberOfRenderVertices(t)}return i.buffer}}const Z=r(),$=r(),ee=U().vec3f("start").vec3f("end").vec3f("up").vec2f("extrude"),te=n(6);var ie=Object.freeze({__proto__:null,defaultAngleCutoff:te,build:function(e){const t=new v;return t.extensions.add("GL_OES_standard_derivatives"),t.include(G),t.include(k,e),t.fragment.uniforms.add("angleCutoff","vec2"),t.fragment.uniforms.add("globalAlpha","float"),e.heightManifoldEnabled&&t.fragment.uniforms.add("heightPlane","vec4"),e.pointDistanceEnabled&&t.fragment.uniforms.add("pointDistanceSphere","vec4"),e.lineVerticalPlaneEnabled&&t.fragment.uniforms.add("lineVerticalPlane","vec4").add("lineVerticalStart","vec3").add("lineVerticalEnd","vec3"),(e.heightManifoldEnabled||e.pointDistanceEnabled||e.lineVerticalPlaneEnabled)&&t.fragment.uniforms.add("maxPixelDistance","float"),e.intersectsLineEnabled&&t.fragment.uniforms.add("intersectsLineStart","vec3").add("intersectsLineEnd","vec3").add("intersectsLineDirection","vec3").add("intersectsLineRadius","float").add("perScreenPixelRatio","float"),(e.lineVerticalPlaneEnabled||e.heightManifoldEnabled)&&t.fragment.code.add(b`
      float planeDistancePixels(vec4 plane, vec3 pos) {
        float dist = dot(plane.xyz, pos) + plane.w;
        float width = fwidth(dist);
        dist /= min(width, maxPixelDistance);
        return abs(dist);
      }`),e.pointDistanceEnabled&&t.fragment.code.add(b`
    float sphereDistancePixels(vec4 sphere, vec3 pos) {
      float dist = distance(sphere.xyz, pos) - sphere.w;
      float width = fwidth(dist);
      dist /= min(width, maxPixelDistance);
      return abs(dist);
    }
    `),e.intersectsLineEnabled&&t.fragment.code.add(b`
    float lineDistancePixels(vec3 start, vec3 dir, float radius, vec3 pos) {
      float dist = length(cross(dir, pos - start)) / (length(pos) * perScreenPixelRatio);
      return abs(dist) - radius;
    }
    `),(e.lineVerticalPlaneEnabled||e.intersectsLineEnabled)&&t.fragment.code.add(b`
    bool pointIsWithinLine(vec3 pos, vec3 start, vec3 end) {
      vec3 dir = end - start;
      float t2 = dot(dir, pos - start);
      float l2 = dot(dir, dir);
      return t2 >= 0.0 && t2 <= l2;
    }
    `),t.fragment.code.add(b`
  void main() {
    vec3 pos;
    vec3 normal;
    float depthDiscontinuityAlpha;

    if (!laserlineReconstructFromDepth(pos, normal, depthDiscontinuityAlpha)) {
      discard;
    }

    vec4 color = vec4(0, 0, 0, 0);
  `),e.heightManifoldEnabled&&t.fragment.code.add(b`
    {
      float heightManifoldAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, abs(dot(normal, heightPlane.xyz)));
      vec4 heightManifoldColor = laserlineProfile(planeDistancePixels(heightPlane, pos));
      color = max(color, heightManifoldColor * heightManifoldAlpha);
    }
    `),e.pointDistanceEnabled&&t.fragment.code.add(b`
    {
      // distance to sphere
      float pointDistanceSphereDistance = sphereDistancePixels(pointDistanceSphere, pos);
      vec4 pointDistanceSphereColor = laserlineProfile(pointDistanceSphereDistance);
      float pointDistanceSphereAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, abs(dot(normal, normalize(pos - pointDistanceSphere.xyz))));

      color = max(color, pointDistanceSphereColor * pointDistanceSphereAlpha);
    }
    `),e.lineVerticalPlaneEnabled&&t.fragment.code.add(b`
    {
      if (pointIsWithinLine(pos, lineVerticalStart, lineVerticalEnd)) {
        float lineVerticalDistance = planeDistancePixels(lineVerticalPlane, pos);

        vec4 lineVerticalColor = laserlineProfile(lineVerticalDistance);
        float lineVerticalAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, abs(dot(normal, lineVerticalPlane.xyz)));

        color = max(color, lineVerticalColor * lineVerticalAlpha);
      }
    }
    `),e.intersectsLineEnabled&&t.fragment.code.add(b`
    {
      if (pointIsWithinLine(pos, intersectsLineStart, intersectsLineEnd)) {
        float intersectsLineDistance = lineDistancePixels(intersectsLineStart, intersectsLineDirection, intersectsLineRadius, pos);
        vec4 intersectsLineColor = laserlineProfile(intersectsLineDistance);
        float intersectsLineAlpha = 1.0 - smoothstep(angleCutoff.x, angleCutoff.y, 1.0 - abs(dot(normal, intersectsLineDirection)));

        color = max(color, intersectsLineColor * intersectsLineAlpha);
      }
    }
    `),t.fragment.code.add(b`
    gl_FragColor = laserlineOutput(color * depthDiscontinuityAlpha);
  }
  `),t}});class ne extends V{initializeProgram(e){const t=ne.shader.get(),i=this.configuration,n=t.build(i);return new L(e.rctx,n.generateSource("vertex"),n.generateSource("fragment"),E)}bind(e,t){this.program.setUniform3fv("innerColor",e.innerColor),this.program.setUniform1f("innerWidth",e.innerWidth*t.pixelRatio),this.program.setUniform3fv("glowColor",e.glowColor),this.program.setUniform1f("glowWidth",e.glowWidth*t.pixelRatio),this.program.setUniform1f("glowFalloff",e.glowFalloff),this.program.setUniform1f("globalAlpha",e.globalAlpha),this.configuration.contrastControlEnabled&&this.program.setUniform1f("globalAlphaContrastBoost",null!=e.globalAlphaContrastBoost?e.globalAlphaContrastBoost:1);const i=null!=e.angleCutoff?e.angleCutoff:te;this.program.setUniform2f("angleCutoff",Math.cos(i),Math.cos(Math.max(0,i-n(2)))),this.configuration.intersectsLineEnabled&&this.program.setUniform1f("perScreenPixelRatio",t.perScreenPixelRatio)}initializePipeline(){return S({blending:A(1,771),colorWrite:M})}}ne.shader=new D(ie,(()=>Promise.resolve().then((function(){return ie}))));class re extends P{constructor(){super(...arguments),this.heightManifoldEnabled=!1,this.pointDistanceEnabled=!1,this.lineVerticalPlaneEnabled=!1,this.intersectsLineEnabled=!1,this.contrastControlEnabled=!1}}e([x()],re.prototype,"heightManifoldEnabled",void 0),e([x()],re.prototype,"pointDistanceEnabled",void 0),e([x()],re.prototype,"lineVerticalPlaneEnabled",void 0),e([x()],re.prototype,"intersectsLineEnabled",void 0),e([x()],re.prototype,"contrastControlEnabled",void 0);const ae=r(),se=u(),oe={glowColor:[1,.5,0],glowWidth:8,glowFalloff:8,innerColor:[1,1,1],innerWidth:1,globalAlpha:.75,angleCutoff:n(6),globalAlphaContrastBoost:2};function le(e,t,i,n){const r=ae,a=se;l(r,t,n),o(a,i),a[3]=0,g(a,a,n),j.fromPositionAndNormal(r,a,e)}class ce{constructor(e,t={},i={contrastControlEnabled:!1}){this._technique=null,this._projInfo=u(),this._zScale=_(),this._heightManifoldEnabled=!1,this._heightManifoldTarget=r(),this._pointDistanceEnabled=!1,this._pointDistanceOrigin=r(),this._pointDistanceTarget=r(),this._lineVerticalPlaneEnabled=!1,this._lineVerticalPlaneSegment=q.create(),this._intersectsLineEnabled=!1,this._intersectsLineSegment=q.create(),this._intersectsLineRadius=3,this._intersectsLineInfinite=!1,this._pathVerticalPlaneEnabled=!1,this._pathVerticalPlaneData=null,this._pathTechnique=null,this.canRender=!0,this._tempNormal=r(),this._tempDir=r(),this._tempUp=r(),this._tempVec3A=r(),this._tempVec3B=r(),this._tempVec4=u(),this._tempPlane=j.create(),this._tempSphere=T.create(),this._renderCoordsHelper=e,this._params=w(t,oe),this._config=i}get renderSlots(){return[this._config.contrastControlEnabled?17:16]}get needsLinearDepth(){return!0}get heightManifoldEnabled(){return this._heightManifoldEnabled}set heightManifoldEnabled(e){this._heightManifoldEnabled!==e&&(this._heightManifoldEnabled=e,this._requestRender())}get heightManifoldTarget(){return this._heightManifoldTarget}set heightManifoldTarget(e){o(this._heightManifoldTarget,e),this._requestRender()}get pointDistanceEnabled(){return this._pointDistanceEnabled}set pointDistanceEnabled(e){e!==this._pointDistanceEnabled&&(this._pointDistanceEnabled=e,this._requestRender())}get pointDistanceTarget(){return this._pointDistanceTarget}set pointDistanceTarget(e){o(this._pointDistanceTarget,e),this._requestRender()}get pointDistanceOrigin(){return this._pointDistanceOrigin}set pointDistanceOrigin(e){o(this._pointDistanceOrigin,e),this._requestRender()}get lineVerticalPlaneEnabled(){return this._lineVerticalPlaneEnabled}set lineVerticalPlaneEnabled(e){e!==this._lineVerticalPlaneEnabled&&(this._lineVerticalPlaneEnabled=e,this._requestRender())}get lineVerticalPlaneSegment(){return this._lineVerticalPlaneSegment}set lineVerticalPlaneSegment(e){q.copy(e,this._lineVerticalPlaneSegment),this._requestRender()}get intersectsLineEnabled(){return this._intersectsLineEnabled}set intersectsLineEnabled(e){e!==this._intersectsLineEnabled&&(this._intersectsLineEnabled=e,this._requestRender())}get intersectsLineSegment(){return this._intersectsLineSegment}set intersectsLineSegment(e){q.copy(e,this._intersectsLineSegment),this._requestRender()}get intersectsLineRadius(){return this._intersectsLineRadius}set intersectsLineRadius(e){e!==this._intersectsLineRadius&&(this._intersectsLineRadius=e,this._requestRender())}get intersectsLineInfinite(){return this._intersectsLineInfinite}set intersectsLineInfinite(e){e!==this._intersectsLineInfinite&&(this._intersectsLineInfinite=e,this._requestRender())}get pathVerticalPlaneEnabled(){return this._pathVerticalPlaneEnabled}set pathVerticalPlaneEnabled(e){e!==this._pathVerticalPlaneEnabled&&(this._pathVerticalPlaneEnabled=e,t(this._pathVerticalPlaneData)&&this._requestRender())}set pathVerticalPlaneVertices(e){i(this._pathVerticalPlaneData)&&(this._pathVerticalPlaneData=new Y(this._renderCoordsHelper)),this._pathVerticalPlaneData.vertices=e,this.pathVerticalPlaneEnabled&&this._requestRender()}set pathVerticalPlaneBuffers(e){i(this._pathVerticalPlaneData)&&(this._pathVerticalPlaneData=new Y(this._renderCoordsHelper)),this._pathVerticalPlaneData.buffers=e,this.pathVerticalPlaneEnabled&&this._requestRender()}setParameterValues(e){C(this._params,e)&&this._requestRender()}initializeRenderContext(e){this._initContext=e;const t=e.rctx;this._quadVAO=F(t),this._techniqueRepository=e.shaderTechniqueRep,this._techniqueConfig=new re,this._pathTechniqueConfig=new K}uninitializeRenderContext(){this._quadVAO.dispose(),this._quadVAO=null,t(this._technique)&&this._techniqueRepository.release(this._technique),t(this._pathVerticalPlaneData)&&this._pathVerticalPlaneData.dispose(),t(this._pathTechnique)&&this._techniqueRepository.release(this._pathTechnique)}render(e){const t=this.heightManifoldEnabled||this.pointDistanceEnabled||this.lineVerticalPlaneSegment||this.intersectsLineEnabled,i=this.pathVerticalPlaneEnabled;if(!t&&!i)return!0;const n=e.camera;return W(n.projectionMatrix,n.fullWidth,n.fullHeight,this._projInfo,this._zScale),t&&this.renderUnified(e),i&&this.renderPath(e),!0}renderUnified(e){const t=e.rctx,i=this._selectTechnique(),n=i.program;t.bindProgram(n),i.bindPipelineState(t),this.bindGlobalUniforms(e,n),this.bindHeightManifoldUniforms(e,n),this.bindPointDistanceUniforms(e,n),this.bindLineVerticalPlaneUniforms(e,n),this.bindIntersectsLineUniforms(e,n),i.bind(this._params,e.camera),t.bindVAO(this._quadVAO),t.drawArrays(5,0,4)}renderPath(e){if(i(this._pathVerticalPlaneData))return;this._pathTechniqueConfig.contrastControlEnabled=this._config.contrastControlEnabled,this._pathTechnique=this._techniqueRepository.acquireAndReleaseExisting(J,this._pathTechniqueConfig,this._pathTechnique);const t=e.rctx,n=this._pathTechnique,r=n.program;t.bindProgram(r),n.bindPipelineState(t),this.bindGlobalUniforms(e,r),n.bind(this._params,this._pathVerticalPlaneData.origin,e.camera),this._pathVerticalPlaneData.draw(e.rctx)}bindHeightManifoldUniforms(e,t){if(!this.heightManifoldEnabled)return;const i=this._tempVec3A,n=this._tempPlane;this._renderCoordsHelper.worldUpAtPosition(this._heightManifoldTarget,i),le(n,this._heightManifoldTarget,i,e.camera.viewMatrix),t.setUniform4fv("heightPlane",n)}bindPointDistanceUniforms(e,t){if(!this._pointDistanceEnabled)return;const i=e.camera,n=this._tempSphere;o(n.center,this._pointDistanceOrigin),l(n.center,n.center,i.viewMatrix),n.radius=c(this._pointDistanceOrigin,this._pointDistanceTarget),t.setUniform4f("pointDistanceSphere",n.center[0],n.center[1],n.center[2],n.radius)}bindLineVerticalPlaneUniforms(e,t){if(!this._lineVerticalPlaneEnabled)return;const i=this._renderCoordsHelper,n=e.camera,r=this._tempPlane,a=this._tempVec3A,s=this._tempUp,c=this._tempDir,p=this._tempNormal;q.pointAt(this._lineVerticalPlaneSegment,.5,a),i.worldUpAtPosition(a,s),d(c,this._lineVerticalPlaneSegment.vector),h(p,s,c),d(p,p),le(r,this._lineVerticalPlaneSegment.origin,p,n.viewMatrix),t.setUniform4fv("lineVerticalPlane",r);const u=this._tempVec3A;o(u,this._lineVerticalPlaneSegment.origin),i.setAltitude(0,u),l(u,u,n.viewMatrix),t.setUniform3fv("lineVerticalStart",u);const m=this._tempVec3B;f(m,this._lineVerticalPlaneSegment.origin,this._lineVerticalPlaneSegment.vector),i.setAltitude(0,m),l(m,m,n.viewMatrix),t.setUniform3fv("lineVerticalEnd",m)}bindIntersectsLineUniforms(e,t){if(!this._intersectsLineEnabled)return;const i=he,n=fe;if(this._intersectsLineInfinite){const t=e.camera;if(R.fromRay(z.wrap(this._intersectsLineSegment.origin,this._intersectsLineSegment.vector),de),de.c0=-Number.MAX_VALUE,!O.intersectClipRay(t.frustum.planes,de))return;R.getStart(de,i),R.getEnd(de,n)}else o(i,this._intersectsLineSegment.origin),f(n,this._intersectsLineSegment.origin,this._intersectsLineSegment.vector);const r=this._tempVec3A;l(r,i,e.camera.viewMatrix),t.setUniform3fv("intersectsLineStart",r);const a=this._tempVec4;o(a,this._intersectsLineSegment.vector),this._tempVec4[3]=0,g(this._tempVec4,this._tempVec4,e.camera.viewMatrix),l(n,n,e.camera.viewMatrix),t.setUniform3fv("intersectsLineEnd",n),d(a,a),t.setUniform3f("intersectsLineDirection",a[0],a[1],a[2]),t.setUniform1f("intersectsLineRadius",this._intersectsLineRadius)}bindGlobalUniforms(e,t){const i=e.camera;t.setUniform4fv("projInfo",this._projInfo),t.setUniform2fv("zScale",this._zScale),t.setUniform2f("nearFar",i.near,i.far),this._heightManifoldEnabled?t.setUniform1f("maxPixelDistance",2*i.computeScreenPixelSizeAt(this._heightManifoldTarget)):this._pointDistanceEnabled?t.setUniform1f("maxPixelDistance",2*i.computeScreenPixelSizeAt(this._pointDistanceTarget)):this._lineVerticalPlaneEnabled&&t.setUniform1f("maxPixelDistance",2*i.computeScreenPixelSizeAt(this._lineVerticalPlaneSegment.origin)),t.setUniform1i("depthMap",0),e.rctx.bindTexture(e.offscreenRenderingHelper.linearDepthTexture,0),t.setUniform1i("frameColor",1),e.rctx.bindTexture(e.offscreenRenderingHelper.mainColorTexture,1)}_requestRender(){this._initContext&&this._initContext.requestRender()}_selectTechnique(){return this._techniqueConfig.heightManifoldEnabled=this.heightManifoldEnabled,this._techniqueConfig.lineVerticalPlaneEnabled=this.lineVerticalPlaneEnabled,this._techniqueConfig.pointDistanceEnabled=this.pointDistanceEnabled,this._techniqueConfig.intersectsLineEnabled=this.intersectsLineEnabled,this._techniqueConfig.contrastControlEnabled=this._config.contrastControlEnabled,this._technique=this._techniqueRepository.acquireAndReleaseExisting(ne,this._techniqueConfig,this._technique),this._technique}}const de=R.create(),he=r(),fe=r();export{ce as L,te as d};
