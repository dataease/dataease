// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){function f(b,c,a){const l=b.typedBuffer;b=b.typedBufferStride;const m=c.typedBuffer,g=c.typedBufferStride;c=a?a.count:c.count;let h=(a&&a.dstIndex?a.dstIndex:0)*b;a=(a&&a.srcIndex?a.srcIndex:0)*g;for(let k=0;k<c;++k){for(let d=0;16>d;++d)l[h+d]=m[a+d];h+=b;a+=g}}var n=Object.freeze({__proto__:null,copy:f});e.copy=f;e.mat4=n});