// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/2d/engine/webgl/brushes/WGLGeometryBrush",["require","exports","../../../../../core/tsSupport/extendsHelper","./WGLBrush","../util/iterator"],function(a,d,g,h,k){Object.defineProperty(d,"__esModule",{value:!0});a=function(b){function c(){return null!==b&&b.apply(this,arguments)||this}g(c,b);c.prototype.draw=function(b,e){var c=this;if(e.canDisplay){var a=this.getGeometryType(),d=e.getDisplayList(b.drawPhase),f=e.getGeometry(a);f&&k.forEachIter(d.ofType(a),function(a){return c.drawGeometry(b,
e,a,f)})}};return c}(h.default);d.default=a});