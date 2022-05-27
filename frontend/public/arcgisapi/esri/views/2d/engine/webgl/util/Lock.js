// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../../core/promiseUtils"],function(c,e){let f=function(){function a(){this._resolver=null}var b=a.prototype;b.isHeld=function(){return!!this._resolver};b.acquire=function(){return this._resolver?this._resolver.promise.then(()=>this.acquire()):(this._resolver=e.createResolver(),e.resolve())};b.release=function(){const d=this._resolver;this._resolver=null;d.resolve()};return a}();c.default=f;c.withLock=function(a,b,d){return a.acquire().then(()=>b(d)).then(()=>a.release()).catch(g=>
{a.release();throw g;})};Object.defineProperty(c,"__esModule",{value:!0})});