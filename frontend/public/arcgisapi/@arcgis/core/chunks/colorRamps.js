/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import t from"../tasks/support/ColorRamp.js";import o from"../tasks/support/AlgorithmicColorRamp.js";import r from"../tasks/support/MultipartColorRamp.js";const p={key:"type",base:t,typeMap:{algorithmic:o,multipart:r}};function m(t){return t&&t.type?"algorithmic"===t.type?o.fromJSON(t):"multipart"===t.type?r.fromJSON(t):null:null}export{m as f,p as t};
