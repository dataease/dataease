// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/parsers/AlignParser",["../../ConversionUtil"],function(c){return{parseAlign:function(a,b){b.horizontalAlign=a.align||"left";b.verticalAlign=c.getPropValuePtoE("valign",a.valign)||"top"}}});