// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/grid/_ResizableColumnsSupport",["dojo/_base/declare"],function(a){return a(null,{_populateFieldForResizableColumns:function(b,a,d){var c=this;0===a.index&&(b.onManualWidthChange=function(){c._setCellWidth(b,b.getWidth());c.onColumnWidthChanged()})},onColumnWidthChanged:function(){}})});