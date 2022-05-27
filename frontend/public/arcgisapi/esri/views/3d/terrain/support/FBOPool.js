// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/ObjectPool","../../../webgl/FramebufferObject"],function(d,g,e){let h=function(){function f(a){this.renderingContext=a;this._pools=new Map}var c=f.prototype;c.acquire=function(a){return this.getPool(a).acquire()};c.release=function(a){this.getPool(a.width).release(a)};c.clear=function(){this._pools.forEach(a=>{a.destroy()});this._pools.clear()};c.getPool=function(a){var b=this._pools.get(a);b||(b=e.bind(e,this.renderingContext,{colorTarget:0,depthStencilTarget:1,
width:a,height:a}),b=new g(b),this._pools.set(a,b));return b};return f}();d.FBOPool=h;Object.defineProperty(d,"__esModule",{value:!0})});