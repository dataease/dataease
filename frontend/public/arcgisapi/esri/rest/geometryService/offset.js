// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../geometry/support/jsonUtils ../../request ../utils ../../tasks/operations/offset ../../tasks/support/OffsetParameters".split(" "),function(c,e,f,d,g,h){c.offset=async function(b,a,k){a=h.from(a);const l=g.offsetToRESTParameters(a);b=d.parseUrl(b);const m=a.geometries[0].spatialReference;a=d.asValidOptions({...b.query,f:"json",...l},k);return f(b.path+"/offset",a).then(({data:n})=>(n.geometries||[]).map(p=>e.fromJSON(p).set({spatialReference:m})))};Object.defineProperty(c,"__esModule",
{value:!0})});