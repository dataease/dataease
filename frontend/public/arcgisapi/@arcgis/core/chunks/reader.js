/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{s as r}from"./object.js";import{propertyJSONMeta as o}from"../core/accessorSupport/decorators/property.js";function e(e,t,a){let c,s;return void 0===t||Array.isArray(t)?(s=e,a=t,c=[void 0]):(s=t,c=Array.isArray(e)?e:[e]),(e,t)=>{const d=e.constructor.prototype;c.forEach((c=>{const p=o(e,c,s);p.read&&"object"!=typeof p.read&&(p.read={}),r("read.reader",d[t],p),a&&(p.read.source=(p.read.source||[]).concat(a))}))}}export{e as r};
