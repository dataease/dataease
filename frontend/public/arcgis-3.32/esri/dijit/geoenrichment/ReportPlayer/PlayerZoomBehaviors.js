// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/PlayerZoomBehaviors",[],function(){var a={RESET:"reset",FIT_PAGE:"fitPage",FIT_PAGE_WIDTH:"fitPageWidth",FIT_PAGE_HEIGHT:"fitPageHeight",isSupported:function(b){for(var c in a)if(a[c]===b)return!0;return!1},toSupportedValue:function(b){return a.isSupported(b)?b:a.RESET}};return a});