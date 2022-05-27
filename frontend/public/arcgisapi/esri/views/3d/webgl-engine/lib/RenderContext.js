// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../../chunks/_rollupPluginBabelHelpers","./Camera"],function(b,c){return function(){function a(d,e,f,g,h,k){this.camera=null;this.lastFrameCamera=new c;this.slot=this.pass=0;this.highlightDepthTexture=null;this.renderOccludedMask=13;this.hasOccludees=!1;this.rctx=d;this.offscreenRenderingHelper=e;this.scenelightingData=f;this.shadowMap=g;this.ssaoHelper=h;this.sliceHelper=k}a.prototype.resetRenderOccludedMask=function(){this.renderOccludedMask=13};b._createClass(a,[{key:"isHighlightPass",
get:function(){return 5===this.pass}}]);return a}()});