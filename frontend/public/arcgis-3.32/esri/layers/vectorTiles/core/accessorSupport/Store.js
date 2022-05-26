// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/accessorSupport/Store",["require","exports","./PropertyOrigin"],function(c,d,e){Object.defineProperty(d,"__esModule",{value:!0});c=function(){function a(){this._values={}}a.prototype.get=function(b){return this._values[b]};a.prototype.originOf=function(b){return e.OriginId.USER};a.prototype.keys=function(){return Object.keys(this._values)};a.prototype.set=function(b,a){this._values[b]=a};a.prototype.clear=function(a){delete this._values[a]};a.prototype.has=function(a){return a in
this._values};return a}();d.default=c});