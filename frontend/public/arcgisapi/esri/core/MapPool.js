// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["./ObjectPool"],function(f){let e=function(){function b(a=50,g=50){this._pool=new f(Map,void 0,h=>h.clear(),g,a)}var c=b.prototype;c.acquire=function(){return this._pool.acquire()};c.release=function(a){this._pool.release(a)};b.acquire=function(){return d.acquire()};b.release=function(a){return d.release(a)};return b}();const d=new e(100);return e});