// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../../core/mathUtils","../../../../chunks/vec3f64","../../../../chunks/vec3"],function(d,h,e,b){const f=e.create(),c=e.create();d.viewAngle=function(a,g,k){a.worldUpAtPosition(g,f);b.subtract(c,k,g);a=b.length(c);return 0===a?0:h.acosClamped(b.dot(c,f)/a)};Object.defineProperty(d,"__esModule",{value:!0})});