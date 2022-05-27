/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as r}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as e}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as s}from"../core/accessorSupport/decorators/subclass.js";import"../core/urlUtils.js";import"./resourceExtension.js";const o=o=>{let t=class extends o{constructor(){super(...arguments),this.refreshInterval=0}refresh(){this.emit("refresh")}};return r([e({type:Number,cast:r=>r>=.1?r:r<=0?0:.1,json:{write:!0,origins:{"web-document":{write:!0}}}})],t.prototype,"refreshInterval",void 0),t=r([s("esri.layers.mixins.RefreshableLayer")],t),t};export{o as R};
