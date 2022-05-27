// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../request","../utils","./units"],function(e,g,f,h){e.toGeoCoordinateString=async function(c,a,d){const b={};b.sr=null!=a.sr&&"object"===typeof a.sr?a.sr.wkid||JSON.stringify(a.sr):a.sr;b.coordinates=JSON.stringify(a.coordinates);b.conversionType=h.conversionTypeKebabDict.toJSON(a.conversionType||"mgrs");b.conversionMode=a.conversionMode;b.numOfDigits=a.numOfDigits;b.rounding=a.rounding;b.addSpaces=a.addSpaces;c=f.parseUrl(c);d=f.asValidOptions({...c.query,f:"json",...b},d);
return g(c.path+"/toGeoCoordinateString",d).then(({data:k})=>k.strings)};Object.defineProperty(e,"__esModule",{value:!0})});