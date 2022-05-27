// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../geometry/support/jsonUtils","../../request","../utils","./utils"],function(c,d,h,k,l){c.difference=async function(b,a,e,f){const g=a[0].spatialReference;b=k.parseUrl(b);a={query:{...b.query,f:"json",sr:JSON.stringify(g.toJSON()),geometries:JSON.stringify(l.encodeGeometries(a)),geometry:JSON.stringify({geometryType:d.getJsonType(e),geometry:e.toJSON()})}};f&&(a={...f,...a});return h(b.path+"/difference",a).then(({data:m})=>(m.geometries||[]).map(n=>d.fromJSON(n).set({spatialReference:g})))};
Object.defineProperty(c,"__esModule",{value:!0})});