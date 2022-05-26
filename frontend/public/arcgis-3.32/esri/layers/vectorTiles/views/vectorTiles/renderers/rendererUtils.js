// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/layers/vectorTiles/views/vectorTiles/renderers/rendererUtils",["require","exports"],function(d,a){Object.defineProperty(a,"__esModule",{value:!0});var c=new Uint32Array(1),b=new Uint8Array(c.buffer);a.int32To4Bytes=function(a){c[0]=a|0;return[b[0],b[1],b[2],b[3]]}});