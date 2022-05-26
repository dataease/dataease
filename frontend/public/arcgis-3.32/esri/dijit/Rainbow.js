// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/Rainbow",["dojo/has","dojox/charting/themes/PlotKit/base","../kernel"],function(c,b){var a=b.base.clone();a.chart.fill=a.plotarea.fill="#e7eef6";a.colors="#284B70 #702828 #5F7143 #F6BC0C #382C6C #50224F #1D7554 #4C4C4C #0271AE #706E41 #446A73 #0C3E69 #757575 #B7B7B7 #A3A3A3".split(" ");a.series.stroke.width=1;a.marker.stroke.width=1;c("extend-esri")&&(b.popup=a);return a});