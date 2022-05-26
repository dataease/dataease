// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/core/infographics/InfographicRenderer",["require","esri/dijit/geoenrichment/Deferred","dojo/dom-construct"],function(e,f,g){var d,c={isAsync:!0,loadModules:function(){var a=new f;e(["./InfographicContainer"],function(b){d=b;c.isAsync=!1;a.resolve()});return a.promise},createInfographicPage:function(a,b){var c=a.node?g.create("div",null,a.node):void 0;b=new (b||d)(a.creationParams,c);"function"===typeof a.placeFunc&&a.placeFunc(b);b.updateInfographic(a.json);
return b}};return c});