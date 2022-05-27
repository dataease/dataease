// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(g){const l=Math.PI/180;g.bboxIntersects=function(a,b){const [c,d,e,f]=a,[h,k,m,n]=b;return!(c>m||e<h||d>n||f<k)};g.getBBox=function(a,b,c,d){const [e,f]=b,[h,k]=d;b=.5*c;a[0]=e-b*h;a[1]=f-b*k;a[2]=e+b*h;a[3]=f+b*k;return a};g.getOuterSize=function(a,b){var c=b.rotation*l;const d=Math.abs(Math.cos(c));c=Math.abs(Math.sin(c));const [e,f]=b.size;a[0]=Math.round(f*c+e*d);a[1]=Math.round(f*d+e*c);return a};g.snapToPixel=function(a,b,c){const {resolution:d,size:e}=c;a[0]=d*(Math.round(b[0]/
d)+e[0]%2*.5);a[1]=d*(Math.round(b[1]/d)+e[1]%2*.5);return a};Object.defineProperty(g,"__esModule",{value:!0})});