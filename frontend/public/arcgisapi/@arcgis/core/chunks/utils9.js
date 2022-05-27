/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"../core/promiseUtils.js";import"../Basemap.js";import"../geometry.js";import{a as e,t,c as i}from"./screenUtils.js";import"./basemapUtils.js";function r(e,t){return e.r===t.r&&e.g===t.g&&e.b===t.b}function s(e,t){let i=0;if(e.length===t.length){let s=e.every(((e,i)=>r(e,t[i])));if(s)i=1;else{s=e.slice(0).reverse().every(((e,i)=>r(e,t[i]))),s&&(i=-1)}}return i}function o(r,s){return Math.ceil(function(e){const t=e.width,r=e.height;let s=e.pixelSizeAt(e.toMap(i(.5*t,.5*r),{exclude:[]}));if(s<=0&&(s=e.pixelSizeAt(e.toMap(i(.5*t,.95*r),{exclude:[]})),s<=0)){const t=e.camera.position.clone();t.z=0,s=2*e.pixelSizeAt(t)}return s}(s)*e(t(r)))}export{s as h,o as t};
