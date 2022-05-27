/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{urlToObject as r}from"../core/urlUtils.js";import t from"../portal/Portal.js";function o(o){return{origin:"portal-item",url:r(o.itemUrl),portal:o.portal||t.getDefault(),portalItem:o,readResourcePaths:[]}}export{o as c};
