// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/reportContainerStack/utils/ReportQueryUtil",["esri/dijit/geoenrichment/utils/DomUtil"],function(f){var c={getPanelInfoByNode:function(d,e){var b={panelIndex:-1,panelScale:void 0};if(d.isReportContainerStackAll)return d.getInnerContainers().some(function(a){a=c.getPanelInfoByNode(a,e);if(-1!==a.panelIndex)return b=a,!0}),b;d.infographicPage.getSections().some(function(a,c){if(f.isChildOf(e,a.domNode))return b.panelIndex=c,b.panelScale=d.infographicPage.getPanelScaleAt(c),
!0});return b}};return c});