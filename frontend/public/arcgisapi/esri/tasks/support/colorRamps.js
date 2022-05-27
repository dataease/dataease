// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./ColorRamp","./AlgorithmicColorRamp","./MultipartColorRamp"],function(b,c,d,e){c={key:"type",base:c,typeMap:{algorithmic:d,multipart:e}};b.fromJSON=function(a){return a&&a.type?"algorithmic"===a.type?d.fromJSON(a):"multipart"===a.type?e.fromJSON(a):null:null};b.types=c;Object.defineProperty(b,"__esModule",{value:!0})});