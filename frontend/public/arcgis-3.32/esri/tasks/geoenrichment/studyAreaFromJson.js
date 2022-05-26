// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/tasks/geoenrichment/studyAreaFromJson",["./GeometryStudyArea","./AddressStudyArea","./StandardGeographyStudyArea","../../extend"],function(c,d,e,f){var b=function(a){if(a.geometry)return new c(a);if(a.address)return new d(a);if(a.layer)return new e(a)};f("esri.tasks.geoenrichment.studyAreaFromJson",b);return b});