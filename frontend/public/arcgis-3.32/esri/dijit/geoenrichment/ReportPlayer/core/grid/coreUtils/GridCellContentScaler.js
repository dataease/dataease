// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/grid/coreUtils/GridCellContentScaler",["./GridDataUtil"],function(f){return{fitContentInsideCell:function(b){var c=f.getFieldInfo(b);if(c&&b.content){var d=b.content,a=b.parentGrid,e=b[a.hasRealBorders?"getContentWidth":"getWidth"](),a=b[a.hasRealBorders?"getContentHeight":"getHeight"]();c.isReportSection?(d.setHeight(a,{resizeContentProportionally:!0}),d.setWidth(e,{resizeContentProportionally:!0})):c.isChart?d.resize(e,a):c.isInfographic?d.resize(e,
a):c.isMap?d.resize({w:e,h:a}):(c.isImage||c.isShape)&&d.resize({w:e,h:a},b.getFullStyle())}}}});