// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/toolbar/Select",["dojo/_base/declare","dojo/dom-class","esri/dijit/geoenrichment/OnDemandSelect","../PlayerThemes"],function(a,b,c,d){return a(c,{"class":"esriGEOnDemandSelectNoBackground",listClass:"esriGEOnDemandSelectVeryTallList esriGEOnDemandSelectSpacedOut",allowRepetitiveSelection:!1,setTheme:function(a){b[a===d.DARK?"add":"remove"](this.domNode,"esriGEOnDemandSelectWhite")}})});