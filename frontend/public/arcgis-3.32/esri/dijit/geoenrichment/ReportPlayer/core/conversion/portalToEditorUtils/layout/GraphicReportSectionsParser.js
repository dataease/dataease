// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/layout/GraphicReportSectionsParser",["esri/dijit/geoenrichment/utils/JsonXmlConverter"],function(a){return{parseSectionsGraphic:function(c,b){a.queryJson(c,"table",!0).forEach(function(a){try{b.templateJson.sectionsTables.push(b.parsers.getParser("section").parseTable(a,b,{fixTableWidthsForPage:!0}))}catch(d){console.log(d)}})}}});