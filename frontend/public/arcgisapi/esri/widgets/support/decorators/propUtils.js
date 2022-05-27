// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){c.normalizePropNames=function(d,b){return d.map(a=>{a=0===a.indexOf(b)?a:`${b}.${a}`;return a})};c.splitProps=function(d){return d.split(",").map(b=>b.trim())};Object.defineProperty(c,"__esModule",{value:!0})});