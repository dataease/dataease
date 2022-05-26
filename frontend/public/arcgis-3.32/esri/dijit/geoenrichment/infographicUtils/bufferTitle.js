// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/infographicUtils/bufferTitle","dojo/string esri/tasks/geoenrichment/DriveBuffer esri/tasks/geoenrichment/IntersectingGeographies esri/tasks/geoenrichment/DriveUnits dojo/i18n!esri/nls/jsapi esri/extend".split(" "),function(c,f,g,d,b,h){function e(e,a){switch(e){case "polyline":return a instanceof f?c.substitute(b.lineBuffer[d.MILES],{radius:"1"}):c.substitute(b.lineBuffer[a&&a.units||d.MILES],{radius:(a&&a.radii[0]||1).toString()});case "polygon":return b.polygon;
default:return a instanceof f?c.substitute(b.pointDriveTime[a&&a.units||d.MILES],{radius:(a&&a.radii[0]||1).toString()}):a instanceof g?c.substitute(b.stdGeo,{level:a.geographyLevels[0].layerID}):c.substitute(b.pointRing[a&&a.units||d.MILES],{radius:(a&&a.radii[0]||1).toString()})}}b=b.geoenrichment.dijit.bufferTitle;h("esri.dijit.geoenrichment.bufferTitle",e);return e});