// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../request","../utils","../../tasks/operations/lengths","../../tasks/support/LengthsParameters"],function(d,f,e,g,h){d.lengths=async function(b,a,c){a=h.from(a);a=g.lengthsToRESTParameters(a);b=e.parseUrl(b);c=e.asValidOptions({...b.query,f:"json",...a},c);return f(b.path+"/lengths",c).then(({data:k})=>k)};Object.defineProperty(d,"__esModule",{value:!0})});