/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import t from"../core/Error.js";import{u as i}from"./utils5.js";import{supportedAgeUnits as s}from"../smartMapping/statistics/support/ageUtils.js";import n from"../smartMapping/statistics/summaryStatisticsForAge.js";async function a(a){const r="days",o={...a,unit:r},u=await n(o);if(null==u.avg)throw new t("age-unit:insufficient-info","'avg' statistics is invalid");const c=function(t){const n=Math.abs(t.avg);let a=null;return s.some((t=>{const s=i[t];return n>2*s&&(a=t),!!a})),a}(u);if(c===r)return{unit:c,statistics:u};o.unit=c;const e=await n(o);if(null==e.avg)throw new t("age-unit:insufficient-info","'avg' statistics is invalid");return{unit:c,statistics:e}}export{a};
