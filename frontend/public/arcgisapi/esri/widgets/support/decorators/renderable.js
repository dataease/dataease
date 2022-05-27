// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./propUtils"],function(c,d){c.renderable=function(b){const e="string"===typeof b?d.splitProps(b):b;return function(a,f){a.hasOwnProperty("_renderableProps")||(a._renderableProps=a._renderableProps?a._renderableProps.slice():[]);a=a._renderableProps;e?a.push.apply(a,d.normalizePropNames(e,f)):a.push(f)}};Object.defineProperty(c,"__esModule",{value:!0})});