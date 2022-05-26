// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/charts/tooltips/_GaugeChartTooltipBuilder",["dojo/dom-construct","dojo/string","./_BuilderUtil","dojo/i18n!esri/nls/jsapi"],function(e,f,c,d){d=d.geoenrichment.dijit.ReportPlayer.ChartTooltip;return{buildGaugeChartTooltip:function(a,b){c.addTitle(b,a.label,a);b=e.create("div",{"class":"chartTooltip_row esriGERowHigh"},b);c.addRowOffset(b);a.isUnavailableData?c.addLabel(d.unavailableData,b):a.hasNegativeValues?c.addLabel(a.valueLabel,b):c.addLabel(f.substitute(d.gaugeChartTooltip_label,
{value:a.valueLabel,total:a.sumValueLabel}),b)}}});