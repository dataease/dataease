// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../layers/graphics/dehydratedFeatures"],function(c,g){function f(b){return"array"in b}c.SamplePosition=function(b,a=0){this.array=b;this.offset=a};c.getElevationAtPoint=function(b,a,d="ground"){if(g.isDehydratedPoint(a))return b.getElevation(a.x,a.y,a.z||0,a.spatialReference,d);if(f(a)){let e=a.offset;return b.getElevation(a.array[e++],a.array[e++],a.array[e]||0,b.spatialReference,d)}return b.getElevation(a[0],a[1],a[2]||0,b.spatialReference,d)};c.isSamplePosition=f;Object.defineProperty(c,
"__esModule",{value:!0})});