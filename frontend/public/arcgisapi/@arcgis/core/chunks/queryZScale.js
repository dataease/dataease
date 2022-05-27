/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{g as e}from"./zscale.js";function t(t,o,r){if(!r||!r.features||!r.hasZ)return;const a=e(r.geometryType,o,t.outSpatialReference);if(a)for(const e of r.features)a(e.geometry)}export{t as a};
