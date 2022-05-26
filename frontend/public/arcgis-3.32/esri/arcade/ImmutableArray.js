// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/arcade/ImmutableArray",["require","exports"],function(c,d){return function(){function a(b){void 0===b&&(b=[]);this._elements=b}a.prototype.length=function(){return this._elements.length};a.prototype.get=function(b){return this._elements[b]};a.prototype.toArray=function(){for(var b=[],a=0;a<this.length();a++)b.push(this.get(a));return b};return a}()});