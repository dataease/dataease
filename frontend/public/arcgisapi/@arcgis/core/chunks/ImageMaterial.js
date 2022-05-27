/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import{S as t,T as i,g as a,e as s,Y as r,C as n,p as o,a as l,b as c,D as u,V as p,i as d,j as h,k as g,l as f,m,n as v,o as P,R as y,M as b,q as x,r as C,t as O}from"./PiUtils.glsl.js";import{P as q}from"./Program.js";import{c as T,m as S,a as D,d as w}from"./isWebGL2Context.js";import{D as _,e as F}from"./ColorMaterial.js";import{G as E}from"./GLMaterialTexture.js";var H=Object.freeze({__proto__:null,build:function(e){const o=new t;return o.include(i,{linearDepth:!1}),o.vertex.uniforms.add("proj","mat4").add("view","mat4"),o.attributes.add("position","vec3"),o.attributes.add("uv0","vec2"),o.varyings.add("vpos","vec3"),o.vertex.uniforms.add("textureCoordinateScaleFactor","vec2"),o.vertex.code.add(a`
    void main(void) {
      vpos = position;
      vTexCoord = uv0 * textureCoordinateScaleFactor;
      gl_Position = transformPosition(proj, view, vpos);
    }
  `),o.include(s,e),o.fragment.uniforms.add("tex","sampler2D"),o.fragment.uniforms.add("opacity","float"),o.varyings.add("vTexCoord","vec2"),7===e.output?o.fragment.code.add(a`
    void main() {
      discardBySlice(vpos);

      float alpha = texture2D(tex, vTexCoord).a * opacity;
      if (alpha  < ${a.float(r)}) {
        discard;
      }

      gl_FragColor = vec4(alpha);
    }
    `):(o.fragment.include(n),o.fragment.code.add(a`
    void main() {
      discardBySlice(vpos);
      gl_FragColor = texture2D(tex, vTexCoord) * opacity;

      if (gl_FragColor.a < ${a.float(r)}) {
        discard;
      }

      gl_FragColor = highlightSlice(gl_FragColor, vpos);
      ${e.OITEnabled?"gl_FragColor = premultiplyAlpha(gl_FragColor);":""}
    }
    `)),o}});class j extends c{initializeProgram(e){const t=j.shader.get(),i=this.configuration,a=t.build({output:i.output,slicePlaneEnabled:i.slicePlaneEnabled,sliceHighlightDisabled:i.sliceHighlightDisabled,sliceEnabledForVertexPrograms:!1,OITEnabled:0===i.transparencyPassType});return new q(e.rctx,a.generateSource("vertex"),a.generateSource("fragment"),u)}bindPass(e,t,i){p.bindProjectionMatrix(this.program,i.camera.projectionMatrix),this.program.setUniform1f("opacity",t.opacity)}bindDraw(e){p.bindView(this.program,e),s.bindUniformsWithOrigin(this.program,this.configuration,e)}setPipelineState(e,t){const i=this.configuration,a=3===e,s=2===e;return S({blending:0!==i.output&&7!==i.output||!i.transparent?null:a?W:d(e),culling:(r=i.cullFace,0!==r&&{face:1===r?1028:1029,mode:2305}),depthTest:{func:h(e)},depthWrite:a?i.writeDepth&&D:g(e),colorWrite:w,stencilWrite:i.sceneHasOcludees?f:null,stencilTest:i.sceneHasOcludees?t?m:v:null,polygonOffset:a||s?i.polygonOffset&&M:P(i.enableOffset)});var r}initializePipeline(){return this._occludeePipelineState=this.setPipelineState(this.configuration.transparencyPassType,!0),this.setPipelineState(this.configuration.transparencyPassType,!1)}getPipelineState(e){return e?this._occludeePipelineState:this.pipeline}}j.shader=new y(H,(()=>Promise.resolve().then((function(){return H}))));const M={factor:1,units:1},W=T(1,771);class I extends l{constructor(){super(...arguments),this.output=0,this.cullFace=0,this.slicePlaneEnabled=!1,this.sliceHighlightDisabled=!1,this.transparent=!1,this.polygonOffset=!1,this.enableOffset=!0,this.writeDepth=!0,this.sceneHasOcludees=!1,this.transparencyPassType=3}}e([o({count:8})],I.prototype,"output",void 0),e([o({count:3})],I.prototype,"cullFace",void 0),e([o()],I.prototype,"slicePlaneEnabled",void 0),e([o()],I.prototype,"sliceHighlightDisabled",void 0),e([o()],I.prototype,"transparent",void 0),e([o()],I.prototype,"polygonOffset",void 0),e([o()],I.prototype,"enableOffset",void 0),e([o()],I.prototype,"writeDepth",void 0),e([o()],I.prototype,"sceneHasOcludees",void 0),e([o({count:4})],I.prototype,"transparencyPassType",void 0);class G extends b{constructor(e,t){super(t,e,z),this.supportsEdges=!0,this.techniqueConfig=new I}getTechniqueConfig(e,t){return this.techniqueConfig.output=e,this.techniqueConfig.cullFace=this.params.cullFace,this.techniqueConfig.slicePlaneEnabled=this.params.slicePlaneEnabled,this.techniqueConfig.transparent=this.params.transparent,this.techniqueConfig.writeDepth=this.params.writeDepth,this.techniqueConfig.sceneHasOcludees=this.params.sceneHasOcludees,this.techniqueConfig.transparencyPassType=t?t.transparencyPassType:3,this.techniqueConfig.enableOffset=!t||t.camera.relativeElevation<x,this.techniqueConfig}intersect(e,t,i,a,s,r,n){C(e,t,a,s,r,void 0,n)}getGLMaterial(e){return 0===e.output||7===e.output||4===e.output?new V(e):void 0}createBufferWriter(){return new _(F)}}class V extends E{constructor(e){super({...e,...e.material.params}),this.updateParameters()}updateParameters(e){const t=this.material.params;this.updateTexture(t.textureId),this.technique=this.techniqueRep.acquireAndReleaseExisting(j,this.material.getTechniqueConfig(this.output,e),this.technique)}beginSlot(e){if(4===this.output)return 3===e;return e===(this.technique.configuration.transparent?this.technique.configuration.writeDepth?5:8:3)}_updateOccludeeState(e){e.hasOccludees!==this.material.params.sceneHasOcludees&&(this.material.setParameterValues({sceneHasOcludees:e.hasOccludees}),this.updateParameters(e))}ensureParameters(e){0!==this.output&&7!==this.output||this._updateOccludeeState(e),this.updateParameters(e)}bind(e,t){e.bindProgram(this.technique.program),this.bindTexture(e,this.technique.program),this.bindTextureScale(e,this.technique.program),this.technique.bindPass(e,this.material.params,t)}getPipelineState(e,t){return this.technique.getPipelineState(t)}}const z={transparent:!1,writeDepth:!0,slicePlaneEnabled:!1,cullFace:0,sceneHasOcludees:!1,opacity:1,textureId:null,initTextureTransparent:!0,...O};export{G as I};
