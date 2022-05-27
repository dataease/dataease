// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../renderers/support/lengthUtils"],function(b,c){const d=function(){const a=Object.keys(c.meterIn);a.sort();return a}();b.getMetersPerUnit=function(a){return 1/(c.meterIn[a]||1)};b.supportedUnits=d;b.supportsUnit=function(a){return null!=c.meterIn[a]};Object.defineProperty(b,"__esModule",{value:!0})});