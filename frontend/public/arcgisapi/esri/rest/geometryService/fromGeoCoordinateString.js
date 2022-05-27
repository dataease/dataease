// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../request","../utils","./units"],function(e,g,f,h){e.fromGeoCoordinateString=async function(c,a,d){const b={};b.sr=null!=a.sr&&"object"===typeof a.sr?a.sr.wkid||JSON.stringify(a.sr):a.sr;b.strings=JSON.stringify(a.strings);b.conversionType=h.conversionTypeKebabDict.toJSON(a.conversionType||"mgrs");b.conversionMode=a.conversionMode;c=f.parseUrl(c);d=f.asValidOptions({...c.query,f:"json",...b},d);return g(c.path+"/fromGeoCoordinateString",d).then(({data:k})=>k.coordinates)};Object.defineProperty(e,
"__esModule",{value:!0})});