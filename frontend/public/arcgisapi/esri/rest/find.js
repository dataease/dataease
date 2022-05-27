// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../request ./utils ../tasks/operations/find ../tasks/support/FindParameters ../tasks/support/FindResult".split(" "),function(e,f,d,g,h,k){function l(a){a=a.data;a.results=a.results||[];const b={results:[]};b.results=a.results.map(c=>k.fromJSON(c));return b}e.find=async function(a,b,c){b=h.from(b);b=g.findToFindRESTParameters(b);a=d.parseUrl(a);a.path+="/find";b=d.encode({...a.query,f:"json",...b});c=d.asValidOptions(b,c);return f(a.path,c).then(l)};Object.defineProperty(e,"__esModule",
{value:!0})});