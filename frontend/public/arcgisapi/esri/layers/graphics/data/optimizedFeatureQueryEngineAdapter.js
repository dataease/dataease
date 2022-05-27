// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../OptimizedFeature","../OptimizedGeometry","../centroid"],function(c,e,f,g){const d={getObjectId(a){return a.objectId},getAttributes(a){return a.attributes},getAttribute(a,b){return a.attributes[b]},cloneWithGeometry(a,b){return new e(b,a.attributes,null,a.objectId)},getGeometry(a){return a.geometry},getCentroid(a,b){a.centroid||(a.centroid=g.getCentroidOptimizedGeometry(new f,a.geometry,b.hasZ,b.hasM));return a.centroid}};c.default=d;c.optimizedFeatureQueryEngineAdapter=d;Object.defineProperty(c,
"__esModule",{value:!0})});