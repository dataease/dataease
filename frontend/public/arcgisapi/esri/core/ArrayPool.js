// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["./ObjectPool"],function(g){function h(b){b.length=0}const k=Array.prototype.splice;let f=function(){function b(a=50,c=50){this._pool=new g(Array,void 0,h,c,a)}var d=b.prototype;d.acquire=function(){return this._pool.acquire()};d.copy=function(a){const c=this.acquire();a.unshift(0,0);k.apply(c,a);a.splice(0,2);return c};d.release=function(a){this._pool.release(a)};d.prune=function(){this._pool.prune(0)};b.acquire=function(){return e.acquire()};b.copy=function(a){return e.copy(a)};b.release=
function(a){return e.release(a)};b.prune=function(){e.prune()};return b}();const e=new f(100);return f});