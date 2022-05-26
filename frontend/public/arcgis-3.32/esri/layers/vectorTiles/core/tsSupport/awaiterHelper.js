// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/tsSupport/awaiterHelper",["dojo/Deferred","dojo/when"],function(g,f){return function(h,k,n,c){function l(a){try{d(c.next(a))}catch(e){b.reject(e)}}function m(a){try{d(c["throw"](a))}catch(e){b.reject(e)}}function d(a){a.done?f(a.value).then(b.resolve,b.reject):f(a.value).then(l,m)}var b=new g;d((c=c.apply(h,k||[])).next());return b.promise}});