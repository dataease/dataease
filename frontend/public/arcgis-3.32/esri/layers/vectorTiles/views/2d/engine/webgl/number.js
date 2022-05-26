// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/2d/engine/webgl/number",["require","exports"],function(f,b){Object.defineProperty(b,"__esModule",{value:!0});var c=new Float32Array(1),d=new Uint32Array(c.buffer);b.nextHighestPowerOfTwo=function(a){a--;a|=a>>1;a|=a>>2;a|=a>>4;a|=a>>8;a|=a>>16;a++;return a};b.toUint32=function(a){c[0]=a;return d[0]};b.i1616to32=function(a,b){return 65535&a|b<<16};b.i8888to32=function(a,b,e,c){return a&255|(b&255)<<8|(e&255)<<16|c<<24};b.i8816to32=function(a,b,c){return a&255|
(b&255)<<8|c<<16};b.numTo32=function(a){return a|0}});