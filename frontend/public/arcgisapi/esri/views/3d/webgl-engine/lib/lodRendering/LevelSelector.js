// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../Camera"],function(e,h){let k=function(){function f(a,b){this.thresholdScale=1;this._camera=new h;this._worldSpaceRadius=a;this._thresholds=b.map(c=>c)}var g=f.prototype;g.updateCamera=function(a){this._camera.copyFrom(a)};g.selectLevel=function(a,b){a=this._camera.computeScreenPixelSizeAt(a);b=this._worldSpaceRadius*b/a;a=this._thresholds;let c=-1;for(let d=0;d<a.length;++d)b>=a[d]*this.thresholdScale&&(c=d);return c};return f}();e.LevelSelector=k;Object.defineProperty(e,"__esModule",
{value:!0})});