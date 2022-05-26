// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/dijit/geoenrichment/infographicUtils/formatVariable",["dojo/number"],function(c){return function(a,b){var d=a.decimals||0;switch(a.units){case "pct":return c.format(b/100,{places:d,type:"percent"});case "currency":return c.format(b,{places:d,type:"currency",symbol:a.symbol||"$"});default:return"esriFieldTypeDouble"==a.type?c.format(b,{places:d}):b}}});