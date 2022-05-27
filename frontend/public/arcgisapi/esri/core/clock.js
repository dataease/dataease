// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./global"],function(a,b){function c(d){return{setTimeout:(e,f)=>{const g=d.setTimeout(e,f);return{remove:()=>d.clearTimeout(g)}}}}b=c(b);a.default=b;a.wrap=c;Object.defineProperty(a,"__esModule",{value:!0})});