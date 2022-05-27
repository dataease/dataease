// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../geometry/support/jsonUtils","../../geometry","../../request","../utils"],function(d,e,p,g,h){d.cut=async function(b,a,k,l){b=h.parseUrl(b);const f=a[0].spatialReference;a={...l,query:{...b.query,f:"json",sr:JSON.stringify(f),target:JSON.stringify({geometryType:e.getJsonType(a[0]),geometries:a}),cutter:JSON.stringify(k)}};a=await g(b.path+"/cut",a);const {cutIndexes:m,geometries:n=[]}=a.data;return{cutIndexes:m,geometries:n.map(c=>{c=e.fromJSON(c);c.spatialReference=f;return c})}};
Object.defineProperty(d,"__esModule",{value:!0})});