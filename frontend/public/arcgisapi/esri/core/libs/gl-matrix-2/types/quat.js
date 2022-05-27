// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(b){function c(a){return a instanceof Float32Array&&4<=a.length}function d(a){return Array.isArray(a)&&4<=a.length}b.isQuat=function(a){return c(a)||d(a)};b.isQuatf32=c;b.isQuatf64=d;Object.defineProperty(b,"__esModule",{value:!0})});