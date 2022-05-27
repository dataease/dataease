// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./global"],function(b,c){const d=c.crypto||c.msCrypto;b.generateUUID=function(){const a=d.getRandomValues(new Uint16Array(8));a[3]=a[3]&4095|16384;a[4]=a[4]&16383|32768;return a[0].toString(16)+a[1].toString(16)+"-"+a[2].toString(16)+"-"+a[3].toString(16)+"-"+a[4].toString(16)+"-"+a[5].toString(16)+a[6].toString(16)+a[7].toString(16)};Object.defineProperty(b,"__esModule",{value:!0})});