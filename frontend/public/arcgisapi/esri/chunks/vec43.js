// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(k){function l(c,e,a){const f=c.typedBuffer;c=c.typedBufferStride;const g=e.typedBuffer,b=e.typedBufferStride;e=a?a.count:e.count;let d=(a&&a.dstIndex?a.dstIndex:0)*c;a=(a&&a.srcIndex?a.srcIndex:0)*b;for(let h=0;h<e;++h)f[d]=g[a],f[d+1]=g[a+1],f[d+2]=g[a+2],f[d+3]=g[a+3],d+=c,a+=b}function m(c,e,a,f,g,b){const d=c.typedBuffer,h=c.typedBufferStride;c=b?b.count:c.count;b=(b&&b.dstIndex?b.dstIndex:0)*h;for(let n=0;n<c;++n)d[b]=e,d[b+1]=a,d[b+2]=f,d[b+3]=g,b+=h}var p=Object.freeze({__proto__:null,
copy:l,fill:m});k.copy=l;k.fill=m;k.vec4=p});