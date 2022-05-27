// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/maybe","../../core/urlUtils","../../chunks/persistableUrlUtils"],function(d,m,f,p){function h(a){a=f.urlToObject(a);a=a.path.match(n)||a.path.match(q);if(!a)return null;const [,b,c,e,g]=a;a=c.indexOf("/");return{title:k(-1!==a?c.slice(a+1):c),serverType:e,sublayer:null!=g&&""!==g?parseInt(g,10):null,url:{path:b}}}function k(a){a=a.replace(/\s*[/_]+\s*/g," ");return a[0].toUpperCase()+a.slice(1)}const l="MapServer ImageServer FeatureServer SceneServer StreamServer VectorTileServer".split(" "),
n=new RegExp(`^((?:https?:)?\\/\\/\\S+?\\/rest\\/services\\/(.+?)\\/(${l.join("|")}))(?:\\/(?:layers\\/)?(\\d+))?`,"i"),q=new RegExp(`^((?:https?:)?\\/\\/\\S+?\\/([^\\/\\n]+)\\/(${l.join("|")}))(?:\\/(?:layers\\/)?(\\d+))?`,"i");d.cleanTitle=k;d.isArcGISUrl=function(a){return!!n.test(a)};d.isHostedAgolService=function(a){if(!a)return!1;a=a.toLowerCase();const b=-1!==a.indexOf(".arcgis.com/");a=-1!==a.indexOf("//services")||-1!==a.indexOf("//tiles")||-1!==a.indexOf("//features");return b&&a};d.isHostedSecuredProxyService=
function(a,b){return b&&a&&-1!==a.toLowerCase().indexOf(b.toLowerCase())};d.isServerOrServicesAGOLUrl=function(a){if(!a)return!1;a=(new f.Url(f.makeAbsolute(a))).authority.toLowerCase();return"server.arcgisonline.com"===a||"services.arcgisonline.com"===a};d.isWmsServer=function(a){if(!a)return!1;var b=a.toLowerCase();a=-1!==b.indexOf("/services/");const c=-1!==b.indexOf("/mapserver/wmsserver"),e=-1!==b.indexOf("/imageserver/wmsserver");b=-1!==b.indexOf("/wmsserver");return a&&(c||e||b)};d.parse=h;
d.sanitizeUrl=function(a,b){return a?f.removeTrailingSlash(f.removeQueryParameters(a,b)):a};d.sanitizeUrlWithLayerId=function(a,b,c){if(!b)return{url:b};b=f.removeQueryParameters(b,c);c=f.urlToObject(b);c=h(c.path);let e;m.isSome(c)&&(null!=c.sublayer&&null==a.layerId&&(e=c.sublayer),b=c.url.path);return{url:f.removeTrailingSlash(b),layerId:e}};d.serverTypes=l;d.titleFromUrlAndName=function(a,b){const c=[];a&&(a=h(a),m.isSome(a)&&a.title&&c.push(a.title));b&&(b=k(b),c.push(b));if(2===c.length){if(-1!==
c[0].toLowerCase().indexOf(c[1].toLowerCase()))return c[0];if(-1!==c[1].toLowerCase().indexOf(c[0].toLowerCase()))return c[1]}return c.join(" - ")};d.writeUrlWithLayerId=function(a,b,c,e,g){p.write(b,e,"url",g);e.url&&null!=a.layerId&&(e.url=f.join(e.url,c,a.layerId.toString()))};Object.defineProperty(d,"__esModule",{value:!0})});