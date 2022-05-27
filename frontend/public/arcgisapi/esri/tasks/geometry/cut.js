// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/urlUtils","../../geometry/support/jsonUtils","../../geometry","../../request"],function(d,g,e,p,h){d.cut=async function(b,a,k,l){b="string"===typeof b?g.urlToObject(b):b;const f=a[0].spatialReference;a={...l,query:{...b.query,f:"json",sr:JSON.stringify(f),target:JSON.stringify({geometryType:e.getJsonType(a[0]),geometries:a}),cutter:JSON.stringify(k)}};a=await h(b.path+"/cut",a);const {cutIndexes:m,geometries:n=[]}=a.data;return{cutIndexes:m,geometries:n.map(c=>{c=e.fromJSON(c);
c.spatialReference=f;return c})}};Object.defineProperty(d,"__esModule",{value:!0})});