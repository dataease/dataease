// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/unitUtils"],function(d,c){function f(b,a){a=c.getMetersPerUnitForSR(a);return b/(a*c.inchesPerMeter*96)}d.getExtentForScale=function(b,a){const e=b.extent;b=b.width;a=f(a,e.spatialReference);return e.clone().expand(a*b/e.width)};d.getResolutionForScale=f;d.getScale=function(b,a){a=a||b.extent;b=b.width;const e=c.getMetersPerUnitForSR(a&&a.spatialReference);return a&&b?a.width/b*e*c.inchesPerMeter*96:0};d.getScaleForResolution=function(b,a){a=c.getMetersPerUnitForSR(a);
return b*a*c.inchesPerMeter*96};Object.defineProperty(d,"__esModule",{value:!0})});