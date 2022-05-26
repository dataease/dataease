// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/dataProvider/supportClasses/tasks/EnrichAreasTask","dojo/_base/declare dojo/_base/lang esri/dijit/geoenrichment/when esri/dijit/geoenrichment/promise/all esri/kernel esri/SpatialReference esri/tasks/FeatureSet esri/geometry/Point ../GEUtil ../areas/AnalysisAreaUtil ../attachments/AttributesUtil ../../../core/supportClasses/map/Projector ../../../countryConfig esri/dijit/geoenrichment/utils/CoordinateUtil".split(" "),function(u,h,f,l,p,m,v,q,w,r,x,y,z,
n){function t(a){return(a=a&&p.id.findCredential(a)||p.id.credentials[0])&&a.token}return u(null,{enrichAreas:function(a){var b={};return f(this._analysisAreasToStudyAreas(a.analysisAreas,a.countryID,a.comparisonLevels),function(c){b.studyAreas=c;a.report?b.analysisVariables=[{itemid:a.report.reportID,url:a.report.portalUrl,token:t(a.portalUrl),outFields:["*"]}]:a.fields&&(b.analysisVariables=a.fields);return this._doRunTask(b,a,"enrich")}.bind(this)).then(this._handleFeatureSetsRequest.bind(this))},
createReport:function(a){var b={f:"bin",format:"xml",reportfields:{}};b.report={itemid:a.report.reportID,url:a.report.portalUrl,token:t(a.portalUrl)};return f(this._analysisAreasToStudyAreas(a.analysisAreas,a.countryID,null,a.getAttributes),function(c){b.studyAreas=c;return this._doRunTask(b,a,"createReport")}.bind(this))},_analysisAreasToStudyAreas:function(a,b,c,k){var g=this;c=c&&c.map(function(a){return{layer:a}});return l(a.map(function(a,e){var d;d=a.geographies&&a.geographies.length?g._studyAreaFromGeographies(a.geographies,
b,!0):{attributes:h.mixin({},a.feature.attributes),geometry:a.feature.geometry.toJson()};c&&(d.comparisonLevels=c);return l([g._getStorePointForArea(a),k&&k(a)]).then(function(a){var b=a[0];a=a[1];d.attributes=d.attributes||{};b&&(d.attributes.STORE_LAT=d.attributes.STORE_LAT||b.STORE_LAT,d.attributes.STORE_LONG=d.attributes.STORE_LONG||b.STORE_LONG);d.attributes.STORE_ID=e+"";a&&a.forEach(function(a){d.attributes[a.name]=x.formatAttributeValueForStudyArea(a)});return d})}))},_getStorePointForArea:function(a){a=
a.location&&a.location.geometry||a.buffer&&a.buffer.point;if(a=a instanceof q?a:new q(a)){var b=r.geometryToLatLong(a);return b?b:f(y.projectGeometries(a,new m(n.WGS_84_WKID)),function(a){return r.geometryToLatLong(a)})}},createFeatureForGeographies:function(a,b){return this._createFeaturesForGeographies(a,b,!0).then(function(a){return a[0]})},createFeaturesForGeographies:function(a,b){return this._createFeaturesForGeographies(a,b,!1)},_createFeaturesForGeographies:function(a,b,c){a={returnGeometry:!0,
outSR:b.outSR||new m(n.WEB_MERCATOR_WKID),studyAreas:c?[this._studyAreaFromGeographies(a,b.countryID,!0,b.generalizationLevel)]:a.map(function(a){return this._studyAreaFromGeographies([a],b.countryID,!1,b.generalizationLevel)},this),dataCollections:["GlobalIntersect"]};return this._doRunTask(a,b,"enrich").then(this._handleFeatureSetsRequest.bind(this))},_studyAreaFromGeographies:function(a,b,c,k){var g={sourceCountry:b,layer:null,ids:null},d=null,e=[],f;a.forEach(function(a){if(!a||!a.id)throw Error("Wrong geography.");
var b=a.levelId;if(b)if(!d)d=b;else if(c&&d!==b)throw Error("Geographies have different level IDs.");e.push(a.id);a.sourceCountry&&(g.sourceCountry=a.sourceCountry);a.hierarchy&&(g.hierarchy=a.hierarchy);a.attributes&&(f=h.mixin(f||{},a.attributes))});g.layer=d;g.ids=c?[e.join(",")]:e;g.attributes=f;g.generalizationlevel=k;return g},createFeaturesForBuffer:function(a,b){b=b||{};var c={bufferUnits:a.units,bufferRadii:a.radii||[a.radius],areaType:a.areaType||"RingBuffer"};"NetworkServiceArea"===c.areaType&&
(c.travelMode=a.travelMode,c.networkOptions=a.networkOptions);a=a.point||a.polyline;c={returnGeometry:!0,outSR:b.outSR||a.spatialReference||new m(n.WEB_MERCATOR_WKID),dataCollections:["GlobalIntersect"],studyAreasOptions:c,studyAreas:[{geometry:a.toJson?a.toJson():a}]};return this._doRunTask(c,b,"enrich").then(this._handleFeatureSetsRequest.bind(this))},createFeaturesForBuffers:function(a,b){var c={},f=[];a.forEach(function(a){var b;b=a.point||a.polyline;b=JSON.stringify(b.toJson?b.toJson():b)+";"+
a.units+";"+a.areaType+";"+a.travelMode+";"+JSON.stringify(a.networkOptions);var e=c[b];e||(e=c[b]=h.clone(a),delete e.radius,e.radii=[],f.push(e));a.radii?e.radii=e.radii.concat(a.radii):e.radii.push(a.radius)});return l(f.map(function(a){return this.createFeaturesForBuffer(a,b)},this)).then(function(a){var b=[];a.forEach(function(a){b=b.concat(a)});return b})},_doRunTask:function(a,b,c){a=h.mixin({useData:{sourceCountry:b.countryID,hierarchy:b.hierarchy||z.getHierarchyID()},forStorage:!1},a);return w[c](a)},
_handleFeatureSetsRequest:function(a){return(new v(a[0])).features}})});