// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/parsers/infographic/AttachmentsInfographicParser",["../../../ConversionUtil"],function(b){return{portalToEditor:function(a,c){return{type:a.attributes.type,useCircularMask:a.attributes.useCircularMask,alwaysShowCaptions:a.attributes.alwaysShowCaptions,scaleToCover:a.attributes.scaleToCover,defaultImageIndex:Number(a.attributes.defaultImageIndex)||0,style:{width:b.ptToPx(a.attributes.width),height:b.ptToPx(a.attributes.height),
padding:a.attributes.padding?b.ptToPx(a.attributes.padding):void 0,backgroundColor:a.attributes.backgroundColor}}}}});