// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/kebabDictionary",["require","exports"],function(k,l){return function(b,c){void 0===c&&(c={});var f=c.ignoreUnknown||!1,d={},e;for(e in b)d[b[e]]=e;var g=function(a){return d.hasOwnProperty(a)?d[a]:f?void 0:a},h=function(a){return b.hasOwnProperty(a)?b[a]:f?void 0:a};return{toJSON:g,fromJSON:h,read:function(a){return h(a)},write:function(a,b,c){a=g(a);void 0!==a&&(b[c]=a)}}}});