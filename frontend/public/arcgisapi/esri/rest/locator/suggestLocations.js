// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../request","../utils","../../tasks/support/SuggestLocationsParameters"],function(e,f,c,g){function h(a){({data:a}=a);if(!a)return[];({suggestions:a}=a);return a||[]}e.suggestLocations=async function(a,b,d){a=c.parseUrl(a);b=g.from(b);b={...b.toJSON(),f:"json"};b=c.encode({...a.query,...b});d=c.asValidOptions(b,d);return f(`${a.path}/suggest`,d).then(h)};Object.defineProperty(e,"__esModule",{value:!0})});