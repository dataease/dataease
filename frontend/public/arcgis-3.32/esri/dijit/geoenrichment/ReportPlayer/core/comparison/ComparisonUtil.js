// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/comparison/ComparisonUtil",["esri/dijit/geoenrichment/utils/ObjectUtil","dojo/i18n!esri/nls/jsapi"],function(d,b){b=b.geoenrichment.dijit.ReportPlayer.ReportPlayer;return{valueFormatFunction:function(a,c){return c&&c.isMissing?b.missingVariable:void 0===a||"string"===typeof a?a:d.formatNumber(a,{places:c.decimals,preserveTrailingZeroes:!0})}}});