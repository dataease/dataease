// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../core/urlUtils"],function(f,l){function h(b,e,k){const d={};for(const c in b)if("declaredClass"!==c){var a=b[c];if(null!=a&&"function"!==typeof a)if(Array.isArray(a)){d[c]=[];for(let g=0;g<a.length;g++)d[c][g]=h(a[g])}else"object"===typeof a?a.toJSON?(a=a.toJSON(k&&k[c]),d[c]=e?a:JSON.stringify(a)):d[c]=e?a:JSON.stringify(a):d[c]=a}return d}f.asValidOptions=function(b,e){b={query:b};e&&(b={...e,...b});return b};f.encode=h;f.parseUrl=function(b){return"string"===typeof b?l.urlToObject(b):
b};Object.defineProperty(f,"__esModule",{value:!0})});