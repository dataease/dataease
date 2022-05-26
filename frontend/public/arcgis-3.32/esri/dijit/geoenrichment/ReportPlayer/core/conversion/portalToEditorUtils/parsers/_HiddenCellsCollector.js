// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/conversion/portalToEditorUtils/parsers/_HiddenCellsCollector",["./_TableRowFixer"],function(k){return{collectHiddenCells:function(a,h){var c={};a&&a.forEach(function(a,e){var d=0;k.fixTr(a,e,c,h);a.tags&&a.tags.forEach(function(b){for(;c[d+"_"+e];)d++;var a=Number(b.attributes&&b.attributes.colspan);b=Number(b.attributes&&b.attributes.rowspan);if(1<a||1<b){a=a||1;b=b||1;for(var f=d;f<d+a;f++)for(var g=e;g<e+b;g++)if(f!==d||g!==e)c[f+"_"+g]=!0}d++})});
return c},isHidden:function(a,h,c){return a[h+"_"+c]}}});