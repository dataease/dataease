// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){let e=function(){function d(){this._names=new Map}var b=d.prototype;b.begin=function(a){this._names.has(a)||(this._names.set(a,!1),-1!==a.indexOf("Brush")&&this.record("Esri.FirstDraw"),performance.mark(`Esri.${a}.Start`))};b.end=function(a){this._names.has(a)&&!this._names.get(a)&&(this._names.set(a,!0),performance.mark(`Esri.${a}.End`))};b.record=function(a){this._names.has(a)||(this._names.set(a,!0),performance.mark(`Esri.${a}`))};return d}();c.Timeline=e;Object.defineProperty(c,
"__esModule",{value:!0})});