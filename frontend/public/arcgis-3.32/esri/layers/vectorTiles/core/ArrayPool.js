// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/ArrayPool",["require","exports","./ObjectPool"],function(c,h,e){function f(a){a.length=0}var g=Array.prototype.splice;c=function(){function a(b,a){void 0===b&&(b=50);void 0===a&&(a=50);this._pool=new e(Array,!1,f,a,b)}a.prototype.acquire=function(){return this._pool.acquire()};a.prototype.copy=function(a){var b=this.acquire();a.unshift(0,0);g.apply(b,a);a.splice(0,2);return b};a.prototype.release=function(a){this._pool.release(a)};a.acquire=function(){return d.acquire()};
a.copy=function(a){return d.copy(a)};a.release=function(a){return d.release(a)};return a}();var d=new c(100);return c});