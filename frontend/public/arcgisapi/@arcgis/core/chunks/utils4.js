/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{urlToObject as t}from"../core/urlUtils.js";function e(t,e){let n={query:t};return e&&(n={...e,...n}),n}function n(e){return"string"==typeof e?t(e):e}function r(t,e,n){const o={};for(const i in t){if("declaredClass"===i)continue;const s=t[i];if(null!=s&&"function"!=typeof s)if(Array.isArray(s)){o[i]=[];for(let t=0;t<s.length;t++)o[i][t]=r(s[t])}else if("object"==typeof s)if(s.toJSON){const t=s.toJSON(n&&n[i]);o[i]=e?t:JSON.stringify(t)}else o[i]=e?s:JSON.stringify(s);else o[i]=s}return o}export{e as a,r as e,n as p};
