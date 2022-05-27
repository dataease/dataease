// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../geometry/Polygon ../../geometry ../../request ../utils ./utils".split(" "),function(d,h,p,k,e,f){d.autoComplete=async function(a,b,l,c){const g=b[0].spatialReference;a=e.parseUrl(a);b={...a.query,f:"json",sr:JSON.stringify(g.toJSON()),polygons:JSON.stringify(f.encodeGeometries(b).geometries),polylines:JSON.stringify(f.encodeGeometries(l).geometries)};c=e.asValidOptions(b,c);return k(a.path+"/autoComplete",c).then(({data:m})=>(m.geometries||[]).map(({rings:n})=>new h({spatialReference:g,
rings:n})))};Object.defineProperty(d,"__esModule",{value:!0})});