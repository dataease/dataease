// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../core/jsonMap"],function(a,b){const d=b.strict()({esriGeometryPoint:"point",esriGeometryMultipoint:"multipoint",esriGeometryPolyline:"polyline",esriGeometryPolygon:"polygon"});b=b.strict()({esriGeometryPoint:"point",esriGeometryMultipoint:"multipoint",esriGeometryPolyline:"polyline",esriGeometryPolygon:"polygon",esriGeometryEnvelope:"extent",mesh:"mesh"});a.featureGeometryTypeKebabDictionary=d;a.isFeatureGeometryType=function(c){return"point"===c||"multipoint"===c||"polyline"===
c||"polygon"===c};a.typeKebabDictionary=b;Object.defineProperty(a,"__esModule",{value:!0})});