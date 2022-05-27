// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(g){function h(b,c,a){const e=b.typedBuffer;b=b.typedBufferStride;const f=c.typedBuffer,k=c.typedBufferStride;c=a?a.count:c.count;let d=(a&&a.dstIndex?a.dstIndex:0)*b;a=(a&&a.srcIndex?a.srcIndex:0)*k;for(let l=0;l<c;++l)e[d]=f[a],e[d+1]=f[a+1],e[d+2]=f[a+2],d+=b,a+=k}var m=Object.freeze({__proto__:null,copy:h});g.copy=h;g.vec3=m});