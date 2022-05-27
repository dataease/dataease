// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(g){let k=function(){function c(d={}){this._options=d}c.prototype.toQueryParams=function(d){if(!d)return null;const e=d.toJSON(),f={};Object.keys(e).forEach(b=>{var a=this._options[b];if(a){const h="boolean"!==typeof a&&a.name?a.name:b;b="boolean"!==typeof a&&a.getter?a.getter(e):e[b];null!=b&&(a=b,Array.isArray(a)?([a]=a,a="number"===typeof a||"string"===typeof a):a=!1,f[h]=a?b.join(","):"object"===typeof b?JSON.stringify(b):b)}else f[b]=e[b]},this);return f};return c}();
g.createQueryParamsHelper=function(c){return new k(c)};Object.defineProperty(g,"__esModule",{value:!0})});