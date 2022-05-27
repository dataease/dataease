// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(h){(function(g){g.length=function(a,b){const c=a[b],e=a[b+1];a=a[b+2];return Math.sqrt(c*c+e*e+a*a)};g.normalize=function(a,b){var c=a[b];const e=a[b+1],d=a[b+2];c=1/Math.sqrt(c*c+e*e+d*d);a[b]*=c;a[b+1]*=c;a[b+2]*=c};g.scale=function(a,b,c){a[b]*=c;a[b+1]*=c;a[b+2]*=c};g.add=function(a,b,c,e,d,f=b){d=d||a;d[f]=a[b]+c[e];d[f+1]=a[b+1]+c[e+1];d[f+2]=a[b+2]+c[e+2]};g.subtract=function(a,b,c,e,d,f=b){d=d||a;d[f]=a[b]-c[e];d[f+1]=a[b+1]-c[e+1];d[f+2]=a[b+2]-c[e+2]}})(h.Vec3Compact||
(h.Vec3Compact={}));Object.defineProperty(h,"__esModule",{value:!0})});