/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{i as e}from"./dehydratedFeatures.js";class r{constructor(e,r=0){this.array=e,this.offset=r}}function a(e){return"array"in e}function t(r,t,n="ground"){if(e(t))return r.getElevation(t.x,t.y,t.z||0,t.spatialReference,n);if(a(t)){let e=t.offset;return r.getElevation(t.array[e++],t.array[e++],t.array[e]||0,r.spatialReference,n)}return r.getElevation(t[0],t[1],t[2]||0,r.spatialReference,n)}export{r as S,t as g,a as i};
