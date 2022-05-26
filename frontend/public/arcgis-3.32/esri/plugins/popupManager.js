// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/plugins/popupManager",["../PopupManager"],function(c){return{add:function(a,b){a.popupManager||(a.popupManager=new c(b),a.popupManager.setMap(a))},remove:function(a){var b=a.popupManager;b&&(b.unsetMap(),a.popupManager=void 0)}}});