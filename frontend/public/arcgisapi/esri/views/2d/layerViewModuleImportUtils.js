// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["require","exports","../../core/maybe","../../core/Error"],function(d,k,l,n){function e(a){return Object.freeze({__proto__:null,"default":a})}function f(){return Promise.all([new Promise(function(a,b){d(["../webgl"],a,b)}),new Promise(function(a,b){d(["./mapViewDeps"],a,b)})])}const g=()=>f().then(()=>new Promise(function(a,b){d(["./layers/TileLayerView2D"],function(c){a(e(c))},b)})),h=()=>f().then(()=>new Promise(function(a,b){d(["./layers/FeatureLayerView2D"],function(c){a(e(c))},b)})),
m={"base-dynamic":()=>f().then(()=>new Promise(function(a,b){d(["./layers/BaseDynamicLayerView2D"],function(c){a(e(c))},b)})),"base-tile":g,"bing-maps":g,csv:h,"geo-rss":()=>f().then(()=>new Promise(function(a,b){d(["./layers/GeoRSSLayerView2D"],function(c){a(e(c))},b)})),feature:h,geojson:h,graphics:()=>f().then(()=>new Promise(function(a,b){d(["./layers/GraphicsLayerView2D"],function(c){a(e(c))},b)})),group:()=>f().then(()=>new Promise(function(a,b){d(["./layers/GroupLayerView2D"],function(c){a(e(c))},
b)})),imagery:()=>f().then(()=>new Promise(function(a,b){d(["./layers/ImageryLayerView2D"],function(c){a(e(c))},b)})),"imagery-tile":()=>f().then(()=>new Promise(function(a,b){d(["./layers/ImageryTileLayerView2D"],function(c){a(e(c))},b)})),kml:()=>f().then(()=>new Promise(function(a,b){d(["./layers/KMLLayerView2D"],function(c){a(e(c))},b)})),"map-image":()=>f().then(()=>new Promise(function(a,b){d(["./layers/MapImageLayerView2D"],function(c){a(e(c))},b)})),"map-notes":()=>f().then(()=>new Promise(function(a,
b){d(["./layers/MapNotesLayerView2D"],function(c){a(e(c))},b)})),"ogc-feature":h,"open-street-map":g,route:()=>f().then(()=>new Promise(function(a,b){d(["./layers/RouteLayerView2D"],function(c){a(e(c))},b)})),stream:()=>f().then(()=>new Promise(function(a,b){d(["./layers/StreamLayerView2D"],function(c){a(e(c))},b)})),tile:g,"vector-tile":()=>f().then(()=>new Promise(function(a,b){d(["./layers/VectorTileLayerView2D"],function(c){a(e(c))},b)})),wcs:()=>f().then(()=>new Promise(function(a,b){d(["./layers/ImageryTileLayerView2D"],
function(c){a(e(c))},b)})),"web-tile":g,wms:()=>f().then(()=>new Promise(function(a,b){d(["./layers/WMSLayerView2D"],function(c){a(e(c))},b)})),wmts:()=>f().then(()=>new Promise(function(a,b){d(["./layers/WMTSLayerView2D"],function(c){a(e(c))},b)})),"base-elevation":null,"building-scene":null,elevation:null,"integrated-mesh":null,"point-cloud":null,scene:null,unknown:null,unsupported:null};k.layerView2DImporter={hasLayerViewModule:a=>l.isSome(m[a.type]),importLayerView:a=>{var b=m[a.type];if(!l.isSome(b))throw a=
a.declaredClass?a.declaredClass.slice(a.declaredClass.lastIndexOf(".")+1):"Unknown",b=a.replace(/([a-z])([A-Z])/g,"$1-$2").toLowerCase(),a=new n(`${b}:view-not-supported`,`${a} is not supported in 2D`),a;return b(a)}};Object.defineProperty(k,"__esModule",{value:!0})});