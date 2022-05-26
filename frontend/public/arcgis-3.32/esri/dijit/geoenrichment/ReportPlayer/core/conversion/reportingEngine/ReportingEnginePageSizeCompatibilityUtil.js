// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/reportingEngine/ReportingEnginePageSizeCompatibilityUtil",["../../supportClasses/DocumentOptions"],function(c){var d={},e={a4:1,letter:1};d.getReportingEnginePageSize=function(a,b){return e[a]?a:(b=c.SIZE_TYPE_TO_DIM_HASH[a]&&c.SIZE_TYPE_TO_DIM_HASH[a][b])?c.combineCustomSizeString(b.w,b.h,"in"):a};return d});