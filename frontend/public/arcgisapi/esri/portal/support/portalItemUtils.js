// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){function d(a,b){return a.typeKeywords?-1<a.typeKeywords.indexOf(b):!1}c.addTypeKeyword=function(a,b){if(!d(a,b)){const e=a.typeKeywords;e?e.push(b):a.typeKeywords=[b]}};c.hasTypeKeyword=d;c.removeTypeKeyword=function(a,b){if(a=a.typeKeywords)b=a.indexOf(b),-1<b&&a.splice(b,1)};Object.defineProperty(c,"__esModule",{value:!0})});