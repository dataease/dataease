// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/metadata/map/MapTagsUtil",[],function(){return{parseColorTag:function(a){if(!a)return null;a=a.attributes;return[Number(a.Red)||0,Number(a.Green)||0,Number(a.Blue)||0,a.Transparency?255*(1-Number(a.Transparency)/100):255]}}});