// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(b){b.keyFromSymbol=function(a){switch(a.type){case "esriSMS":return`${a.style}.${a.path}`;case "esriSLS":return`${a.style}.${a.cap}`;case "esriSFS":return`${a.style}`;case "esriPFS":case "esriPMS":return a.imageData?`${a.imageData}${a.width}${a.height}`:`${a.url}${a.width}${a.height}`;default:return a.mosaicHash?a.mosaicHash:JSON.stringify(a)}};Object.defineProperty(b,"__esModule",{value:!0})});