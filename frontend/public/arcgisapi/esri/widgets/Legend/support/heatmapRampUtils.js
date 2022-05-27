// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../renderers/support/HeatmapColorStop"],function(e,g){e.getHeatmapRampStops=function(a){a=a.colorStops;let c=a.length-1;if(a&&a[0]){const b=a[c];b&&1!==b.ratio&&(a=a.slice(0),a.push(new g({ratio:1,color:b.color})),c++)}return a.map((b,f)=>{let d="";0===f?d="low":f===c&&(d="high");return{color:b.color,label:d,ratio:b.ratio}}).reverse()};Object.defineProperty(e,"__esModule",{value:!0})});