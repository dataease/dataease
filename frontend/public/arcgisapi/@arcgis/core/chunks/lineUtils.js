/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import"./object.js";import{s as t}from"../core/lang.js";import{b as r,L as i,i as s,d as o,u as a,c as n}from"./Logger.js";import{o as d}from"./metadata.js";import{property as l}from"../core/accessorSupport/decorators/property.js";import h from"../core/Accessor.js";import{r as c,a as u,P as m}from"../core/scheduling.js";import{all as p}from"../core/promiseUtils.js";import"./ensureType.js";import{subclass as g}from"../core/accessorSupport/decorators/subclass.js";import"../core/urlUtils.js";import"./resourceExtension.js";import f from"../geometry/SpatialReference.js";import{n as _,c as v,b as y}from"./mathUtils2.js";import{c as w,f as R}from"./vec3f64.js";import{c as T,f as b,g as x,o as S,s as E,a as C,n as O,d as I}from"./vec3.js";import M from"../core/Handles.js";import{init as D}from"../core/watchUtils.js";import{a as P,b as A,m as U,o as L,i as H,t as N}from"./mat4.js";import{c as j}from"./aaBoundingRect.js";import{projectBuffer as z}from"../geometry/projection.js";import{Z as F,O as G}from"./vec4f64.js";import{p as V}from"./triangulationUtils.js";import{I as W,d as B,c as k}from"./quatf64.js";import{s as q}from"./MapUtils.js";import{c as Z}from"./vec2f64.js";import{g as Q,S as Y,T as $,s as X,e as J,C as K,f as ee,p as te,a as re,b as ie,D as se,V as oe,h as ae,i as ne,j as de,k as le,o as he,R as ce,G as ue,_ as me,P as pe,$ as ge}from"./PiUtils.glsl.js";import{P as fe}from"./Program.js";import{F as _e,m as ve,a as ye,d as we,s as Re,T as Te}from"./isWebGL2Context.js";import{g as be}from"./glUtil.js";import{R as xe,L as Se,f as Ee}from"./ColorMaterial.js";import{a as Ce,V as Oe,g as Ie,s as Me}from"./Util.js";import{a as De,G as Pe,f as Ae,c as Ue}from"./Geometry.js";import{g as Le,V as He,B as Ne,a as je,v as ze}from"./VertexArrayObject.js";import{O as Fe,a as Ge,r as Ve,c as We}from"./Object3D.js";import{I as Be}from"./intersectorUtils.js";import{C as ke}from"./Camera.js";import{D as qe}from"./DefaultTextureUnits.js";import{b as Ze}from"./elevationAlignmentUtils.js";import"./RenderingContext.js";import{P as Qe,F as Ye,R as $e}from"./PhysicallyBasedRendering.glsl.js";import{R as Xe,S as Je}from"./CameraSpace.glsl.js";import{S as Ke}from"./lineStippleUtils.js";class et{constructor(e=w(),t=R(.57735,.57735,.57735),r=!0){this.intensity=e,this.direction=t,this.castShadows=r}}class tt{constructor(e=w(),t=R(.57735,.57735,.57735)){this.intensity=w(),this.direction=w(),this.intensity=e,this.direction=t}}class rt{constructor(e=w()){this.intensity=e}}class it{constructor(){this.sh={r:[0],g:[0],b:[0]}}}let st=class extends h{constructor(){super(...arguments),this.SCENEVIEW_HITTEST_RETURN_INTERSECTOR=!1,this.SCENEVIEW_LOCKING_LOG=!1,this.HIGHLIGHTS_GRID_OPTIMIZATION_DISABLED=!1,this.HIGHLIGHTS_PROFILE_TO_CONSOLE=!1,this.DECONFLICTOR_SHOW_VISIBLE=!1,this.DECONFLICTOR_SHOW_INVISIBLE=!1,this.DECONFLICTOR_SHOW_GRID=!1,this.LABELS_SHOW_BORDER=!1,this.OVERLAY_DRAW_DEBUG_TEXTURE=!1,this.OVERLAY_SHOW_CENTER=!1,this.SHOW_POI=!1,this.TERRAIN_DEBUG_POPUP=!1,this.TESTS_DISABLE_UPDATE_THRESHOLDS=!1,this.DISABLE_DECONFLICTOR_VISIBILITY_OFFSET=!1,this.DISABLE_ELEVATION_ALIGNERS_ITERATIVE_UPDATES=!1,this.DRAW_MESH_GEOMETRY_NORMALS=!1,this.FEATURE_TILE_FETCH_SHOW_TILES=!1,this.FEATURE_TILE_TREE_SHOW_TILES=!1,this.I3S_TREE_SHOW_TILES=!1,this.I3S_SHOW_MODIFICATIONS=!1,this.ENABLE_PROFILE_DEPTH_RANGE=!1,this.DISABLE_FAST_UPDATES=!1,this.LOD_INSTANCE_RENDERER_DISABLE_UPDATES=!1,this.LOD_INSTANCE_RENDERER_COLORIZE_BY_LEVEL=!1,this.EDGES_SHOW_HIDDEN_TRANSPARENT_EDGES=!1}};e([l()],st.prototype,"SCENEVIEW_HITTEST_RETURN_INTERSECTOR",void 0),e([l()],st.prototype,"SCENEVIEW_LOCKING_LOG",void 0),e([l()],st.prototype,"HIGHLIGHTS_GRID_OPTIMIZATION_DISABLED",void 0),e([l()],st.prototype,"HIGHLIGHTS_PROFILE_TO_CONSOLE",void 0),e([l()],st.prototype,"DECONFLICTOR_SHOW_VISIBLE",void 0),e([l()],st.prototype,"DECONFLICTOR_SHOW_INVISIBLE",void 0),e([l()],st.prototype,"DECONFLICTOR_SHOW_GRID",void 0),e([l()],st.prototype,"LABELS_SHOW_BORDER",void 0),e([l()],st.prototype,"OVERLAY_DRAW_DEBUG_TEXTURE",void 0),e([l()],st.prototype,"OVERLAY_SHOW_CENTER",void 0),e([l()],st.prototype,"SHOW_POI",void 0),e([l()],st.prototype,"TERRAIN_DEBUG_POPUP",void 0),e([l()],st.prototype,"TESTS_DISABLE_UPDATE_THRESHOLDS",void 0),e([l()],st.prototype,"DISABLE_DECONFLICTOR_VISIBILITY_OFFSET",void 0),e([l()],st.prototype,"DISABLE_ELEVATION_ALIGNERS_ITERATIVE_UPDATES",void 0),e([l()],st.prototype,"DRAW_MESH_GEOMETRY_NORMALS",void 0),e([l()],st.prototype,"FEATURE_TILE_FETCH_SHOW_TILES",void 0),e([l()],st.prototype,"FEATURE_TILE_TREE_SHOW_TILES",void 0),e([l()],st.prototype,"I3S_TREE_SHOW_TILES",void 0),e([l()],st.prototype,"I3S_SHOW_MODIFICATIONS",void 0),e([l()],st.prototype,"ENABLE_PROFILE_DEPTH_RANGE",void 0),e([l()],st.prototype,"DISABLE_FAST_UPDATES",void 0),e([l()],st.prototype,"LOD_INSTANCE_RENDERER_DISABLE_UPDATES",void 0),e([l()],st.prototype,"LOD_INSTANCE_RENDERER_COLORIZE_BY_LEVEL",void 0),e([l()],st.prototype,"EDGES_SHOW_HIDDEN_TRANSPARENT_EDGES",void 0),st=e([g("esri.views.3d.support.DebugFlags")],st);const ot=new st;class at{constructor(e,t,r,i){this._renderTarget=null,this.id=`${t}_${++r}`,this._renderTarget=new _e(e,{colorTarget:0,depthStencilTarget:0},{target:3553,pixelFormat:6408,dataType:5121,wrapMode:33071,samplingMode:9987,hasMipmap:i,maxAnisotropy:8,width:0,height:0})}dispose(){this._renderTarget.dispose(),this._renderTarget=null}getTexture(){return this._renderTarget?this._renderTarget.colorTexture:null}isValid(){return null!==this._renderTarget}resize(e,t){this._renderTarget.resize(e,t)}bind(e){e.bindFramebuffer(this._renderTarget)}generateMipMap(){this._renderTarget.colorTexture.descriptor.hasMipmap&&this._renderTarget.colorTexture.generateMipmap()}disposeRenderTargetMemory(){this._renderTarget&&this._renderTarget.resize(0,0)}getGpuMemoryUsage(){let e=0;return this._renderTarget&&(e+=Le(this._renderTarget)),e}}class nt{constructor(e,t){this.vec3=e,this.id=t}}function dt(e,t,r,i){return new nt(R(e,t,r),i)}class lt{constructor(e,t){this.extent=j(),this.resolution=0,this.renderLocalOrigin=dt(0,0,0,"O"),this.pixelRatio=1,this.renderTargets={color:{fbo:new at(e,"overlay",t,!0),valid:!1,lastUsed:1/0},colorWithoutRasterImage:{fbo:new at(e,"overlayWithoutRasterImage",t,!0),valid:!1,lastUsed:1/0},highlight:{fbo:new at(e,"overlayHighlight",t,!1),valid:!1,lastUsed:1/0},water:{fbo:new at(e,"overlayWaterMask",t,!0),valid:!1,lastUsed:1/0},occluded:{fbo:new at(e,"overlayOccluded",t,!0),valid:!1,lastUsed:1/0}}}dispose(){this.renderTargets.color.fbo.dispose(),this.renderTargets.colorWithoutRasterImage.fbo.dispose(),this.renderTargets.highlight.fbo.dispose(),this.renderTargets.water.fbo.dispose(),this.renderTargets.occluded.fbo.dispose()}drawRenderTargets(e,t,r){const i=this.renderTargets;i.color.valid=e.drawPass(0,i.color.fbo,t),i.highlight.valid=e.drawPass(5,i.highlight.fbo,t),i.water.valid=e.drawPass(3,i.water.fbo,t),i.occluded.valid=e.drawPass(0,i.occluded.fbo,t,1),i.colorWithoutRasterImage.valid=r&&e.drawPass(0,i.colorWithoutRasterImage.fbo,t,2)}computeRenderTargetValidityBitfield(){const e=this.renderTargets;return+e.color.valid|+e.colorWithoutRasterImage.valid<<1|+e.highlight.valid<<2|+e.water.valid<<3|+e.occluded.valid<<4}validateUsage(e,t){if(e.valid)e.lastUsed=t;else if(t-e.lastUsed>ht)e.fbo.disposeRenderTargetMemory(),e.lastUsed=1/0;else if(e.lastUsed<1/0)return!0;return!1}collectUnusedMemory(e){let t=!1;return t=this.validateUsage(this.renderTargets.color,e)||t,t=this.validateUsage(this.renderTargets.colorWithoutRasterImage,e)||t,t=this.validateUsage(this.renderTargets.highlight,e)||t,t=this.validateUsage(this.renderTargets.occluded,e)||t,t=this.validateUsage(this.renderTargets.water,e)||t,t}getGpuMemoryUsage(){return this.renderTargets.color.fbo.getGpuMemoryUsage()+this.renderTargets.colorWithoutRasterImage.fbo.getGpuMemoryUsage()+this.renderTargets.highlight.fbo.getGpuMemoryUsage()+this.renderTargets.water.fbo.getGpuMemoryUsage()+this.renderTargets.occluded.fbo.getGpuMemoryUsage()}}const ht=1e3;class ct{constructor(){this._uniforms={proj:[],shadowMapDistance:[],viewportPixelSz:[],lightingMainDirection:[]}}dispose(){this._uniforms=null}getPrograms(e){return this._uniforms[e]||[]}subscribeProgram(e){for(const t in this._uniforms)e.hasUniform(t)&&this._uniforms[t].push(e)}unsubscribeProgram(e){for(const t in this._uniforms)c(this._uniforms[t],e)}}class ut{constructor(e){this.technique=e,this.refCount=0,this.refZeroFrame=0}}class mt{constructor(e){this._context=e,this._perConstructorInstances=new Map,this._frameCounter=0,this._keepAliveFrameCount=1200}get viewingMode(){return this._context.viewingMode}get constructionContext(){return this._context}dispose(){this._perConstructorInstances.forEach((e=>e.forEach((e=>e.technique.dispose())))),this._perConstructorInstances.clear()}acquire(e,t){const r=t.key;let i=this._perConstructorInstances.get(e);i||(i=new Map,this._perConstructorInstances.set(e,i));let s=i.get(r);return void 0===s&&(s=new ut(new e(this._context,t)),i.set(r,s)),++s.refCount,s.technique}acquireAndReleaseExisting(e,t,i){return r(i)?this.acquire(e,t):t.key===i.key&&i instanceof e?i:(this.release(i),this.acquire(e,t))}release(e){const t=this._perConstructorInstances.get(e.constructor).get(e.key);t.refCount-=1,0===t.refCount&&(t.refZeroFrame=this._frameCounter)}frameUpdate(){this._frameCounter++,this._perConstructorInstances.forEach(((e,t)=>{e.forEach(((t,r)=>{0===t.refCount&&t.refZeroFrame+this._keepAliveFrameCount<this._frameCounter&&(t.technique.dispose(),e.delete(r))})),0===e.size&&this._perConstructorInstances.delete(t)}))}getProgramsUsingUniform(e){return this._context.commonUniformStore.getPrograms(e)}async hotReload(){const e=new Array;this._perConstructorInstances.forEach(((t,r)=>{e.push((async(e,t)=>{const r=t.shader;r&&(await r.reload(),e.forEach((e=>{e.technique.reload(this._context)})))})(t,r))})),await p(e)}}const pt=[{output:0,name:"color"},{output:1,name:"depth"},{output:2,name:"normal"},{output:3,name:"depthShadowMap"},{output:4,name:"highlight"},{output:5,name:"draped"},{output:6,name:"occlusion"},{output:7,name:"alpha"}];function gt(e,t){return e+"_"+pt.find((e=>e.output===t)).name}const ft=i.getLogger("esri.views.3d.webgl-engine.lib.GLMaterialRep");class _t{constructor(e){this.refCnt=0,this.glMaterial=e}incRefCnt(){++this.refCnt}decRefCnt(){--this.refCnt,Ce(this.refCnt>=0)}getRefCnt(){return this.refCnt}getGLMaterial(){return this.glMaterial}}class vt{constructor(e,t,r){this._textureRep=e,this._techniqueRep=t,this.onMaterialChanged=r,this._id2glMaterialRef=new Map}dispose(){this._textureRep.dispose()}acquire(e,t){this.ownMaterial(e);const r=gt(e.id,t);let i=this._id2glMaterialRef.get(r);if(null==i){const s=e.getGLMaterial({material:e,techniqueRep:this._techniqueRep,textureRep:this._textureRep,output:t});return i=new _t(s),i.incRefCnt(),this._id2glMaterialRef.set(r,i),s}return i.incRefCnt(),i.getGLMaterial()}release(e,t){const r=gt(e.id,t),i=this._id2glMaterialRef.get(r);if(i.decRefCnt(),0===i.getRefCnt()){const e=i.getGLMaterial();e&&e.dispose(),this._id2glMaterialRef.delete(r)}}materialChanged(e){for(const t of pt)if(5!==t.output&&6!==t.output){const r=this._id2glMaterialRef.get(gt(e.id,t.output));if(r&&r.getGLMaterial()){const e=r.getGLMaterial();e.updateParameters&&e.updateParameters()}}this.onMaterialChanged&&this.onMaterialChanged(e)}ownMaterial(e){s(e.materialRepository)&&e.materialRepository!==this&&ft.error("Material is already owned by a different material repository"),e.materialRepository=this}}let yt=0;class wt{constructor(){this.ROOT_ORIGIN_ID="rg_root_"+yt++,this._origins=new Map,this._gridSize=42e5}getOrigin(e){const t=this._origins.get(this.ROOT_ORIGIN_ID);if(null==t){if(s(wt.testOrigin)){const t=wt.testOrigin;return this._origins.set(this.ROOT_ORIGIN_ID,dt(t[0],t[1],t[2],this.ROOT_ORIGIN_ID)),this.getOrigin(e)}const t=dt(e[0]+Math.random()-.5,e[1]+Math.random()-.5,e[2]+Math.random()-.5,this.ROOT_ORIGIN_ID);return this._origins.set(this.ROOT_ORIGIN_ID,t),t}T(Rt,e,t.vec3),Rt[0]=Math.abs(Rt[0]),Rt[1]=Math.abs(Rt[1]),Rt[2]=Math.abs(Rt[2]);const r=this._gridSize;if(Rt[0]<r&&Rt[1]<r&&Rt[2]<r)return t;const i=Math.round(e[0]/r),o=Math.round(e[1]/r),a=Math.round(e[2]/r),n=`rg_${i}/${o}/${a}`;let d=this._origins.get(n);return d||(d=dt(i*r,o*r,a*r,n),this._origins.set(n,d)),d}_drawOriginBox(e){const t=window.view,r=t._stage;if(null==this._object){this._material=new xe({width:2,color:[0,1,0,1]},"GridLocalOriginMaterial"),r.add(3,this._material);const e=new Se("GridLocalOrigin",{isPickable:!1});r.add(0,e),this._object=new Fe({idHint:"GridLocalOrigin",castShadow:!1}),r.add(1,this._object),e.addObject(this._object),r.addToViewContent([e.id])}const i=[0,1,5,4,0,2,1,7,6,2,0,1,3,7,5,4,6,2,0],s=i.length,o=new Float32Array(3*s),a=new Uint32Array(2*(s-1)),n=.5*this._gridSize;for(let t=0;t<s;t++)o[3*t+0]=e[0]+(1&i[t]?n:-n),o[3*t+1]=e[1]+(2&i[t]?n:-n),o[3*t+2]=e[2]+(4&i[t]?n:-n),t>0&&(a[2*t+0]=t-1,a[2*t+1]=t);z(o,f.WebMercator,0,o,t.spatialReference,0,s);const d={};d[Oe.POSITION]={size:3,data:o};const l={};l[Oe.POSITION]=a;const h=new De(d,l,"line"),c=new Pe(h,"GridLocalOriginBox");r.add(2,c),this._object.addGeometry(c,this._material,W),console.log(this._origins.size,e)}}const Rt=w();!function(e){e.testOrigin=null}(wt||(wt={}));var Tt=wt;class bt{constructor(e,t,r,i,s,o){this.camera=null,this.lastFrameCamera=new ke,this.pass=0,this.slot=0,this.highlightDepthTexture=null,this.renderOccludedMask=xt,this.hasOccludees=!1,this.rctx=e,this.offscreenRenderingHelper=t,this.scenelightingData=r,this.shadowMap=i,this.ssaoHelper=s,this.sliceHelper=o}resetRenderOccludedMask(){this.renderOccludedMask=xt}get isHighlightPass(){return 5===this.pass}}const xt=13;function St(e){const t=new Map;let r=null,i=null;const s=e=>{if(e===r)return i;let s=t.get(e);return s||(s={toAdd:[],numToAdd:-1,toRemove:[],numToRemove:-1,toUpdate:[],numToUpdate:-1},t.set(e,s)),r=e,i=s,s};for(let t=0;t<e.numToAdd;t++){const r=e.toAdd[t];if(Et(r)){s(r.material).toAdd.push(r)}}for(let t=0;t<e.numToRemove;t++){const r=e.toRemove[t];if(Et(r)){s(r.material).toRemove.push(r)}}for(let t=0;t<e.numToUpdate;t++){const r=e.toUpdate[t];if(Et(r.renderGeometry)){s(r.renderGeometry.material).toUpdate.push(r)}}return t}function Et(e){return t=e.data,Ie(t.indices).length>=1;var t}class Ct{constructor(e){null==e?e=16:e<65536&&(e=_(e)),this._array=new Float32Array(e),this._size=0}resize(e,t){if(this._size=e,this._size>this._array.length){let e=this._array.length||1;for(;e<this._size;)e*=2;const r=new Float32Array(e);return t&&r.set(this._array),this._array=r,!0}const r=2*this._size;if(r<=this._array.length){let e=this._array.length;for(;e>=r;)e=Math.floor(e/2);const i=new Float32Array(e);return t&&i.set(this._array.subarray(0,e)),this._array=i,!0}return!1}append(e){const t=this._size;this.resize(this._size+e.length,!0),this._array.set(e,t)}erase(e,t){for(let r=e;r<t;++r)this._array[r]=0}get array(){return this._array}get size(){return this._size}}function Ot(e){e.fragment.uniforms.add("lastFrameColorMap","sampler2D"),e.fragment.uniforms.add("reprojectionMat","mat4"),e.fragment.uniforms.add("rpProjectionMat","mat4"),e.fragment.code.add(Q`
  vec2 reprojectionCoordinate(vec3 projectionCoordinate)
  {
    vec4 zw = rpProjectionMat * vec4(0.0, 0.0, -projectionCoordinate.z, 1.0);
    vec4 reprojectedCoord = reprojectionMat * vec4(zw.w * (projectionCoordinate.xy * 2.0 - 1.0), zw.z, zw.w);
    reprojectedCoord.xy /= reprojectedCoord.w;
    return reprojectedCoord.xy*0.5 + 0.5;
  }
  `)}function It(e,t){e.fragment.uniforms.add("nearFar","vec2"),e.fragment.uniforms.add("depthMapView","sampler2D"),e.fragment.uniforms.add("ssrViewMat","mat4"),e.fragment.uniforms.add("invResolutionHeight","float"),e.include(Xe),e.include(Ot),e.fragment.code.add(Q`
  const int maxSteps = ${t.highStepCount?"150;":"75;"}

  vec4 applyProjectionMat(mat4 projectionMat, vec3 x)
  {
    vec4 projectedCoord =  projectionMat * vec4(x, 1.0);
    projectedCoord.xy /= projectedCoord.w;
    projectedCoord.xy = projectedCoord.xy*0.5 + 0.5;
    return projectedCoord;
  }

  vec3 screenSpaceIntersection(vec3 dir, vec3 startPosition, vec3 viewDir, vec3 normal)
  {
    vec3 viewPos = startPosition;
    vec3 viewPosEnd = startPosition;

    // Project the start position to the screen
    vec4 projectedCoordStart = applyProjectionMat(rpProjectionMat, viewPos);
    vec3  Q0 = viewPos / projectedCoordStart.w; // homogeneous camera space
    float k0 = 1.0/ projectedCoordStart.w;

    // advance the position in the direction of the reflection
    viewPos += dir;

    vec4 projectedCoordVanishingPoint = applyProjectionMat(rpProjectionMat, dir);

    // Project the advanced position to the screen
    vec4 projectedCoordEnd = applyProjectionMat(rpProjectionMat, viewPos);
    vec3  Q1 = viewPos / projectedCoordEnd.w; // homogeneous camera space
    float k1 = 1.0/ projectedCoordEnd.w;

    // calculate the reflection direction in the screen space
    vec2 projectedCoordDir = (projectedCoordEnd.xy - projectedCoordStart.xy);
    vec2 projectedCoordDistVanishingPoint = (projectedCoordVanishingPoint.xy - projectedCoordStart.xy);

    float yMod = min(abs(projectedCoordDistVanishingPoint.y), 1.0);

    float projectedCoordDirLength = length(projectedCoordDir);
    float maxSt = float(maxSteps);

    // normalize the projection direction depending on maximum steps
    // this determines how blocky the reflection looks
    vec2 dP = yMod * (projectedCoordDir)/(maxSt * projectedCoordDirLength);

    // Normalize the homogeneous camera space coordinates
    vec3  dQ = yMod * (Q1 - Q0)/(maxSt * projectedCoordDirLength);
    float dk = yMod * (k1 - k0)/(maxSt * projectedCoordDirLength);

    // initialize the variables for ray marching
    vec2 P = projectedCoordStart.xy;
    vec3 Q = Q0;
    float k = k0;
    float rayStartZ = -startPosition.z; // estimated ray start depth value
    float rayEndZ = -startPosition.z;   // estimated ray end depth value
    float prevEstimateZ = -startPosition.z;
    float rayDiffZ = 0.0;
    float dDepth;
    float depth;
    float rayDiffZOld = 0.0;

    // early outs
    if (dot(normal, dir) < 0.0 || dot(-viewDir, normal) < 0.0)
      return vec3(P, 0.0);

    for(int i = 0; i < maxSteps-1; i++)
    {
      depth = -linearDepthFromTexture(depthMapView, P, nearFar); // get linear depth from the depth buffer

      // estimate depth of the marching ray
      rayStartZ = prevEstimateZ;
      dDepth = -rayStartZ - depth;
      rayEndZ = (dQ.z * 0.5 + Q.z)/ ((dk * 0.5 + k));
      rayDiffZ = rayEndZ- rayStartZ;
      prevEstimateZ = rayEndZ;

      if(-rayEndZ > nearFar[1] || -rayEndZ < nearFar[0] || P.y < 0.0  || P.y > 1.0 )
      {
        return vec3(P, 0.);
      }

      // If we detect a hit - return the intersection point, two conditions:
      //  - dDepth > 0.0 - sampled point depth is in front of estimated depth
      //  - if difference between dDepth and rayDiffZOld is not too large
      //  - if difference between dDepth and 0.025/abs(k) is not too large
      //  - if the sampled depth is not behind far plane or in front of near plane

      if((dDepth) < 0.025/abs(k) + abs(rayDiffZ) && dDepth > 0.0 && depth > nearFar[0] && depth < nearFar[1] && abs(P.y - projectedCoordStart.y) > invResolutionHeight)
      {
          return vec3(P, depth);
      }

      // continue with ray marching
      P += dP;
      Q.z += dQ.z;
      k += dk;
      rayDiffZOld = rayDiffZ;
    }
    return vec3(P, 0.0);
  }
  `)}function Mt(e){e.fragment.code.add(Q`
    float normals2FoamIntensity(vec3 n, float waveStrength){
      float normalizationFactor =  max(0.015, waveStrength);
      return max((n.x + n.y)*0.3303545/normalizationFactor + 0.3303545, 0.0);
    }
  `)}function Dt(e){e.fragment.code.add(Q`
    vec3 foamIntensity2FoamColor(float foamIntensityExternal, float foamPixelIntensity, vec3 skyZenitColor, float dayMod){
      return foamIntensityExternal * (0.075 * skyZenitColor * pow(foamPixelIntensity, 4.) +  50.* pow(foamPixelIntensity, 23.0)) * dayMod;
    }
  `)}function Pt(e){e.fragment.uniforms.add("texWaveNormal","sampler2D"),e.fragment.uniforms.add("texWavePerturbation","sampler2D"),e.fragment.uniforms.add("waveParams","vec4"),e.fragment.uniforms.add("waveDirection","vec2"),e.include(Mt),e.fragment.code.add(Q`
      const vec2  FLOW_JUMP = vec2(6.0/25.0, 5.0/24.0);

      vec2 textureDenormalized2D(sampler2D _tex, vec2 _uv) {
        return 2.0 * texture2D(_tex, _uv).rg - 1.0;
      }

      float sampleNoiseTexture(vec2 _uv) {
        return texture2D(texWavePerturbation, _uv).b;
      }

      vec3 textureDenormalized3D(sampler2D _tex, vec2 _uv) {
        return 2.0 * texture2D(_tex, _uv).rgb - 1.0;
      }

      float computeProgress(vec2 uv, float time) {
        return fract(time);
      }

      float computeWeight(vec2 uv, float time) {
        float progress = computeProgress(uv, time);
        return 1.0 - abs(1.0 - 2.0 * progress);
      }

      vec3 computeUVPerturbedWeigth(sampler2D texFlow, vec2 uv, float time, float phaseOffset) {
        float flowStrength = waveParams[2];
        float flowOffset = waveParams[3];

        vec2 flowVector = textureDenormalized2D(texFlow, uv) * flowStrength;

        float progress = computeProgress(uv, time + phaseOffset);
        float weight = computeWeight(uv, time + phaseOffset);

        vec2 result = uv;
        result -= flowVector * (progress + flowOffset);
        result += phaseOffset;
        result += (time - progress) * FLOW_JUMP;

        return vec3(result, weight);
      }

      const float TIME_NOISE_TEXTURE_REPEAT = 0.3737;
      const float TIME_NOISE_STRENGTH = 7.77;

      vec3 getWaveLayer(sampler2D _texNormal, sampler2D _dudv, vec2 _uv, vec2 _waveDir, float time) {
        float waveStrength = waveParams[0];

        // overall directional shift in uv's for directional wave movement for
        // now we do a hard coded scale for wave speed such that a unit length
        // direction is not too fast.
        vec2 waveMovement = time * -_waveDir;

        float timeNoise = sampleNoiseTexture(_uv * TIME_NOISE_TEXTURE_REPEAT) * TIME_NOISE_STRENGTH;

        // compute two perturbed uvs and blend weights
        // then sample the wave normals at the two positions and blend
        vec3 uv_A = computeUVPerturbedWeigth(_dudv, _uv + waveMovement, time + timeNoise, 0.0);
        vec3 uv_B = computeUVPerturbedWeigth(_dudv, _uv + waveMovement, time + timeNoise, 0.5);

        vec3 normal_A = textureDenormalized3D(_texNormal, uv_A.xy) * uv_A.z;
        vec3 normal_B = textureDenormalized3D(_texNormal, uv_B.xy) * uv_B.z;

        // logic to flatten the wave pattern
        // scale xy components of the normal, then adjust z (up) component
        vec3 mixNormal = normalize(normal_A + normal_B);
        mixNormal.xy *= waveStrength;
        mixNormal.z = sqrt(1.0 - dot(mixNormal.xy, mixNormal.xy));

        return mixNormal;
      }

      vec4 getSurfaceNormalAndFoam(vec2 _uv, float _time) {
        float waveTextureRepeat = waveParams[1];
        vec3 normal = getWaveLayer(texWaveNormal, texWavePerturbation, _uv * waveTextureRepeat, waveDirection, _time);
        float foam  = normals2FoamIntensity(normal, waveParams[0]);
        return vec4(normal, foam);
      }
    `)}function At(e,t){1===t.viewingMode?e.vertex.code.add(Q`
      vec3 getLocalUp(in vec3 pos, in vec3 origin) {
          return normalize(pos + origin);
      }
    `):e.vertex.code.add(Q`
      vec3 getLocalUp(in vec3 pos, in vec3 origin) {
          return vec3(0.0, 0.0, 1.0); // WARNING: up-axis dependent code
      }
    `),1===t.viewingMode?e.vertex.code.add(Q`
        mat3 getTBNMatrix(in vec3 n) {
            vec3 t = normalize(cross(vec3(0.0, 0.0, 1.0), n));
            vec3 b = normalize(cross(n, t));
            return mat3(t, b, n);
        }
    `):e.vertex.code.add(Q`
        mat3 getTBNMatrix(in vec3 n) {
            vec3 t = vec3(1.0, 0.0, 0.0);
            vec3 b = normalize(cross(n, t));
            return mat3(t, b, n);
        }
    `)}function Ut(e){e.fragment.code.add(Q`
    const float GAMMA = 2.2;
    const float INV_GAMMA = 0.4545454545;

    vec4 delinearizeGamma(vec4 color) {
      return vec4(pow(color.rgb, vec3(INV_GAMMA)), color.w);
    }

    vec3 linearizeGamma(vec3 color) {
      return pow(color, vec3(GAMMA));
    }
  `)}function Lt(e,t){e.include(Qe,t),e.include(Ut),e.include(Dt),t.ssrEnabled&&e.include(It,t),e.fragment.code.add(Q`
    const vec3 fresnelSky =  vec3(0.02, 1.0, 15.0); // f0, f0max, exp
    const vec2 fresnelMaterial =  vec2(0.02, 0.1); // f0, f0max for specular term
    const float roughness = 0.015;
    const float foamIntensityExternal = 1.7;
    const float ssrIntensity = 0.65;
    const float ssrHeightFadeStart = 300000.0;
    const float ssrHeightFadeEnd = 500000.0;
    const float waterDiffusion = 0.775;
    const float waterSeeColorMod = 0.8;
    const float correctionViewingPowerFactor = 0.4;

    const vec3 skyZenitColor = vec3(0.52, 0.68, 0.90);
    const vec3 skyColor = vec3(0.67, 0.79, 0.9);

    PBRShadingWater shadingInfo;

    /*
    *   This function is an approximation for the sky gradient reflected
    *   the water surface and describes a combination of two fresnel terms.
    *   @parameter: cosTheta = is the result of max(dot(n,v), 0.0)
    *   @parameter: horizon = the dominant color of the sky horizon
    *   @parameter: cosTheta = the dominant color of the sky zenit
    */
    vec3 getSkyGradientColor(in float cosTheta, in vec3 horizon, in vec3 zenit) {
      float exponent = pow((1.0 - cosTheta), fresnelSky[2]);
      return mix(zenit, horizon, exponent);
    }

    /*
    *   This function determines the water color per pixel.
    *   @parameter: n = normal facing away from the surface
    *   @parameter: v = view direction facing away from the surface.
    *   @parameter: l = light direction facing away from the surface
    *   @parameter: lightIntensity = light intensity, currently between 0...PI
    *   @parameter: localUp = a normal for the general direction of the surface
    *   @parameter: shadow = the amount of shadow at this pixel (0 = no shadow)
    */
    vec3 getSeaColor(in vec3 n, in vec3 v, in vec3 l, vec3 color, in vec3 lightIntensity, in vec3 localUp, in float shadow, float foamIntensity, vec3 positionView) {

      float reflectionHit = 0.;
      vec3 seaWaterColor = linearizeGamma(color);
      // using half vector to determine the specular light
      vec3 h = normalize(l + v);
      shadingInfo.NdotL = clamp(dot(n, l), 0.0, 1.0);
      shadingInfo.NdotV = clamp(dot(n, v), 0.001, 1.0);
      shadingInfo.VdotN = clamp(dot(v, n), 0.001, 1.0);
      shadingInfo.NdotH = clamp(dot(n, h), 0.0, 1.0);
      shadingInfo.VdotH = clamp(dot(v, h), 0.0, 1.0);
      shadingInfo.LdotH = clamp(dot(l, h), 0.0, 1.0);

      // angle between vertex normal and view direction
      float upDotV = max(dot(localUp,v), 0.0);
      // reflected sky color: the reflected sky color consists of two main colors, the
      // reflected color at the horizon and the reflected color of the zenit.
      // the reflected sky color is then an approximation based on the fresnel term.
      vec3 skyHorizon = linearizeGamma(skyColor);
      vec3 skyZenit = linearizeGamma(skyZenitColor);
      vec3 skyColor = getSkyGradientColor(upDotV, skyHorizon, skyZenit );

      // we use the upDotL to smoothen out the
      // reflected color of the water
      float upDotL = max(dot(localUp,l),0.0);

      // The approximated sky color is adjusted according to the sun position.
      // This is done as approximation for e.g. night views.
      float daytimeMod = 0.1 + upDotL * 0.9;
      skyColor *= daytimeMod;

      // If a water surface is in shadow we just use a slight darkening of the
      // water surface expressed with this shadowModifier.
      float shadowModifier = clamp(shadow, 0.8, 1.0);

      // The reflected sky color consists of the fresnel reflection multiplied with the approximated sky color.
      // The shadow is influencing the frensel term to keep the shadow impression for really near views. As long
      // as reflection are absent there is a need to have a slight shadow for depth perception.
      vec3 fresnelModifier = fresnelReflection(shadingInfo.VdotN, vec3(fresnelSky[0]), fresnelSky[1]);
      vec3 reflSky = fresnelModifier * skyColor * shadowModifier;

      // The reflected sea color is the input water color combined with the reflected sky color.
      // The reflected sky color is modified by the incoming light.
      vec3 reflSea = seaWaterColor * mix(skyColor, upDotL * lightIntensity * LIGHT_NORMALIZATION, 2.0 / 3.0) * shadowModifier;

      vec3 specular = vec3(0.0);
      // This prevents the specular light to be rendered when:
      // - sun is behind a polygon (e.g. sundown for elevated polygons where nDotL might be still ok)
      // - viewer is under water (for this localUp is better than n)
      if(upDotV > 0.0 && upDotL > 0.0) {
        // calculate the cook torrance BRDF but with simplified occlusion
        vec3 specularSun = brdfSpecularWater(shadingInfo, roughness, vec3(fresnelMaterial[0]), fresnelMaterial[1]);

        // Normalize light intensity to be between 0...1. Shadow cancels out specular light here
        vec3 incidentLight = lightIntensity * LIGHT_NORMALIZATION * shadow;

        specular = shadingInfo.NdotL * incidentLight * specularSun;
      }

      vec3 foam = vec3(0.0);
      if(upDotV > 0.0) {
        foam = foamIntensity2FoamColor(foamIntensityExternal, foamIntensity, skyZenitColor, daytimeMod);
      }
      `),t.ssrEnabled?e.fragment.code.add(Q`
      // Convert the world position to view position
      vec4 viewPosition = vec4(positionView.xyz, 1.0);
      vec3 viewDir = normalize(viewPosition.xyz);
      vec4 viewNormalVectorCoordinate = ssrViewMat *vec4(n, 0.0);
      vec3 viewNormal = normalize(viewNormalVectorCoordinate.xyz);
      vec4 viewUp = ssrViewMat *vec4(localUp, 0.0);

      // at steeper viewing angles we use more of a vertex normal (in this case up) then the wave normal
      // this removes some artifacts of normal mapping
      float correctionViewingFactor = pow(max(dot(-viewDir, viewUp.xyz), 0.0), correctionViewingPowerFactor);
      vec3 viewNormalCorrected = mix(viewUp.xyz, viewNormal, correctionViewingFactor);

      vec3 reflected = normalize(reflect(viewDir, viewNormalCorrected));

      // perform screen space reflection to detect hit
      vec3 hitCoordinate = screenSpaceIntersection( normalize(reflected), viewPosition.xyz, viewDir, viewUp.xyz);
      vec3 reflectedColor = vec3(0.0);

      // if there is a hit with ssr find reflected color from the reprojeted frame
      if (hitCoordinate.z > 0.0)
      {
        vec2 reprojectedCoordinate = reprojectionCoordinate(hitCoordinate);

        // fade out if there if the hit is near end of Y axis
        vec2 dCoords = smoothstep(0.3, 0.6, abs(vec2(0.5, 0.5) - hitCoordinate.xy));
        float heightMod = smoothstep(ssrHeightFadeEnd, ssrHeightFadeStart, -positionView.z);
        reflectionHit = waterDiffusion * clamp(1.0 - (1.3*dCoords.y), 0.0, 1.0) * heightMod;

        reflectedColor = linearizeGamma(texture2D(lastFrameColorMap, reprojectedCoordinate).xyz)* reflectionHit * fresnelModifier.y * ssrIntensity;
      }
      float seeColorMod =  mix(waterSeeColorMod, waterSeeColorMod*0.5, reflectionHit);
      // combining reflected sky, reflected sea, specular highlight and SSR reflections.
      return tonemapACES((1. - reflectionHit) * reflSky + reflectedColor + reflSea * seeColorMod + specular + foam);
    }
  `):e.fragment.code.add(Q`
      // combining reflected sky, reflected sea, specular highlight and SSR reflections.
      return tonemapACES(reflSky + reflSea * waterSeeColorMod + specular + foam);
    }
  `)}!function(e){e.bindUniforms=function(e,t,r){e.setUniform1i("lastFrameColorMap",r.lastFrameColorTextureID),t.bindTexture(r.lastFrameColorTexture,r.lastFrameColorTextureID),e.setUniformMatrix4fv("reprojectionMat",r.reprojectionMat),e.setUniformMatrix4fv("rpProjectionMat",r.camera.projectionMatrix)}}(Ot||(Ot={})),function(e){e.bindUniforms=function(e,t,r){r.ssrEnabled&&(e.setUniform1i("depthMapView",r.linearDepthTextureID),t.bindTexture(r.linearDepthTexture,r.linearDepthTextureID),e.setUniform2fv("nearFar",r.camera.nearFar),e.setUniformMatrix4fv("ssrViewMat",r.camera.viewMatrix),e.setUniform1f("invResolutionHeight",1/r.camera.height),Ot.bindUniforms(e,t,r))}}(It||(It={})),function(e){e.bindUniforms=function(e,t){e.setUniform1i("texWaveNormal",0),e.setUniform1i("texWavePerturbation",1),e.setUniform4f("waveParams",t.waveStrength,t.waveTextureRepeat,t.flowStrength,t.flowOffset),e.setUniform2f("waveDirection",t.waveDirection[0]*t.waveVelocity,t.waveDirection[1]*t.waveVelocity)}}(Pt||(Pt={}));var Ht=Object.freeze({__proto__:null,build:function(e){const t=new Y;return t.include($,{linearDepth:!1}),t.attributes.add("position","vec3"),t.attributes.add("uv0","vec2"),t.vertex.uniforms.add("proj","mat4").add("view","mat4").add("localOrigin","vec3"),t.vertex.uniforms.add("waterColor","vec4"),0!==e.output&&7!==e.output||(t.include(At,e),t.include(Ye,e),t.varyings.add("vuv","vec2"),t.varyings.add("vpos","vec3"),t.varyings.add("vnormal","vec3"),t.varyings.add("vtbnMatrix","mat3"),t.vertex.code.add(Q`
      void main(void) {
        if (waterColor.a < ${Q.float(X)}) {
          // Discard this vertex
          gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
          return;
        }

        vuv = uv0;
        vpos = position;

        vnormal = getLocalUp(vpos, localOrigin);
        vtbnMatrix = getTBNMatrix(vnormal);

        gl_Position = transformPosition(proj, view, vpos);
        ${0===e.output?"forwardLinearDepth();":""}
      }
    `)),7===e.output&&(t.include(J,e),t.fragment.uniforms.add("waterColor","vec4"),t.fragment.code.add(Q`
        void main() {
          discardBySlice(vpos);

          gl_FragColor = vec4(waterColor.a);
        }
      `)),0===e.output&&(t.include(Pt,e),t.include(J,e),e.receiveShadows&&t.include($e,e),t.include(Lt,e),t.fragment.uniforms.add("waterColor","vec4").add("lightingMainDirection","vec3").add("lightingMainIntensity","vec3").add("camPos","vec3").add("timeElapsed","float").add("view","mat4"),t.fragment.include(K),t.fragment.code.add(Q`
      void main() {
        discardBySlice(vpos);
        vec3 localUp = vnormal;
        // the created normal is in tangent space
        vec4 tangentNormalFoam = getSurfaceNormalAndFoam(vuv, timeElapsed);

        // we rotate the normal according to the tangent-bitangent-normal-Matrix
        vec3 n = normalize(vtbnMatrix * tangentNormalFoam.xyz);
        vec3 v = -normalize(vpos - camPos);
        vec3 l = normalize(-lightingMainDirection);
        float shadow = ${e.receiveShadows?Q`1.0 - readShadowMap(vpos, linearDepth)`:"1.0"};
        vec4 vPosView = view*vec4(vpos, 1.0);
        vec4 final = vec4(getSeaColor(n, v, l, waterColor.rgb, lightingMainIntensity, localUp, shadow, tangentNormalFoam.w, vPosView.xyz), waterColor.w);

        // gamma correction
        gl_FragColor = delinearizeGamma(final);
        gl_FragColor = highlightSlice(gl_FragColor, vpos);
        ${e.OITEnabled?"gl_FragColor = premultiplyAlpha(gl_FragColor);":""}
      }
    `)),2===e.output&&(t.include(At,e),t.include(Pt,e),t.include(J,e),t.varyings.add("vpos","vec3"),t.varyings.add("vuv","vec2"),t.vertex.code.add(Q`
        void main(void) {
          if (waterColor.a < ${Q.float(X)}) {
            // Discard this vertex
            gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
            return;
          }

          vuv = uv0;
          vpos = position;

          gl_Position = transformPosition(proj, view, vpos);
        }
    `),t.fragment.uniforms.add("timeElapsed","float"),t.fragment.code.add(Q`
        void main() {
          discardBySlice(vpos);

          // the created normal is in tangent space and foam
          vec4 tangentNormalFoam = getSurfaceNormalAndFoam(vuv, timeElapsed);
          tangentNormalFoam.xyz = normalize(tangentNormalFoam.xyz);

          gl_FragColor = vec4((tangentNormalFoam.xyz + vec3(1.0)) * 0.5, tangentNormalFoam.w);
        }
    `)),5===e.output&&(t.varyings.add("vpos","vec3"),t.vertex.code.add(Q`
        void main(void) {
          if (waterColor.a < ${Q.float(X)}) {
            // Discard this vertex
            gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
            return;
          }

          vpos = position;
          gl_Position = transformPosition(proj, view, vpos);
        }
    `),t.fragment.uniforms.add("waterColor","vec4"),t.fragment.code.add(Q`
        void main() {
          gl_FragColor = waterColor;
        }
    `)),4===e.output&&(t.include(ee),t.varyings.add("vpos","vec3"),t.vertex.code.add(Q`
      void main(void) {
        if (waterColor.a < ${Q.float(X)}) {
          // Discard this vertex
          gl_Position = vec4(1e38, 1e38, 1e38, 1.0);
          return;
        }

        vpos = position;
        gl_Position = transformPosition(proj, view, vpos);
      }
    `),t.include(J,e),t.fragment.code.add(Q`
      void main() {
        discardBySlice(vpos);
        outputHighlight();
      }
    `)),t}});class Nt extends ie{constructor(e,t){super(e,t),this.waterTextureRepository=e.waterTextureRepository}initializeProgram(e){const t=Nt.shader.get(),r=this.configuration,i=t.build({OITEnabled:0===r.transparencyPassType,output:r.output,viewingMode:e.viewingMode,slicePlaneEnabled:r.slicePlaneEnabled,sliceHighlightDisabled:!1,sliceEnabledForVertexPrograms:!1,receiveShadows:r.receiveShadows,pbrMode:3,useCustomDTRExponentForWater:!0,ssrEnabled:r.useSSR,highStepCount:!0});return new fe(e.rctx,i.generateSource("vertex"),i.generateSource("fragment"),se)}ensureResource(e){return this.waterTextureRepository.ready||this.waterTextureRepository.updating||this.waterTextureRepository.loadTextures(e),this.waterTextureRepository.ready?2:1}bindPass(e,t,r){oe.bindProjectionMatrix(this.program,r.camera.projectionMatrix),0===this.configuration.output&&(r.lighting.setUniforms(this.program,!1),It.bindUniforms(this.program,e,r)),0!==this.configuration.output&&2!==this.configuration.output||(Pt.bindUniforms(this.program,t),this.waterTextureRepository.bindRepo(e)),this.program.setUniform4fv("waterColor",t.color),4===this.configuration.output&&ee.bindOutputHighlight(e,this.program,r)}bindDraw(e){oe.bindView(this.program,e),0!==this.configuration.output&&7!==this.configuration.output||oe.bindCamPosition(this.program,e.origin,e.camera.viewInverseTransposeMatrix),0===this.configuration.output&&$e.bindUniforms(this.program,e,qe.SHADOW_MAP),0!==this.configuration.output&&7!==this.configuration.output&&4!==this.configuration.output||J.bindUniformsWithOrigin(this.program,this.configuration,e)}setPipelineState(e){const t=this.configuration,r=3===e,i=2===e;return ve({blending:2!==t.output&&4!==t.output&&t.transparent?r?ae:ne(e):null,depthTest:{func:de(e)},depthWrite:r?t.writeDepth&&ye:le(e),colorWrite:we,polygonOffset:r||i?null:he(t.enableOffset)})}initializePipeline(){return this.setPipelineState(this.configuration.transparencyPassType)}}Nt.shader=new ce(Ht,(()=>Promise.resolve().then((function(){return Ht}))));class jt extends re{constructor(){super(...arguments),this.output=0,this.receiveShadows=!1,this.slicePlaneEnabled=!1,this.transparent=!1,this.enableOffset=!0,this.writeDepth=!1,this.useSSR=!1,this.isDraped=!1,this.transparencyPassType=3}}e([te({count:8})],jt.prototype,"output",void 0),e([te()],jt.prototype,"receiveShadows",void 0),e([te()],jt.prototype,"slicePlaneEnabled",void 0),e([te()],jt.prototype,"transparent",void 0),e([te()],jt.prototype,"enableOffset",void 0),e([te()],jt.prototype,"writeDepth",void 0),e([te()],jt.prototype,"useSSR",void 0),e([te()],jt.prototype,"isDraped",void 0),e([te({count:4})],jt.prototype,"transparencyPassType",void 0);class zt extends ue{constructor(e){super(e),this.updateParameters()}updateParameters(e){this.technique=this.techniqueRep.acquireAndReleaseExisting(Nt,this.material.getTechniqueConfig(this.output,e),this.technique)}beginSlot(e){if(2===this.output)return 22===e;if(5===this.output)return null==e;if(4===this.output)return 3===e;let t=3;return this.material.params.transparent&&(t=this.material.params.writeDepth?5:8),e===t}setElapsedTimeUniform(e){const t=.001*this.material.animation.time;e.setUniform1f("timeElapsed",t*this.material.params.animationSpeed)}_updateShadowState(e){e.shadowMappingEnabled!==this.material.params.receiveShadows&&this.material.setParameterValues({receiveShadows:e.shadowMappingEnabled})}_updateSSRState(e){e.ssrEnabled!==this.material.params.ssrEnabled&&this.material.setParameterValues({ssrEnabled:e.ssrEnabled})}ensureResources(e){return this.technique.ensureResource(e)}ensureParameters(e){0===this.output&&(this._updateShadowState(e),this._updateSSRState(e)),this.updateParameters(e)}bind(e,t){e.bindProgram(this.technique.program),this.technique.bindPass(e,this.material.params,t),2!==this.output&&0!==this.output||this.setElapsedTimeUniform(this.technique.program)}}class Ft{constructor(e,t,r,i,s,o){this.from=e,this.to=t,this.isVisible=r,this.hasHighlights=i,this.hasOccludees=s,this.transformation=o,null!=o&&(this.transformationNormal=B(o),P(this.transformationNormal,this.transformationNormal),A(this.transformationNormal,this.transformationNormal))}}function Gt(e,t){const r=e=>({first:e.from,count:e.to-e.from});if(0===e.length)return void e.push(r(t));const i=e[e.length-1];if(o=t,(s=i).first+s.count>=o.from){const e=t.from-i.first+t.to-t.from;i.count=e}else e.push(r(t));var s,o}function Vt(e){const t=e.capabilities.disjointTimerQuery;return r(t)?null:t.timestampBits()>0?new Wt(t):kt?null:new Bt(t)}class Wt{constructor(e){this.timer=e,this.start=e.createQuery(),e.createTimestamp(this.start)}stop(e,t=50){this.end=this.timer.createQuery(),this.timer.createTimestamp(this.end),this.checkQueryResult(e,t)}checkQueryResult(e,t){if(!this.timer.resultAvailable(this.end))return void setTimeout((()=>this.checkQueryResult(e,t)),t);if(this.timer.disjoint())return void e(null);const r=this.timer.getResult(this.start),i=this.timer.getResult(this.end);e((i-r)/1e6)}}class Bt{constructor(e){this.timer=e,this.query=e.createQuery(),kt=!0,this.timer.beginTimeElapsed(this.query)}stop(e,t=50){this.timer.endTimeElapsed(),kt=!1,this.checkQueryResult(e,t)}checkQueryResult(e,t){const r=this.timer.resultAvailable(this.query),i=this.timer.disjoint();if(!r||i)i?e(null):setTimeout((()=>this.checkQueryResult(e,t)),t);else{const t=this.timer.getResult(this.query);e(t/1e6)}}}let kt=!1;const qt=["prepare","shadowmap","lineardepth","normals","ssao","opaque","opaque edges","transparent","transparent edges","hudvisibility","transparent terrain","atmosphere","laserline","occluded","antialiasing","highlights","hudOccluded","hudNotoccluded"];class Zt{constructor(){this.triangles=0,this.drawCalls=0}reset(){this.triangles=0,this.drawCalls=0}}function Qt(e,t,r){s(r)&&(r.drawCalls+=e,r.triangles+=t)}class Yt extends u{constructor(){super("total"),this.total=0,this.frameCount=0}}class $t{constructor(){this._startTime=0,this._lastSample=0,this._enableGPUTimer=0,this.totalTime=new Yt,this.gpuTime=new u("gpu",9),this.renderPassTimings=qt.map((e=>new u(e))),this.stats=new Zt}enableGPUTimer(){return++this._enableGPUTimer,{remove:d((()=>--this._enableGPUTimer))}}prerender(e){this._startTime=this._lastSample=performance.now(),this._enableGPUTimer&&(this._gpuTimer=Vt(e))}advance(e){const t=performance.now();this.renderPassTimings[e].record(t-this._lastSample),this._lastSample=t}postrender(){s(this._gpuTimer)&&(this._gpuTimer.stop((e=>s(e)&&this.gpuTime.record(e)),16),this._gpuTimer=null);const e=performance.now()-this._startTime;this.totalTime.record(e),this.totalTime.total+=e,this.totalTime.frameCount++}}class Xt{constructor(e,t,r){this.type="MergedRenderer",this._dataByOrigin=new Map,this._hasHighlights=!1,this._hasOccludees=!1,this._rctx=e,this._material=r,this._materialRep=t,this._glMaterials=Ge(this._material,this._materialRep),this._bufferWriter=r.createBufferWriter()}dispose(){Ve(this._material,this._materialRep),this._dataByOrigin&&(this._dataByOrigin.forEach((e=>{e.vao.dispose(!0),e.vao=null})),this._dataByOrigin.clear(),this._dataByOrigin=null),this._glMaterials&&(this._glMaterials.forEach((e=>{e&&e.dispose()})),this._glMaterials.clear(),this._glMaterials=null)}get isEmpty(){return 0===this._dataByOrigin.size}get hasHighlights(){return this._hasHighlights}get hasOccludees(){return this._hasOccludees}get hasWater(){return!this.isEmpty&&q(this._glMaterials,(e=>e instanceof zt))}get rendersOccluded(){return!this.isEmpty&&1!==this._material.renderOccluded}modify(e){this.updateGeometries(e.toUpdate),this.addAndRemoveGeometries(e.toAdd,e.toRemove),this.updateRenderCommands()}addAndRemoveGeometries(e,t){const r=this._bufferWriter,i=r.vertexBufferLayout,s=i.stride/4,o=this._dataByOrigin,a=function(e,t,r,i){const s=new Map,o=t.vertexBufferLayout.stride/4,a=(r,i)=>{const a=r.origin,n=e.get(a.id);let d=s.get(a.id);null==d&&(d={optimalCount:null==n?0:n.optimalCount,sparseCount:null==n?0:n.buffer.size,toAdd:[],toRemove:[],origin:a.vec3},s.set(a.id,d));const l=t.elementCount(r.data)*o;i?(d.optimalCount+=l,d.sparseCount+=l,d.toAdd.push(r)):(d.optimalCount-=l,d.toRemove.push(r))};for(const e of r)a(e,!0);for(const e of i)a(e,!1);return s}(o,r,e,t);a.forEach(((e,t)=>{a.delete(t);const r=e.optimalCount,n=e.sparseCount;let d=o.get(t);if(null==d)Ce(r>0),d=this.createData(i,r,e.origin),o.set(t,d);else if(0===r)return d.vao.dispose(!0),d.vao=null,void o.delete(t);const l=r<e.sparseCount/2,h=l?r:n,c=Jt,u=d.buffer.size,m=d.buffer.array,p=d.buffer.resize(h,!1);l||p?this.removeAndRebuild(d,e.toRemove,s,m,c):e.toRemove.length>0?(this.removeByErasing(d,e.toRemove,s,c),e.toAdd.length>0&&(c.end=u)):(c.begin=u,c.end=u);const g=Kt;Me(g,-e.origin[0],-e.origin[1],-e.origin[2]),this.append(d,e.toAdd,s,g,c);const f=d.vao.vertexBuffers.geometry;if(f.byteSize!==d.buffer.array.buffer.byteLength)f.setData(d.buffer.array);else{const{begin:e,end:t}=c;if(e<t){const r=d.buffer.array,i=4,s=e*i,o=t*i;f.setSubData(r,s,s,o)}}d.drawCommandsDirty=!0}))}updateGeometries(e){const t=this._bufferWriter,r=t.vertexBufferLayout.stride/4;for(const i of e){const e=i.updateType,s=i.renderGeometry,o=this._dataByOrigin.get(s.origin.id),a=o&&o.instances.get(s.uniqueName);if(!a)return;if(1&e&&(a.isVisible=s.instanceParameters.visible),9&e){const e=s.instanceParameters.visible;a.hasHighlights=!!s.instanceParameters.highlights&&e}if(16&e&&(a.hasOccludees=!!s.instanceParameters.occludees),6&e){const e=o.buffer.array,i=o.vao;We(s,er,tr),t.write({transformation:er,invTranspTransformation:tr},s.data,t.vertexBufferLayout.createView(e.buffer),a.from),Ce(a.from+t.elementCount(s.data)===a.to,"material VBO layout has changed"),i.vertexBuffers.geometry.setSubData(e,a.from*r*4,a.from*r*4,a.to*r*4)}o.drawCommandsDirty=!0}}updateRenderCommands(){this._hasHighlights=!1,this._hasOccludees=!1,this._dataByOrigin.forEach((e=>{e.hasHiddenInstances=!1,e.hasHighlights=!1,e.hasOccludees=!1,q(e.instances,(t=>(t.isVisible?(t.hasHighlights&&(this._hasHighlights=!0,e.hasHighlights=!0),t.hasOccludees&&(this._hasOccludees=!0,e.hasOccludees=!0)):e.hasHiddenInstances=!0,e.hasHiddenInstances&&e.hasHighlights&&e.hasOccludees)))}));this._dataByOrigin.forEach((e=>{e.drawCommandsDirty&&((e=>{if(e.drawCommandsDefault=null,e.drawCommandsHighlight=null,e.drawCommandsOccludees=null,e.stats={default:0,highlight:0,occludees:0},0===e.instances.size)return;if(!(e.hasOccludees||e.hasHighlights||e.hasHiddenInstances)){const t=4*e.buffer.size/je(e.vao.layout.geometry);return e.drawCommandsDefault=[{first:0,count:t}],void(e.stats={default:t,highlight:0,occludees:0})}const t=[...e.instances.values()].sort(((e,t)=>e.from===t.from?e.to>t.to?1:e.to<t.to?-1:0:e.from>t.from?1:e.from<t.from?-1:0));e.drawCommandsDefault=[],e.drawCommandsHighlight=[],e.drawCommandsOccludees=[];for(const r of t)r.isVisible&&(r.hasOccludees?Gt(e.drawCommandsOccludees,r):Gt(e.drawCommandsDefault,r),r.hasHighlights&&Gt(e.drawCommandsHighlight,r));const r=e=>{let t=0;for(const r of e)t+=r.count;return t/3};e.stats={default:r(e.drawCommandsDefault),highlight:r(e.drawCommandsHighlight),occludees:r(e.drawCommandsOccludees)}})(e),e.drawCommandsDirty=!1)}))}updateLogic(e){return this._material.update(e)}render(e,t,i,o){const a=this._rctx,n=this._glMaterials.get(t.pass),d=5===t.pass;let l=e;if(3===t.pass&&null===l&&(l=22),!n||2!==n.ensureResources(a)||null!=l&&!n.beginSlot(l)||d&&!this._hasHighlights)return!1;n.ensureParameters(i);const h=n.getTechnique(),c=n.getPipelineState(l,!1);a.setPipelineState(c),n.bind(a,i);let u=!1;return this._dataByOrigin.forEach((e=>{if(r(e.drawCommandsDefault)&&r(e.drawCommandsHighlight)&&r(e.drawCommandsOccludees))return;if(d&&!e.hasHighlights)return;i.origin=e.origin,h.bindDraw(i),h.ensureAttributeLocations(e.vao),a.bindVAO(e.vao);const t=h.primitiveType;let m=d?e.drawCommandsHighlight:e.drawCommandsDefault;const p=d?e.stats.highlight:e.stats.default;if(s(m)&&(this.renderCommands(a,t,m),Qt(m.length,p,o),u=!0),!d&&(m=e.drawCommandsOccludees,s(m))){const r=n.getPipelineState(l,!0);a.setPipelineState(r),this.renderCommands(a,t,m),a.setPipelineState(c),Qt(m.length,e.stats.occludees,o),u=!0}})),u}renderCommands(e,t,r){for(let i=0;i<r.length;i++)e.drawArrays(t,r[i].first,r[i].count)}createData(e,t,r){return{instances:new Map,vao:new He(this._rctx,this._material.vertexAttributeLocations,{geometry:be(e)},{geometry:Ne.createVertex(this._rctx,35044)}),buffer:new Ct(t),optimalCount:0,origin:r,hasHiddenInstances:!1,hasHighlights:!1,hasOccludees:!1,drawCommandsDirty:!1,drawCommandsDefault:null,drawCommandsOccludees:null,drawCommandsHighlight:null,stats:{default:0,highlight:0,occludees:0}}}removeAndRebuild(e,t,r,i,s){for(const i of t){const t=i.uniqueName,s=e.instances.get(t);e.optimalCount-=(s.to-s.from)*r,e.instances.delete(t)}let o=0;const a=e.buffer.array;s.begin=0,s.end=0;let n=-1,d=-1,l=0;e.instances.forEach((e=>{const t=e.from*r,s=e.to*r,h=s-t;n!==d&&d!==t?(a.set(i.subarray(n,d),l),l+=d-n,n=t):-1===n&&(n=t),d=s,e.from=o/r,o+=h,e.to=o/r})),n!==d&&a.set(i.subarray(n,d),l),s.end=o}removeByErasing(e,t,r,i){i.begin=1/0,i.end=-1/0;let s=-1,o=-1;for(const a of t){const t=a.uniqueName,n=e.instances.get(t),d=n.from*r,l=n.to*r;s!==o&&o!==d?(e.buffer.erase(s,o),s=d):-1===s&&(s=d),o=l,e.instances.delete(t),e.optimalCount-=l-d,d<i.begin&&(i.begin=d),l>i.end&&(i.end=l)}s!==o&&e.buffer.erase(s,o)}append(e,t,r,i,o){const a=this._bufferWriter;for(const n of t){const t=s(n.transformation)?U(er,i,n.transformation):i;P(tr,t),A(tr,tr);const d=o.end;a.write({transformation:t,invTranspTransformation:tr},n.data,a.vertexBufferLayout.createView(e.buffer.array.buffer),o.end/r);const l=a.elementCount(n.data)*r,h=d+l;Ce(null==e.instances.get(n.uniqueName));const c=n.instanceParameters.visible,u=!!n.instanceParameters.highlights&&c,m=!!n.instanceParameters.occludees,p=new Ft(d/r,h/r,c,u,m);e.instances.set(n.uniqueName,p),e.optimalCount+=l,o.end+=l}}get test(){return{material:this._material,glMaterials:this._glMaterials}}}const Jt={begin:0,end:0},Kt=k(),er=k(),tr=k();class rr{}let ir=class extends h{constructor(){super(...arguments),this._pendingAddsRemoves=new Map,this._adds=new m,this._removes=new m,this._updates=new m({allocator:e=>e||new rr,deallocator:e=>(e.renderGeometry=null,e)}),this._materialRenderers=new Map,this._sortedMaterialRenderers=new m,this._hasHighlights=!1,this._hasWater=!1}dispose(){this._adds.prune(),this._removes.prune(),this._updates.prune(),this._materialRenderers&&(this._materialRenderers.forEach((e=>e.dispose())),this._materialRenderers.clear(),this._sortedMaterialRenderers.clear())}get updating(){return this._pendingAddsRemoves.size>0||this._updates.length>0}get hasHighlights(){return this._hasHighlights}get hasWater(){return this._hasWater}get rendersOccluded(){return q(this._materialRenderers,(e=>e.rendersOccluded))}stopAnimationsAtTime(e){this._sortedMaterialRenderers.forAll((t=>o(t.material.animation,(t=>t.stopAtTime(e)))))}get isEmpty(){return!this.updating&&0===this._materialRenderers.size}commitChanges(){let e=!1;if(!this.updating)return!1;this.updateAddsRemoves();const t=St({numToAdd:this._adds.length,toAdd:this._adds.data,numToRemove:this._removes.length,toRemove:this._removes.data,numToUpdate:this._updates.length,toUpdate:this._updates.data});let r=!1,i=!1;return t.forEach(((t,s)=>{let o=this._materialRenderers.get(s);if(!o&&t.toAdd.length>0&&(o=new Xt(this.rctx,this.materialRepository,s),this._materialRenderers.set(s,o),e=!0,r=!0,i=!0),!o)return;const a=r||o.hasHighlights,n=i||o.hasWater;o.modify(t),r=r||a!==o.hasHighlights,i=i||n!==o.hasWater,o.isEmpty&&(this._materialRenderers.delete(s),o.dispose(),e=!0)})),this._adds.clear(),this._removes.clear(),this._updates.clear(),this._pendingAddsRemoves.clear(),e&&this.updateSortedMaterialRenderers(),r&&(this._hasHighlights=q(this._materialRenderers,(e=>e.hasHighlights))),i&&(this._hasWater=q(this._materialRenderers,(e=>e.hasWater))),this.notifyChange("updating"),!0}add(e){if(0===e.length)return;const t=0===this._pendingAddsRemoves.size;for(const t of e)this._pendingAddsRemoves.set(t,0);t&&this.notifyChange("updating")}remove(e){const t=0===this._pendingAddsRemoves.size;for(const t of e){const e=this._pendingAddsRemoves.get(t);0===e?this._pendingAddsRemoves.set(t,2):2!==e&&this._pendingAddsRemoves.set(t,1)}t&&this._pendingAddsRemoves.size>0&&this.notifyChange("updating")}modify(e,t){const r=0===this._updates.length;for(const r of e){const e=this._updates.pushNew();e.renderGeometry=r,e.updateType=t}r&&this._updates.length>0&&this.notifyChange("updating")}updateLogic(e){let t=!1;return this._sortedMaterialRenderers.forAll((({materialRenderer:r})=>{r.updateLogic&&r.updateLogic(e)&&(t=!0)})),t}draw(e,t){for(let r=0;r<this._sortedMaterialRenderers.length;r++){const i=this._sortedMaterialRenderers.data[r];me(i.material,e)&&i.materialRenderer.render(null,e,t,null)}}updateSortedMaterialRenderers(){this._sortedMaterialRenderers.clear();let e=0;this._materialRenderers.forEach(((t,r)=>{r.insertOrder=e++,this._sortedMaterialRenderers.push({material:r,materialRenderer:t})})),this._sortedMaterialRenderers.sort(((e,t)=>{const r=t.material.renderPriority-e.material.renderPriority;return 0!==r?r:e.material.insertOrder-t.material.insertOrder}))}updateAddsRemoves(){this._adds.clear(),this._removes.clear(),this._pendingAddsRemoves.forEach(((e,t)=>{switch(e){case 0:this._adds.push(t);break;case 1:this._removes.push(t)}}));let e=0;for(;e<this._updates.length;){const t=this._updates.data[e].renderGeometry;this._pendingAddsRemoves.has(t)?this._updates.removeUnorderedIndex(e):e++}}get test(){return{sortedMaterialRenderers:this._sortedMaterialRenderers}}};e([l()],ir.prototype,"rctx",void 0),e([l()],ir.prototype,"materialRepository",void 0),e([l()],ir.prototype,"updating",null),ir=e([g("esri.views.3d.webgl-engine.lib.SortedRenderGeometryRenderer")],ir);var sr=Object.freeze({__proto__:null,build:function(){const e=new Y;return e.include(Je),e.fragment.uniforms.add("tex","sampler2D"),e.fragment.uniforms.add("color","vec4"),e.fragment.code.add(Q`
    void main() {
      vec4 texColor = texture2D(tex, uv);
      gl_FragColor = texColor * color;
    }
  `),e}});class or extends ie{initializeProgram(e){const t=or.shader.get().build();return new fe(e.rctx,t.generateSource("vertex"),t.generateSource("fragment"),se)}initializePipeline(){return this.configuration.hasAlpha?ve({blending:Re(770,1,771,771),colorWrite:we}):ve({colorWrite:we})}}or.shader=new ce(sr,(()=>Promise.resolve().then((function(){return sr}))));class ar extends re{constructor(){super(...arguments),this.hasAlpha=!1}}function nr(e,t,r){(r=r||e).length=e.length;for(let i=0;i<e.length;i++)r[i]=e[i]*t[i];return r}function dr(e,t,r){(r=r||e).length=e.length;for(let i=0;i<e.length;i++)r[i]=e[i]*t;return r}function lr(e,t,r){(r=r||e).length=e.length;for(let i=0;i<e.length;i++)r[i]=e[i]+t[i];return r}function hr(e){return(e+1)*(e+1)}function cr(e,t,r){const i=e[0],s=e[1],o=e[2],a=r||[];return a.length=hr(t),t>=0&&(a[0]=.28209479177),t>=1&&(a[1]=.4886025119*i,a[2]=.4886025119*o,a[3]=.4886025119*s),t>=2&&(a[4]=1.09254843059*i*s,a[5]=1.09254843059*s*o,a[6]=.31539156525*(3*o*o-1),a[7]=1.09254843059*i*o,a[8]=.54627421529*(i*i-s*s)),a}function ur(e,t){const r=(i=t.r.length,v(Math.floor(Math.sqrt(i)-1),0,2));var i;for(const i of e)S(yr,i.direction),cr(yr,r,_r),nr(_r,wr),dr(_r,i.intensity[0],vr),lr(t.r,vr),dr(_r,i.intensity[1],vr),lr(t.g,vr),dr(_r,i.intensity[2],vr),lr(t.b,vr);return t}function mr(e,t,r){!function(e,t){const r=hr(e),i=t||{r:[],g:[],b:[]};i.r.length=i.g.length=i.b.length=r;for(let e=0;e<r;e++)i.r[e]=i.g[e]=i.b[e]=0}(t,r.sphericalHarmonics.sh),b(r.main.intensity,0,0,0);let i=!1;const s=pr,o=gr,a=fr;s.length=0,o.length=0,a.length=0;for(const t of e)t instanceof et&&!i?(x(r.main.direction,t.direction),r.main.intensity[0]=t.intensity[0],r.main.intensity[1]=t.intensity[1],r.main.intensity[2]=t.intensity[2],r.main.castShadows=t.castShadows,i=!0):t instanceof et||t instanceof tt?s.push(t):t instanceof rt?o.push(t):t instanceof it&&a.push(t);ur(s,r.sphericalHarmonics.sh),function(e,t){cr(yr,0,_r);for(const r of e)t.r[0]+=_r[0]*wr[0]*r.intensity[0]*4*Math.PI,t.g[0]+=_r[0]*wr[0]*r.intensity[1]*4*Math.PI,t.b[0]+=_r[0]*wr[0]*r.intensity[2]*4*Math.PI}(o,r.sphericalHarmonics.sh);for(const e of a)lr(r.sphericalHarmonics.sh.r,e.sh.r),lr(r.sphericalHarmonics.sh.g,e.sh.g),lr(r.sphericalHarmonics.sh.b,e.sh.b)}e([te()],ar.prototype,"hasAlpha",void 0);const pr=[],gr=[],fr=[],_r=[0],vr=[0],yr=w(),wr=[3.141593,2.094395,2.094395,2.094395,.785398,.785398,.785398,.785398,.785398],Rr=w();class Tr{constructor(){this._renderLighting={main:{intensity:w(),direction:R(1,0,0),castShadows:!1},sphericalHarmonics:{sh:{r:[0],g:[0],b:[0]}}},this._shOrder=2,this._oldSunlight={direction:w(),ambient:{color:w(),intensity:0},diffuse:{color:w(),intensity:0}}}setLightDirectionUniform(e){e.setUniform3fv("lightingMainDirection",this._renderLighting.main.direction)}setUniforms(e,t=!1){if(t){const t=(1-this._info.groundLightingFactor)*(1-this._info.globalFactor);e.setUniform1f("lightingFixedFactor",t)}else e.setUniform1f("lightingFixedFactor",0);e.setUniform1f("lightingGlobalFactor",this._info.globalFactor),this.setLightDirectionUniform(e),e.setUniform3fv("lightingMainIntensity",this._renderLighting.main.intensity),e.setUniform1f("ambientBoostFactor",.4);const r=this._renderLighting.sphericalHarmonics.sh;0===this._shOrder?e.setUniform3f("lightingAmbientSH0",r.r[0],r.g[0],r.b[0]):1===this._shOrder?(e.setUniform4f("lightingAmbientSH_R",r.r[0],r.r[1],r.r[2],r.r[3]),e.setUniform4f("lightingAmbientSH_G",r.g[0],r.g[1],r.g[2],r.g[3]),e.setUniform4f("lightingAmbientSH_B",r.b[0],r.b[1],r.b[2],r.b[3])):2===this._shOrder&&(e.setUniform3f("lightingAmbientSH0",r.r[0],r.g[0],r.b[0]),e.setUniform4f("lightingAmbientSH_R1",r.r[1],r.r[2],r.r[3],r.r[4]),e.setUniform4f("lightingAmbientSH_G1",r.g[1],r.g[2],r.g[3],r.g[4]),e.setUniform4f("lightingAmbientSH_B1",r.b[1],r.b[2],r.b[3],r.b[4]),e.setUniform4f("lightingAmbientSH_R2",r.r[5],r.r[6],r.r[7],r.r[8]),e.setUniform4f("lightingAmbientSH_G2",r.g[5],r.g[6],r.g[7],r.g[8]),e.setUniform4f("lightingAmbientSH_B2",r.b[5],r.b[6],r.b[7],r.b[8]))}set(e){this._info=e,mr(e.lights,this._shOrder,this._renderLighting),S(this._oldSunlight.direction,this._renderLighting.main.direction);const t=1/Math.PI;this._oldSunlight.ambient.color[0]=.282095*this._renderLighting.sphericalHarmonics.sh.r[0]*t,this._oldSunlight.ambient.color[1]=.282095*this._renderLighting.sphericalHarmonics.sh.g[0]*t,this._oldSunlight.ambient.color[2]=.282095*this._renderLighting.sphericalHarmonics.sh.b[0]*t,this._oldSunlight.ambient.intensity=1,this._oldSunlight.diffuse.color[0]=this._renderLighting.main.intensity[0]*t,this._oldSunlight.diffuse.color[1]=this._renderLighting.main.intensity[1]*t,this._oldSunlight.diffuse.color[2]=this._renderLighting.main.intensity[2]*t,this._oldSunlight.diffuse.intensity=1;const r=x(Rr,this._oldSunlight.diffuse.color);E(r,r,.4*this._info.globalFactor),C(this._oldSunlight.ambient.color,this._oldSunlight.ambient.color,r)}get globalFactor(){return this._info.globalFactor}get old(){return this._oldSunlight}}const br=i.getLogger("esri.views.3d.webgl-engine.lib.OverlayRenderer");let xr=class extends(ge(h)){constructor(e){super(e),this._overlays=null,this._hasHighlights=!1,this._rendersOccluded=!1,this._hasWater=!1,this._lighting=new Tr,this._localOrigins=new Tt,this._handles=new M,this._layerRenderers=new Map,this._sortedLayerRenderersDirty=!1,this._sortedLayerRenderers=new m,this._geometries=new Map,this._uniqueIdx=0,this.globalUnitScale=1,this.longitudeCyclical=null}initialize(){this._rctx=this.renderView.renderingContext,this._renderContext=new bt(this._rctx,null,null,null,null,null),this._stippleTextureRepository=new Ke,this.waterTextureRepository=this.renderView.waterTextureRepository,this._shaderTechniqueRepository=new mt({rctx:this._rctx,viewingMode:2,commonUniformStore:new ct,stippleTextureRepository:this._stippleTextureRepository,waterTextureRepository:this.waterTextureRepository}),D(this.waterTextureRepository,"loadingState",(()=>this.emitContentChanged())),this._materialRepository=new vt(this.renderView.textureRepository,this._shaderTechniqueRepository),this._materialRepository.onMaterialChanged=e=>{(e.renderOccluded&Dr)>0!==this._rendersOccluded&&this.updateRendersOccluded(),this.emitContentChanged(),this.notifyChange("updating")},this._compositingHelper=this.renderView.compositingHelper,this._lighting.set({lights:[new rt(R(1,1,1))],groundLightingFactor:1,globalFactor:0}),this._bindParameters={slot:null,highlightDepthTexture:Ae(this._rctx),camera:Cr,inverseViewport:Z(),origin:null,screenToWorldRatio:null,screenToWorldRatioGlobalWM:null,shadowMappingEnabled:!1,slicePlane:null,ssaoEnabled:!1,hasOccludees:!1,linearDepthTexture:null,linearDepthTextureID:0,lastFrameColorTexture:null,lastFrameColorTextureID:0,reprojectionMat:null,ssrEnabled:!1,lighting:this._lighting,transparencyPassType:3}}dispose(){this._handles.destroy(),this._layerRenderers.forEach((e=>e.dispose())),this._layerRenderers.clear(),this._bindParameters.highlightDepthTexture.dispose(),this._bindParameters.highlightDepthTexture=null,this._shaderTechniqueRepository.dispose(),this._shaderTechniqueRepository=null}get updating(){return this._sortedLayerRenderersDirty||q(this._layerRenderers,(e=>e.updating))||this.waterTextureRepository.updating}get hasOverlays(){return s(this._overlays)}get gpuMemoryUsage(){return s(this._overlays)?this._overlays[0].getGpuMemoryUsage()+this._overlays[1].getGpuMemoryUsage():0}get overlays(){return this._overlays}forEachOverlay(e){s(this._overlays)&&(e(this._overlays[0],0),e(this._overlays[1],1))}ensureOverlays(){if(r(this._overlays)){const e=this.renderView.renderingContext;this._overlays=[new lt(e,0),new lt(e,1)]}}disposeOverlays(){s(this._overlays)&&(this._overlays.forEach((e=>e.dispose())),this._overlays=null)}commitChanges(){let e=!1;this._layerRenderers.forEach(((t,r)=>{t.commitChanges()&&(e=!0),t.isEmpty&&(this._layerRenderers.delete(r),this._sortedLayerRenderersDirty=!0,this._handles.remove(r))})),this._sortedLayerRenderersDirty&&(this.updateSortedLayerRenderers(),e=!0),e&&(this.notifyChange("updating"),this.emitContentChanged(),this.updateHasHighlights(),this.updateRendersOccluded(),this.updateHasWater())}addGeometries(e,t,r){for(const t of e)null==t.origin&&(t.origin=this._localOrigins.getOrigin(t.center)),t.uniqueName||(t.uniqueName="ov:"+this._uniqueIdx++),this._geometries.set(t.uniqueName,t);this.ensureLayerRenderer(t).add(e),2===r&&this.notifyGraphicUpdate(e,t,2)}removeGeometries(e,t,r){for(const t of e)this._geometries.delete(t.uniqueName);const i=this._layerRenderers.get(t);i&&(i.remove(e),2===r&&this.notifyGraphicUpdate(e,t,2))}updateGeometries(e,t,r){const i=this._layerRenderers.get(t);i?(i.modify(e,r),this.notifyUpdateGeometries(e,t,r)):br.warn("Attempted to update geometry for nonexistent layer")}notifyUpdateGeometries(e,t,r){const i=4===r||2===r?2:1===r?1:0;this.notifyGraphicUpdate(e,t,i)}notifyGraphicUpdate(e,t,i){if(0===i||r(t.notifyGraphicUpdate))return;let s=-1;for(const r of e){const e=r.data.graphicUid;e!==s&&(t.notifyGraphicUpdate(e,i),s=e)}}updateHighlights(e,t){const r=this._layerRenderers.get(t);r?r.modify(e,8):br.warn("Attempted to update highlights for nonexistent layer")}isEmpty(){return 0===this._geometries.size&&!ot.OVERLAY_DRAW_DEBUG_TEXTURE}get hasHighlights(){return this._hasHighlights}get hasWater(){return this._hasWater}get rendersOccluded(){return this._rendersOccluded}updateLogic(e){let t=!1;return this._layerRenderers.forEach((r=>{r.updateLogic(e)&&(t=!0)})),t}updateLayerOrder(){this._sortedLayerRenderersDirty=!0}drawPass(e,t,r,i=0){if(0===r.numViews)return!1;if(this._screenToWorldRatio=r.pixelRatio*Math.abs(r.views[0].extent[2]-r.views[0].extent[0])/Math.abs(r.views[0].viewport[2]-r.views[0].viewport[0]),this.isEmpty()||5===e&&!this.hasHighlights||3===e&&!this.hasWater)return!1;if(!this.hasNonZeroSizedView(r))return!1;const o=r.width,n=r.height;if(!t.isValid())return!1;t.resize(o,n);const d=this._rctx;if(Cr.pixelRatio=r.pixelRatio||1,this._renderContext.pass=e,this._bindParameters.screenToWorldRatio=this._screenToWorldRatio,this._bindParameters.screenToWorldRatioGlobalWM=this._screenToWorldRatio*this.globalUnitScale,t.bind(d),d.setClearColor(0,0,0,0),d.clearSafe(16384),1===i&&(this._renderContext.renderOccludedMask=Dr),ot.OVERLAY_DRAW_DEBUG_TEXTURE&&1!==i)for(let e=0;e<r.numViews;e++)this.setViewParameters(r.views[e],Cr),this.drawDebugTexture(o,n,Er[r.index%Er.length]);return this._layerRenderers.size>0&&this._sortedLayerRenderers.forAll((({layerView:l,renderer:h})=>{if(2===i&&s(l)&&0===l.drapeSourceType)return;const c=s(l)&&s(l.fullOpacity)&&l.fullOpacity<1&&0===e;c&&(this.bindTemporaryFramebuffer(this._rctx,o,n),d.clearSafe(16384));for(let e=0;e<r.numViews;e++)this.setViewParameters(r.views[e],Cr),h.draw(this._renderContext,this._bindParameters);c&&s(this._temporaryRenderTarget)&&(t.bind(d),this._compositingHelper.composite(this._temporaryRenderTarget.getTexture(),2,a(a(l).fullOpacity)))})),d.bindFramebuffer(null),t.generateMipMap(),this._renderContext.resetRenderOccludedMask(),!0}bindTemporaryFramebuffer(e,t,i){r(this._temporaryRenderTarget)&&(this._temporaryRenderTarget=new at(e,"temp",0,!1)),this._temporaryRenderTarget.resize(t,i),this._temporaryRenderTarget.bind(e)}async reloadShaders(){await this._shaderTechniqueRepository.hotReload()}intersect(e,t,r){let i=0;this._geometries.forEach((s=>{if(r&&!r(s))return;this.intersectRenderGeometry(s,t,0,e,i);const o=this.longitudeCyclical;o&&(s.center[0]-s.bsRadius<o.min&&this.intersectRenderGeometry(s,t,o.range,e,i),s.center[0]+s.bsRadius>o.max&&this.intersectRenderGeometry(s,t,-o.range,e,i)),i++}))}intersectRenderGeometry(e,t,r,i,o){let a=0;s(e.transformation)&&(r+=e.transformation[12],a=e.transformation[13]),Or[0]=t[0]-r,Or[1]=t[1]-a,Or[2]=1,Ir[0]=t[0]-r,Ir[1]=t[1]-a,Ir[2]=0,e.screenToWorldRatio=this._screenToWorldRatio,e.material.intersect(e,null,e.getShaderTransformation(),i,Or,Ir,((t,r,s)=>{this.addIntersectionResult(s,e.material.renderPriority,o,i,e.data.layerUid,e.data.graphicUid)}),e.calculateShaderTransformation,!0)}addIntersectionResult(e,t,r,i,s,o){const a={type:"external",metadata:{layerUid:s,graphicUid:o}},n=s=>{s.set(a,null,i.results.ground.dist,i.results.ground.normal,i.results.ground.transformation,t,null,null,e,r),s.intersector="OverlayRenderer"};if((null==i.results.min.drapedLayerOrder||t>=i.results.min.drapedLayerOrder)&&(null==i.results.min.dist||i.results.ground.dist<=i.results.min.dist)&&n(i.results.min),0!==i.options.store&&(null==i.results.max.drapedLayerOrder||t<i.results.max.drapedLayerOrder)&&(null==i.results.max.dist||i.results.ground.dist>i.results.max.dist)&&n(i.results.max),2===i.options.store){const e=new Be(i.ray);n(e),i.results.all.push(e)}}stopAnimationsAtTime(e){this._sortedLayerRenderers.forAll((t=>t.renderer.stopAnimationsAtTime(e)))}ensureLayerRenderer(e){let t=this._layerRenderers.get(e);return t||(t=new ir({rctx:this._rctx,materialRepository:this._materialRepository}),this._layerRenderers.set(e,t),this._sortedLayerRenderersDirty=!0,"fullOpacity"in e&&this._handles.add(e.watch("fullOpacity",(()=>this.emitContentChanged())),e),this._handles.add(D(t,"updating",(()=>this.notifyChange("updating"))),e)),t}updateSortedLayerRenderers(){if(!this._sortedLayerRenderersDirty)return;if(this._sortedLayerRenderersDirty=!1,this._sortedLayerRenderers.clear(),0===this._layerRenderers.size)return;const[{view:{allLayerViews:e}}]=this._layerRenderers.keys(),t=new Set(this._layerRenderers.values());e.forEach((e=>{const r=e,i=this._layerRenderers.get(r);i&&(this._sortedLayerRenderers.push(new Sr(r,i)),t.delete(i))})),t.forEach((e=>{this._sortedLayerRenderers.push(new Sr(null,e))}))}setViewParameters(e,t){t.viewport=e.viewport;const r=e.extent;L(t.projectionMatrix,0,r[2]-r[0],0,r[3]-r[1],t.near,t.far),H(t.viewMatrix),N(t.viewMatrix,t.viewMatrix,[-e.extent[0],-e.extent[1],0]),t.setGLViewport(this._rctx),this._renderContext.camera=t,this._bindParameters.camera=t,this._bindParameters.inverseViewport[0]=1/t.fullViewport[2],this._bindParameters.inverseViewport[1]=1/t.fullViewport[3]}hasNonZeroSizedView(e){for(let t=0;t<e.numViews;t++){const r=e.views[t];if(r.extent[0]!==r.extent[2]&&r.extent[1]!==r.extent[3])return!0}return!1}emitContentChanged(){this.onContentChanged&&this.onContentChanged()}updateHasWater(){const e=q(this._layerRenderers,(e=>e.hasWater));e!==this._hasWater&&(this._hasWater=e)}updateHasHighlights(){const e=q(this._layerRenderers,(e=>e.hasHighlights));e!==this._hasHighlights&&(this._hasHighlights=e,this.onHasHighlightsChanged&&this.onHasHighlightsChanged(this._hasHighlights))}updateRendersOccluded(){const e=q(this._layerRenderers,(e=>e.rendersOccluded));e!==this._rendersOccluded&&(this._rendersOccluded=e,this.onRendersOccludedChanged&&this.onRendersOccludedChanged(this.rendersOccluded))}drawDebugTexture(e,t,r){const i=this._rctx;this.ensureDebugPatternResources(e,t);const s=this._debugTextureTechnique.program;i.bindProgram(s),i.setPipelineState(this._debugTextureTechnique.pipeline),s.setUniform4f("color",r[0],r[1],r[2],1),s.setUniform1i("tex",0),i.bindTexture(this._debugPatternTexture,0),i.bindVAO(this._quadVAO),i.drawArrays(5,0,ze(this._quadVAO,"geometry"))}ensureDebugPatternResources(e,t){if(this._debugPatternTexture)return;const r=new Uint8Array(e*t*4);let i=0;for(let s=0;s<t;s++)for(let o=0;o<e;o++){const a=Math.floor(o/10),n=Math.floor(s/10);a<2||n<2||10*a>e-20||10*n>t-20?(r[i++]=255,r[i++]=255,r[i++]=255,r[i++]=255):(r[i++]=255,r[i++]=255,r[i++]=255,r[i++]=1&a&&1&n?1&o^1&s?0:255:1&a^1&n?0:128)}this._debugPatternTexture=new Te(this._rctx,{target:3553,pixelFormat:6408,dataType:5121,samplingMode:9728,width:e,height:t},r);const s=new ar;s.hasAlpha=!0,this._debugTextureTechnique=this._shaderTechniqueRepository.acquireAndReleaseExisting(or,s,this._debugTextureTechnique),this._quadVAO=Ue(this._rctx)}get test(){return{layerRenderers:this._layerRenderers}}};e([pe()],xr.prototype,"_shaderTechniqueRepository",void 0),e([pe()],xr.prototype,"_stippleTextureRepository",void 0),e([l(),pe()],xr.prototype,"waterTextureRepository",void 0),e([l({constructOnly:!0})],xr.prototype,"renderView",void 0),e([l()],xr.prototype,"globalUnitScale",void 0),e([l({type:Boolean,readOnly:!0,dependsOn:["waterTextureRepository.updating"]})],xr.prototype,"updating",null),xr=e([g("esri.views.3d.terrain.OverlayRenderer")],xr);class Sr{constructor(e,t){this.layerView=e,this.renderer=t}}const Er=[[1,.5,.5],[.5,.5,1],[.5,1,.5]],Cr=new ke;Cr.near=1,Cr.far=1e4,Cr.relativeElevation=null;const Or=w(),Ir=w(),Mr=-2,Dr=4,Pr=1.2,Ar=F,Ur=G;function Lr(e){const i={},o={};!function(e,r,i){const{attributeData:{position:s},removeDuplicateStartEnd:o}=e,a=function(e){const t=e.length;return e[0]===e[t-3]&&e[1]===e[t-2]&&e[2]===e[t-1]}(s)&&1===o,n=s.length/3-(a?1:0),d=new Uint32Array(2*(n-1)),l=a?t(s,0,s.length-3):s;let h=0;for(let e=0;e<n-1;e++)d[h++]=e,d[h++]=e+1;r[Ee.POSITION]={size:3,data:l,offsetIdx:0,strideIdx:3},i[Ee.POSITION]=d}(e,o,i);const a=new Uint32Array(i[Ee.POSITION].length);return function(e,t,i){const s=e.attributeData.mapPosition;if(r(s))return;i.mapPos=i[Ee.POSITION],t.mapPos={size:3,data:s,offsetIdx:0,strideIdx:3}}(e,o,i),function(e,t,r,i){if(s(e.attributeData.colorFeature))return;const o=e.attributeData.color;t[Ee.COLOR]={size:4,data:n(o,Ur),offsetIdx:0,strideIdx:4},r[Ee.COLOR]=i}(e,o,i,a),function(e,t,r,i){if(s(e.attributeData.sizeFeature))return;const o=e.attributeData.size;t[Ee.SIZE]={size:1,data:new Float32Array([n(o,1)]),offsetIdx:0,strideIdx:1},r[Ee.SIZE]=i}(e,o,i,a),function(e,t,i,s){const o=e.attributeData.colorFeature;if(r(o))return;t[Ee.COLORFEATUREATTRIBUTE]={size:1,data:new Float32Array([o]),offsetIdx:0,strideIdx:1},i[Ee.COLOR]=s}(e,o,i,a),function(e,t,i,s){const o=e.attributeData.sizeFeature;if(r(o))return;t[Ee.SIZEFEATUREATTRIBUTE]={size:1,data:new Float32Array([o]),offsetIdx:0,strideIdx:1},i[Ee.SIZEFEATUREATTRIBUTE]=s}(e,o,i,a),function(e,t,i,s){const o=e.attributeData.opacityFeature;if(r(o))return;t[Ee.OPACITYFEATUREATTRIBUTE]={size:1,data:new Float32Array([o]),offsetIdx:0,strideIdx:1},i[Ee.OPACITYFEATUREATTRIBUTE]=s}(e,o,i,a),function(e,t,r){if("round"!==e.join)return;const i=t[Ee.POSITION].data,s=i.length/3,o=new Float32Array(s),a=Fr,d=Gr;b(a,0,0,0);const l=n(e.uniformSize,1);for(let e=-1;e<s;++e){const t=e<0?s+e:e,r=(e+1)%s;if(b(d,i[3*r+0]-i[3*t+0],i[3*r+1]-i[3*t+1],i[3*r+2]-i[3*t+2]),O(d,d),e>=0){const t=1*((Math.PI-y(I(a,d)))*Vr)*zr(l);o[e]=Math.max(Math.floor(t),0)}E(a,d,-1)}t[Ee.SUBDIVISIONS]={size:1,data:o,offsetIdx:0,strideIdx:1},r[Ee.SUBDIVISIONS]=r[Ee.POSITION]}(e,o,i),new De(o,i,"line")}function Hr(e,t,r,i){const s="polygon"===e.type?1:0,o="polygon"===e.type?e.rings:e.paths,{position:a,outlines:n}=V(o,e.hasZ,s),d=new Float64Array(a.length),l=Ze(a,e.spatialReference,0,d,0,a,0,a.length/3,t,r,i),h=null!=l;return{lines:h?Nr(n,a,d):[],projectionSuccess:h,sampledElevation:l}}function Nr(e,t,r){const i=new Array;for(const{index:s,count:o}of e){if(o<=1)continue;const e=3*s,a=e+3*o;i.push({position:t.subarray(e,a),mapPosition:r?r.subarray(e,a):void 0})}return i}function jr(e,t){const r="polygon"===e.type?1:0,i="polygon"===e.type?e.rings:e.paths,{position:s,outlines:o}=V(i,!1,r),a=z(s,e.spatialReference,0,s,t,0,s.length/3);for(let e=2;e<s.length;e+=3)s[e]=-2;return{lines:a?Nr(o,s):[],projectionSuccess:a}}function zr(e){return 1.863798+-2.0062872/Math.pow(1+e/18.2313,.8856294)}const Fr=w(),Gr=w(),Vr=4/Math.PI;export{rt as A,ct as C,Mr as D,tt as F,Tt as G,et as M,At as N,xr as O,$t as R,It as S,Ar as T,jt as W,jr as a,zt as b,Lr as c,ot as d,Lt as e,dt as f,Hr as g,Tr as h,bt as i,Xt as j,Vt as k,ar as l,or as m,mt as n,Dr as o,vt as p,Pr as q,St as s};
