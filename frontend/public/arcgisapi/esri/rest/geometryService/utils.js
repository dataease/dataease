// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../geometry/support/jsonUtils"],function(c,e){c.decodeGeometries=function(a,d,f){const g=e.getGeometryType(d);return a.map(b=>{b=g.fromJSON(b);b.spatialReference=f;return b})};c.encodeGeometries=function(a){return{geometryType:e.getJsonType(a[0]),geometries:a.map(d=>d.toJSON())}};Object.defineProperty(c,"__esModule",{value:!0})});