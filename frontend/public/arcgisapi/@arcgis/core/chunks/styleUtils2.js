/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{throwIfAborted as e}from"../core/promiseUtils.js";import{W as r}from"../core/accessorSupport/decorators/subclass.js";import{r as s}from"./asyncUtils.js";async function o(o,t,i){const n=o&&o.getAtOrigin&&o.getAtOrigin("renderer",t.origin);if(n&&"unique-value"===n.type&&n.styleOrigin){const a=await s(n.populateFromStyle());if(e(i),!1===a.ok){const e=a.error;t&&t.messages&&t.messages.push(new r("renderer:style-reference",`Failed to create unique value renderer from style reference: ${e.message}`,{error:e,context:t})),o.clear("renderer",t.origin)}}}export{o as l};
