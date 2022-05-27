// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(e){e.bytesToUTF8=function(b,g){let c=0,f="",d=0,a=0;const h=b.length;for(;c<h;)if(a=b[c++],d=a>>4,8>d?d=1:15===d?(d=4,a=(a&7)<<18|(b[c++]&63)<<12|(b[c++]&63)<<6|b[c++]&63):14===d?(d=3,a=(a&15)<<12|(b[c++]&63)<<6|b[c++]&63):(d=2,a=(a&31)<<6|b[c++]&63),0!==a||g)f+=String.fromCharCode(a);return f};Object.defineProperty(e,"__esModule",{value:!0})});