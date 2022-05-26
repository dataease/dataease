// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/ReportPlayer/dataProvider/supportClasses/tasks/parsers/AttrUtil",[],function(){var a={},b="ID OBJECTID AREA_ID STORE_ID HasData aggregationMethod sourceCountry radiusIndex".split(" ");a.cleanUpAttrs=function(a){a&&b.forEach(function(b){delete a[b]})};return a});