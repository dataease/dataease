// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../chunks/_rollupPluginBabelHelpers","./webgl/enums","./brushes","./webgl/WGLContainer"],function(d,f,g,h,a){a=function(b){function c(){return b.apply(this,arguments)||this}f._inheritsLoose(c,b);c.prototype.prepareRenderPasses=function(e){const k=e.registerRenderPass({name:"bitmap",brushes:[h.brushes.bitmap],target:()=>this.children,drawPhase:g.WGLDrawPhase.MAP});return[...b.prototype.prepareRenderPasses.call(this,e),k]};return c}(a);d.BitmapContainer=a;Object.defineProperty(d,
"__esModule",{value:!0})});