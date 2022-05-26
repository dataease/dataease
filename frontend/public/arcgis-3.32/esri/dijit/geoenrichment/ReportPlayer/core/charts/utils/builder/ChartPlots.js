// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/utils/builder/ChartPlots",[],function(){var a={GRID:"grid",PRIMARY:"default",SECONDARY:"secondary",getWorkingPlots:function(c){var b=[];c.getPlot(a.PRIMARY)&&b.push(a.PRIMARY);c.getPlot(a.SECONDARY)&&b.push(a.SECONDARY);return b}};return a});