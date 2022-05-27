// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){function g(a){return a-Math.floor(a)}const h=[1,256,65536,16777216],k=[1/256,1/65536,1/16777216,1/4294967296],f=function(a,d=0){let c=0;for(let b=0;4>b;b++)c+=a[d+b]*k[b];return c}(new Uint8ClampedArray([255,255,255,255]));e.packFloatRGBA=function(a,d,c=0){a=0>a?0:a>f?f:a;for(let b=0;4>b;b++)d[c+b]=Math.floor(256*g(a*h[b]))};Object.defineProperty(e,"__esModule",{value:!0})});