// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../lang"],function(e,g){let k=function(){function d(){this._values=new Map}var b=d.prototype;b.clone=function(a){const c=new d;this._values.forEach((h,f)=>{a&&a.has(f)||c.set(f,g.clone(h))});return c};b.get=function(a){return this._values.get(a)};b.originOf=function(){return 6};b.keys=function(){return[...this._values.keys()]};b.set=function(a,c){this._values.set(a,c)};b.delete=function(a){this._values.delete(a)};b.has=function(a){return this._values.has(a)};b.forEach=function(a){this._values.forEach(a)};
return d}();e.Store=k;Object.defineProperty(e,"__esModule",{value:!0})});