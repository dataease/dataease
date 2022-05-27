// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../chunks/vec3f64","../webgl-engine/lib/Camera"],function(b,e){return function(){function c(a){this.view=a;this._renderTargetHelper=null;this.camera=new e;this.sunLight={direction:b.create(),diffuse:{color:b.create(),intensity:1},ambient:{color:b.create(),intensity:1}}}var d=c.prototype;d.resetWebGLState=function(){null!=this.rctx&&(this.rctx.enforceState(),this._renderTargetHelper&&this._renderTargetHelper.bindFramebuffer())};d.bindRenderTarget=function(){if(this._renderTargetHelper){const a=
this._renderTargetHelper.framebuffer;a.initialize();this.gl.bindFramebuffer(this.gl.FRAMEBUFFER,a.glName)}};return c}()});