// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../chunks/_rollupPluginBabelHelpers","./MemCache"],function(e,f){return function(){function d(a,c){this._storage=new f.MemCacheStorage;this._storage.maxSize=a;c&&this._storage.registerRemoveFunc("",c)}var b=d.prototype;b.put=function(a,c,g){this._storage.put(a,c,g,1)};b.pop=function(a){return this._storage.pop(a)};b.get=function(a){return this._storage.get(a)};b.clear=function(){this._storage.clearAll()};b.destroy=function(){this._storage.clearAll()};e._createClass(d,[{key:"maxSize",get:function(){return this._storage.maxSize},
set:function(a){this._storage.maxSize=a}}]);return d}()});