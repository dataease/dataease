// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/metadata/map/MetadataToRendererParser",["esri/dijit/geoenrichment/utils/JsonXmlTypedConverter","esri/dijit/geoenrichment/utils/JsonXmlConverter"],function(b,c){return{parseRendererJson:function(a){return b.parseXml(c.parseJson(a))},parseLabelRendererJson:function(a){return{type:"uniqueValue",field1:"StdGeographyLevel",uniqueValueInfos:b.parseXml(c.parseJson(a)).map(function(a){return{value:a.where.match(/StdGeographyLevel='(.*?)'/)[1],
symbol:a.symbol}})}}}});