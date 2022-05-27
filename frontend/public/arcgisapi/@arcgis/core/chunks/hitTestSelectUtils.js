/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
async function t(t,s){if("2d"===t.type)return t.hitTest(s);const e=await t.hitTest(s),n=e.results[0],i=e.results.findIndex((t=>t.distance!==n.distance));return-1!==i&&(e.results=e.results.slice(0,i)),e}export{t as h};
