// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../geometry/Polyline ../../geometry ../../request ../utils ../../tasks/operations/trimExtend ../../tasks/support/TrimExtendParameters".split(" "),function(c,e,q,f,d,g,h){c.trimExtend=async function(b,a,k){a=h.from(a);const l=g.trimExtendToRESTParameters(a);b=d.parseUrl(b);const m=a.sr;a=d.asValidOptions({...b.query,f:"json",...l},k);return f(b.path+"/trimExtend",a).then(({data:n})=>(n.geometries||[]).map(({paths:p})=>new e({spatialReference:m,paths:p})))};Object.defineProperty(c,
"__esModule",{value:!0})});