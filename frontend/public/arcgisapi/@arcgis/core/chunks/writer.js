/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{s as r}from"./object.js";import{propertyJSONMeta as t}from"../core/accessorSupport/decorators/property.js";function o(o,e,i){let s,c;return void 0===e?(c=o,s=[void 0]):"string"!=typeof e?(c=o,s=[void 0],i=e):(c=e,s=Array.isArray(o)?o:[o]),(o,e)=>{const p=o.constructor.prototype;s.forEach((s=>{const a=t(o,s,c);a.write&&"object"!=typeof a.write&&(a.write={}),i&&r("write.target",i,a),r("write.writer",p[e],a)}))}}export{o as w};
