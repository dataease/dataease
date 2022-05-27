// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../core/accessorSupport/ensureType ../../geometry/support/jsonUtils ../../request ../../tasks/support/ProjectParameters ../utils ./utils".split(" "),function(c,e,f,g,h,d,k){const l=e.ensureType(h);c.project=async function(b,a,m){a=l(a);b=d.parseUrl(b);const n={...b.query,f:"json",...a.toJSON()},p=a.outSpatialReference,q=f.getJsonType(a.geometries[0]);a=d.asValidOptions(n,m);return g(b.path+"/project",a).then(({data:{geometries:r}})=>k.decodeGeometries(r,q,p))};Object.defineProperty(c,
"__esModule",{value:!0})});