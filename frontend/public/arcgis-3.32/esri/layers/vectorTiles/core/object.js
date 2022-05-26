// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/core/object",["require","exports"],function(g,a){function f(d,e,b){for(var c=0;c<d.length;c++){var a=d[c];if(!(a in b))if(e)b[a]={};else return;b=b[a]}return b}Object.defineProperty(a,"__esModule",{value:!0});a.getDeepValue=function(d,a){return f(d.split("."),!1,a)};a.setDeepValue=function(a,e,b){var c=a.split(".");a=c.pop();(b=f(c,!0,b))&&a&&(b[a]=e)}});