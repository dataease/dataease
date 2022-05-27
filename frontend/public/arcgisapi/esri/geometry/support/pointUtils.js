// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){e.distance=function(a,b){const c=a.x-b.x,d=a.y-b.y;a=a.hasZ&&b.hasZ?a.z-b.z:0;return Math.sqrt(c*c+d*d+a*a)};e.squareDistance=function(a,b){const c=a.x-b.x,d=a.y-b.y;a=a.hasZ&&b.hasZ?a.z-b.z:0;return c*c+d*d+a*a};Object.defineProperty(e,"__esModule",{value:!0})});