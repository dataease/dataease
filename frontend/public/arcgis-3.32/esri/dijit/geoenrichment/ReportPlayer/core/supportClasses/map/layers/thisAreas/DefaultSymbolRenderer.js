// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/supportClasses/map/layers/thisAreas/DefaultSymbolRenderer",["dojo/_base/Color","esri/symbols/SimpleMarkerSymbol","esri/symbols/SimpleFillSymbol","esri/symbols/SimpleLineSymbol"],function(a,d,e,b){return{generateSMS:function(){return new d(d.STYLE_CIRCLE,20,new b(b.STYLE_SOLID,new a([255,255,255,.7]),2),new a([255,0,0,.7]))},generateSFS:function(){var c=new e;c.setOutline(new b(b.STYLE_SOLID,new a([255,0,0,1]),2));c.setColor(new a([100,100,100,.25]));
return c}}});