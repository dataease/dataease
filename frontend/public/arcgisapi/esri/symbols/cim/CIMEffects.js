// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/lang"],function(b,d){let e=function(){function a(c){this._geometry=c}a.prototype.next=function(){const c=this._geometry;this._geometry=null;return c};return a}();b.SimpleGeometryCursor=e;b.clone=function(a){return d.clone(a)};Object.defineProperty(b,"__esModule",{value:!0})});