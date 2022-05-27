/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{h as t}from"./object.js";import{clone as e}from"../core/lang.js";import{d as r,a as n,b as i}from"./defaultsJSON.js";function o(t){return{renderer:{type:"simple",symbol:"esriGeometryPoint"===t||"esriGeometryMultipoint"===t?r:"esriGeometryPolyline"===t?n:i}}}function s(e,r){if(t("csp-restrictions"))return()=>({[r]:null,...e});try{let t=`this.${r} = null;`;for(const r in e){t+=`this${r.indexOf(".")?`["${r}"]`:`.${r}`} = ${JSON.stringify(e[r])};`}const n=new Function(t);return()=>new n}catch(t){return()=>({[r]:null,...e})}}function u(t={}){return[{name:"New Feature",description:"",prototype:{attributes:e(t)}}]}export{u as a,s as b,o as c};
