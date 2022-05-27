/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{i as e}from"./Logger.js";import{getFeatureEditFields as p,fixFields as l,hasField as t}from"../layers/support/fieldUtils.js";async function i(i,s=i.popupTemplate){if(!e(s))return[];const a=await s.getRequiredFields(i.fields),{lastEditInfoEnabled:d}=s,{objectIdField:o,typeIdField:u,globalIdField:n,relationships:f}=i;if(a.includes("*"))return["*"];const r=d?await p(i):[],m=l(i.fields,[...a,...r]);return u&&m.push(u),m&&o&&t(i.fields,o)&&-1===m.indexOf(o)&&m.push(o),m&&n&&t(i.fields,n)&&-1===m.indexOf(n)&&m.push(n),f&&f.forEach((e=>{const{keyField:p}=e;m&&p&&t(i.fields,p)&&-1===m.indexOf(p)&&m.push(p)})),m}function s(p,l){return p.popupTemplate?p.popupTemplate:e(l)&&l.defaultPopupTemplateEnabled&&e(p.defaultPopupTemplate)?p.defaultPopupTemplate:null}export{i as a,s as g};
