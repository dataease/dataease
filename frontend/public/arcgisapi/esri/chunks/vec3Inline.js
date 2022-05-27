// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(d){function e(c,a,b){c[0]=a[0]+b[0];c[1]=a[1]+b[1];c[2]=a[2]+b[2]}function f(c,a,b){c[0]=a[0]-b[0];c[1]=a[1]-b[1];c[2]=a[2]-b[2]}function g(c,a,b){c[0]=a[0]*b;c[1]=a[1]*b;c[2]=a[2]*b}function h(c,a){let b=a[0]*a[0]+a[1]*a[1]+a[2]*a[2];0<b&&(b=1/Math.sqrt(b),c[0]=a[0]*b,c[1]=a[1]*b,c[2]=a[2]*b)}function k(c,a,b){c[0]=a[1]*b[2]-a[2]*b[1];c[1]=a[2]*b[0]-a[0]*b[2];c[2]=a[0]*b[1]-a[1]*b[0]}var l=Object.freeze({__proto__:null,add:e,subtract:f,scale:g,normalize:h,cross:k});d.add=
e;d.cross=k;d.normalize=h;d.scale=g;d.subtract=f;d.vec3Inline=l});