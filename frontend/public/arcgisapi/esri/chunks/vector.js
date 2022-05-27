// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../core/mathUtils","./vec3f64","./vec3"],function(d,k,e,b){function l(a,c,g){c=b.dot(a,c)/b.dot(a,a);return b.scale(g,a,c)}function m(a,c){return b.dot(a,c)/b.length(a)}function n(a,c){a=b.dot(a,c)/(b.length(a)*b.length(c));return-k.acosClamped(a)}function p(a,c,g){b.normalize(f,a);b.normalize(h,c);a=b.dot(f,h);a=k.acosClamped(a);c=b.cross(f,f,h);return 0>b.dot(c,g)?2*Math.PI-a:a}const f=e.create(),h=e.create();e=Object.freeze({__proto__:null,projectPoint:l,projectPointSignedLength:m,
angle:n,angleAroundAxis:p});d.angle=n;d.angleAroundAxis=p;d.projectPoint=l;d.projectPointSignedLength=m;d.vectorModule=e});