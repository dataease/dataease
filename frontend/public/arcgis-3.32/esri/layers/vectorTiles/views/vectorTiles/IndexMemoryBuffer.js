// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/vectorTiles/IndexMemoryBuffer",["require","exports","../../core/tsSupport/extendsHelper","../../core/tsSupport/decorateHelper","./MemoryBuffer"],function(e,a,f,g,c){Object.defineProperty(a,"__esModule",{value:!0});e=function(d){function b(){return d.call(this,12)||this}f(b,d);b.prototype.add=function(b,d,c){var a=this.array;a.push(b);a.push(d);a.push(c)};return b}(c);a.TriangleIndexBuffer=e;c=function(a){function b(){return a.call(this,4)||this}f(b,a);b.prototype.add=
function(a){this.array.push(a)};return b}(c);a.PointElementMemoryBuffer=c});