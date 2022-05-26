// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/printing/PageSizeUtil",["dojo/i18n!esri/nls/jsapi"],function(a){a=a.geoenrichment.dijit.ReportPlayer.PaperSize;return{getSupportedPageSizes:function(c,d){var b=[{label:a.a3,value:"a3"},{label:a.a4,value:"a4"},{label:a.a5,value:"a5"},{label:a.b4,value:"b4"},{label:a.b5,value:"b5"},{label:a.ledger,value:"ledger"},{label:a.legal,value:"legal"},{label:a.letter,value:"letter"},{label:a.tabloid,value:"tabloid"}];c&&b.push({label:d||a.custom,value:"custom"});
return b}}});