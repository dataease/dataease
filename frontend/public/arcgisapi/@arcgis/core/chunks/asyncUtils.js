/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{eachAlways as r,throwIfAbortError as t}from"../core/promiseUtils.js";function e(t,e,n){return r(t.map(((r,t)=>e.apply(n,[r,t]))))}function n(t,e,n){return r(t.map(((r,t)=>e.apply(n,[r,t])))).then((r=>r.map((r=>r.value))))}function o(r){return r.then((r=>({ok:!0,value:r}))).catch((r=>({ok:!1,error:r})))}function a(r){return r.then((r=>({ok:!0,value:r}))).catch((r=>{t(r);return{ok:!1,error:r}}))}function u(r){if(!0===r.ok)return r.value;throw r.error}export{u as a,a as b,e as f,n as m,o as r};
