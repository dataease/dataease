// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../geometry/support/jsonUtils","../../request","../utils"],function(d,f,g,e){d.densify=async function(a,b,c){const h=b.geometries[0].spatialReference;a=e.parseUrl(a);b={...a.query,f:"json",...b.toJSON()};c=e.asValidOptions(b,c);return g(a.path+"/densify",c).then(({data:k})=>(k.geometries||[]).map(l=>f.fromJSON(l).set({spatialReference:h})))};Object.defineProperty(d,"__esModule",{value:!0})});