// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../request","../utils"],function(d,g,e){d.distance=async function(a,b,c){a=e.parseUrl(a);b={...a.query,f:"json",...b.toJSON()};c=e.asValidOptions(b,c);return g(a.path+"/distance",c).then(({data:f})=>f&&f.distance)};Object.defineProperty(d,"__esModule",{value:!0})});