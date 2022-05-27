/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as r}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as s}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as o}from"../core/accessorSupport/decorators/subclass.js";import"../core/urlUtils.js";import"./resourceExtension.js";const e=e=>{let t=class extends e{constructor(){super(...arguments),this.customParameters=null}};return r([s({json:{write:!0,origins:{"web-scene":{write:!1}}}})],t.prototype,"customParameters",void 0),t=r([o("esri.layers.mixins.CustomParametersMixin")],t),t};export{e as C};
