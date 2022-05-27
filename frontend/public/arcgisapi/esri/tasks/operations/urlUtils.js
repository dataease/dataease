// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){function f(g){const c={};for(const b in g){if("declaredClass"===b)continue;const a=g[b];if(null!=a&&"function"!==typeof a)if(Array.isArray(a)){c[b]=[];for(let d=0;d<a.length;d++)c[b][d]=f(a[d])}else"object"===typeof a?a.toJSON&&(c[b]=JSON.stringify(a)):c[b]=a}return c}e.mapParameters=f;Object.defineProperty(e,"__esModule",{value:!0})});