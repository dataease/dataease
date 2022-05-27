// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./zscale"],function(c,d){c.applyFeatureSetZUnitScaling=function(b,e,a){if(a&&a.features&&a.hasZ&&(b=d.getGeometryZScaler(a.geometryType,e,b.outSpatialReference)))for(const f of a.features)b(f.geometry)};Object.defineProperty(c,"__esModule",{value:!0})});