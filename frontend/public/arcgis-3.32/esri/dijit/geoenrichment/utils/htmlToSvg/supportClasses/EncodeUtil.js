// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/utils/htmlToSvg/supportClasses/EncodeUtil",["esri/dijit/geoenrichment/utils/MaskUtil"],function(a){return{encodeXML:function(b){return a.maskWithUnicodeXML(String(b).replace(/&nbsp;/g,"\x26#160;"))}}});