// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(d){function f(a,e=0){let c=0;for(let b=0;4>b;b++)c+=a[e+b]*h[b];return c}function k(a){return a-Math.floor(a)}const l=[1,256,65536,16777216],h=[1/256,1/65536,1/16777216,1/4294967296],g=f(new Uint8ClampedArray([255,255,255,255]));d.packFloatRGBA=function(a,e,c=0){a=0>a?0:a>g?g:a;for(let b=0;4>b;b++)e[c+b]=Math.floor(256*k(a*l[b]))};d.unpackFloatRGBA=f;Object.defineProperty(d,"__esModule",{value:!0})});