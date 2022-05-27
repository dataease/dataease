/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{b as o}from"./Logger.js";import a from"../core/Collection.js";import{L as r}from"./Loadable.js";import{m as l,r as s}from"./asyncUtils.js";async function t(o,a){return await o.load(),i(o,a)}async function i(t,i){const n=[],c=(...l)=>{for(const s of l)o(s)||(Array.isArray(s)?c(...s):a.isCollection(s)?s.forEach((o=>c(o))):r.isLoadable(s)&&n.push(s))};i(c);let f=null;if(await l(n,(async o=>{var a;!1!==(await s((a=o,"loadAll"in a&&"function"==typeof a.loadAll?o.loadAll():o.load()))).ok||f||(f=o)})),f)throw f.loadError;return t}export{i as a,t as l};
