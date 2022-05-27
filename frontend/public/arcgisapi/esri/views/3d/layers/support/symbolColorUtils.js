// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/maybe","../../../../core/mathUtils"],function(e,g,d){e.encodeSymbolColor=function(b,c,a){if(g.isNone(b)||2===c)a[0]=255,a[1]=255,a[2]=255,a[3]=255;else{var f=d.clamp(Math.round(85*b[3]),0,85);c=0===f||4===c?0:3===c?85:170;a[0]=d.clamp(Math.round(255*b[0]),0,255);a[1]=d.clamp(Math.round(255*b[1]),0,255);a[2]=d.clamp(Math.round(255*b[2]),0,255);a[3]=f+c}};e.parseColorMixMode=function(b){switch(b){case "multiply":return 1;case "ignore":return 2;case "replace":return 3;
case "tint":return 4;default:return 1}};Object.defineProperty(e,"__esModule",{value:!0})});