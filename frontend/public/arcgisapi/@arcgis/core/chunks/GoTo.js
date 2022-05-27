/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as o}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as r}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as s}from"../core/accessorSupport/decorators/subclass.js";import"../core/urlUtils.js";import"./resourceExtension.js";const e=e=>{let t=class extends e{constructor(...o){super(...o),this.goToOverride=null,this.view=null}callGoTo(o){const{view:r}=this;return this.goToOverride?this.goToOverride(r,o):r.goTo(o.target,o.options)}};return o([r()],t.prototype,"goToOverride",void 0),o([r()],t.prototype,"view",void 0),t=o([s("esri.widgets.support.GoTo")],t),t};export{e as G};
