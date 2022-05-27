/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{a}from"./object.js";function c(c){return c=c||a.location.hostname,e.some((a=>{var t;return null!=(null==(t=c)?void 0:t.match(a))}))}function t(c,t){return c&&(t=t||a.location.hostname)?null!=t.match(o)||null!=t.match(r)?c.replace("static.arcgis.com","staticdev.arcgis.com"):null!=t.match(s)||null!=t.match(m)?c.replace("static.arcgis.com","staticqa.arcgis.com"):c:c}const o=/^devext.arcgis.com$/,s=/^qaext.arcgis.com$/,r=/^[\w-]*\.mapsdevext.arcgis.com$/,m=/^[\w-]*\.mapsqa.arcgis.com$/,e=[/^([\w-]*\.)?[\w-]*\.zrh-dev-local.esri.com$/,o,s,/^jsapps.esri.com$/,r,m];export{t as a,c as i};
