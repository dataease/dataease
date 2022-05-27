// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){c.hitTestSelectSameDistance=async function(a,b){if("2d"===a.type)return a.hitTest(b);a=await a.hitTest(b);const d=a.results[0];b=a.results.findIndex(e=>e.distance!==d.distance);-1!==b&&(a.results=a.results.slice(0,b));return a};Object.defineProperty(c,"__esModule",{value:!0})});