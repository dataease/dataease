// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){const f={width:600,height:400};e.getOptimalThumbnailSize=function(c,d){d=d||f;let {width:a,height:b}=d;d=a/b;1.5>d?b=a/1.5:1.5<d&&(a=1.5*b);a>c.width&&(b*=c.width/a,a=c.width);b>c.height&&(a*=c.height/b,b=c.height);return{width:Math.round(a),height:Math.round(b)}};Object.defineProperty(e,"__esModule",{value:!0})});