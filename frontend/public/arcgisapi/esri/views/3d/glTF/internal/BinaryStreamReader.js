// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(c){let f=function(){function d(a){this.data=a;this.offset4=0;this.dataUint32=new Uint32Array(this.data,0,Math.floor(this.data.byteLength/4))}var b=d.prototype;b.readUint32=function(){const a=this.offset4;this.offset4+=1;return this.dataUint32[a]};b.readUint8Array=function(a){const e=4*this.offset4;this.offset4+=a/4;return new Uint8Array(this.data,e,a)};b.remainingBytes=function(){return this.data.byteLength-4*this.offset4};return d}();c.BinaryStreamReader=f;Object.defineProperty(c,
"__esModule",{value:!0})});