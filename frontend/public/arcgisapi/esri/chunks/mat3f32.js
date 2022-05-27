// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(d){function e(){const a=new Float32Array(9);a[0]=1;a[4]=1;a[8]=1;return a}function f(a){const b=new Float32Array(9);b[0]=a[0];b[1]=a[1];b[2]=a[2];b[3]=a[3];b[4]=a[4];b[5]=a[5];b[6]=a[6];b[7]=a[7];b[8]=a[8];return b}function g(a,b,k,l,m,n,p,q,r){const c=new Float32Array(9);c[0]=a;c[1]=b;c[2]=k;c[3]=l;c[4]=m;c[5]=n;c[6]=p;c[7]=q;c[8]=r;return c}function h(a,b){return new Float32Array(a,b,9)}var t=Object.freeze({__proto__:null,create:e,clone:f,fromValues:g,createView:h});
d.clone=f;d.create=e;d.createView=h;d.fromValues=g;d.mat3f32=t});