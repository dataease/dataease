// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(d){d.process=function(a){if(a.json&&a.json.origins){const b=a.json.origins;a={"web-document":["web-scene","web-map"]};for(const c in a)if(b[c]){const e=b[c];a[c].forEach(f=>{b[f]=e});delete b[c]}}};Object.defineProperty(d,"__esModule",{value:!0})});