// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/reportContainer/utils/ReportQueryUtil",[],function(){return{getParentReportContainer:function(a){if(a&&a.isReportContainer)return a;for(;a;){if(a.parentWidget&&a.parentWidget.isReportContainer)return a.parentWidget;a=a.parentWidget||a.parentGrid}return null}}});