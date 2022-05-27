// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./propUtils"],function(c,d){c.vmEvent=function(b){return a=>{a.hasOwnProperty("_delegatedEventNames")||(a._delegatedEventNames=a._delegatedEventNames?a._delegatedEventNames.slice():[]);a=a._delegatedEventNames;const e=Array.isArray(b)?b:d.splitProps(b);a.push(...e)}};Object.defineProperty(c,"__esModule",{value:!0})});