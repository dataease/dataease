// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/arcade/featureset/support/shared",["require","exports","../../polyfill/promiseUtils","../../../geometry/Extent","../../../layers/Field"],function(w,c,e,p,q){function f(a){var b=null;a.domain&&(b="range"===a.domain.type?{type:"range",range:[a.domain.minValue,a.domain.maxValue]}:{type:"codedValue",codedValues:a.domain.toJson().codedValues});return{name:a.name,type:a.type,alias:a.alias,length:a.length,editable:a.editable,nullable:a.nullable,domain:b}}function g(a){return a instanceof Date}
Object.defineProperty(c,"__esModule",{value:!0});(function(a){a[a.Standardised=0]="Standardised";a[a.StandardisedNoInterval=1]="StandardisedNoInterval";a[a.SqlServer=2]="SqlServer";a[a.Oracle=3]="Oracle";a[a.Postgres=4]="Postgres";a[a.PGDB=5]="PGDB";a[a.FILEGDB=6]="FILEGDB";a[a.NotEvaluated=7]="NotEvaluated"})(c.FeatureServiceDatabaseType||(c.FeatureServiceDatabaseType={}));c.cloneField=function(a){return new q(f(a))};c.esriFieldToJson=f;(function(a){a[a.InFeatureSet=0]="InFeatureSet";a[a.NotInFeatureSet=
1]="NotInFeatureSet";a[a.Unknown=2]="Unknown"})(c.IdState||(c.IdState={}));c.isString=function(a){return"string"===typeof a||a instanceof String};c.isBoolean=function(a){return"boolean"===typeof a};c.isNumber=function(a){return"number"===typeof a};c.isArray=function(a){return a instanceof Array};c.isDate=g;c.equalityTest=function(a,b){return a===b?!0:g(a)&&g(b)?a.getTime()===b.getTime():!1};c.cloneAttributes=function(a){var b={},c;for(c in a)b[c]=a[c];return b};c.convertSquareUnitsToCode=function(a){if(void 0===
a)return null;if("number"===typeof a)return a;switch(a.toLowerCase()){case "meters":case "meter":return 109404;case "miles":case "mile":return 109413;case "kilometers":case "kilometer":case "km":return 109414}return null};c.shapeExtent=function(a){if(null===a)return null;switch(a.type){case "polygon":case "multipoint":case "polyline":return a.getExtent();case "point":return new p({xmin:a.x,ymin:a.y,xmax:a.x,ymax:a.y,spatialReference:a.spatialReference});case "extent":return a}return null};c.convertLinearUnitsToCode=
function(a){if(void 0===a)return null;if("number"===typeof a||"number"===typeof a)return a;switch(a.toLowerCase()){case "meters":case "meter":return 9001;case "miles":case "mile":return 9035;case "kilometers":case "kilometer":case "km":return 9036}return null};c.sameGeomType=function(a,b){return a===b||"point"===a&&"esriGeometryPoint"===b||"polyline"===a&&"esriGeometryPolyline"===b||"polygon"===a&&"esriGeometryPolygon"===b||"extent"===a&&"esriGeometryEnvelope"===b||"multipoint"===a&&"esriGeometryMultipoint"===
b||"point"===b&&"esriGeometryPoint"===a||"polyline"===b&&"esriGeometryPolyline"===a||"polygon"===b&&"esriGeometryPolygon"===a||"extent"===b&&"esriGeometryEnvelope"===a||"multipoint"===b&&"esriGeometryMultipoint"===a?!0:!1};c.defaultMaxRecords=1E3;c.errback=function(a){return function(b){a.reject(b)}};c.callback=function(a,b){return function(){try{a.apply(null,arguments)}catch(r){b.reject(r)}}};c.layerGeometryEsriConstants={point:"point",polygon:"polygon",polyline:"polyline",multipoint:"multipoint",
extent:"extent",esriGeometryPoint:"point",esriGeometryPolygon:"polygon",esriGeometryPolyline:"polyline",esriGeometryMultipoint:"multipoint",esriGeometryEnvelope:"extent",envelope:"extent"};c.toEsriGeometryType=function(a){switch(a){case "point":return"esriGeometryPoint";case "polygon":return"esriGeometryPolygon";case "multipoint":return"esriGeometryMultipoint";case "polyline":return"esriGeometryPolyline";default:return"esriGeometryPoint"}};c.reduceArrayWithPromises=function(a,b){return e.create(function(c,
t){var h=e.resolve(!0);a.reduce(function(a,c,u,k){return a.then(function(a){try{return b(a,c,u,k)}catch(v){return e.reject(v)}},function(a){return e.reject(a)})},h).then(c,t)})};c.extractServiceUrl=function(a){if(void 0===a)return"";a=a.replace(/\/featureserver\/[0-9]*/i,"/FeatureServer");a=a.replace(/\/mapserver\/[0-9]*/i,"/MapServer");return a=a.split("?")[0]};c.stableStringify=function(a,b){b||(b={});"function"===typeof b&&(b={cmp:b});var c="boolean"===typeof b.cycles?b.cycles:!1,e=b.cmp&&function(a){return function(b){return function(c,
k){return a({key:c,value:b[c]},{key:k,value:b[k]})}}}(b.cmp),h=[];return function l(a){a&&a.toJson&&"function"===typeof a.toJson&&(a=a.toJson());if(void 0!==a){if("number"===typeof a)return isFinite(a)?""+a:"null";if("object"!==typeof a)return JSON.stringify(a);var b,d;if(Array.isArray(a)){d="[";for(b=0;b<a.length;b++)b&&(d+=","),d+=l(a[b])||"null";return d+"]"}if(null===a)return"null";if(-1!==h.indexOf(a)){if(c)return JSON.stringify("__cycle__");throw new TypeError("Converting circular structure to JSON");
}var g=h.push(a)-1,f=Object.keys(a).sort(e&&e(a));d="";for(b=0;b<f.length;b++){var m=f[b],n=l(a[m]);n&&(d&&(d+=","),d+=JSON.stringify(m)+":"+n)}h.splice(g,1);return"{"+d+"}"}}(a)}});