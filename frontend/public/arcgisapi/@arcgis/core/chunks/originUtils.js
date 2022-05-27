/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{i}from"./multiOriginJSONSupportUtils.js";function r(r){r&&r.writtenProperties&&r.writtenProperties.forEach((r=>{const t=r.target;r.newOrigin&&r.oldOrigin!==r.newOrigin&&i(t)&&t.updateOrigin(r.propName,r.newOrigin)}))}export{r as u};
