// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/maybe","../../../core/arrayUtils","../../../core/unitUtils"],function(d,b,e,g){function f(a){a=b.applySome(a,h=>h.tileInfo);if(b.isNone(a))return null;const c=e.last(a.lods);return b.isNone(c)?null:c.resolution*g.getMetersPerUnitForSR(a.spatialReference)}function k(a){return"tileInfo"in a?f(a):null}d.getGroundMinDemResolution=function(a){var c;if(b.isNone(a))return null;a=a.layers.items.map(k).filter(b.isSome);return null!=(c=e.min(a))?c:null};d.getQuerySourceMinDemResolution=
f;Object.defineProperty(d,"__esModule",{value:!0})});