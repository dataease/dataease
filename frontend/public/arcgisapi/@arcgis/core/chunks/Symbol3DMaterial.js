/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as o}from"./tslib.es6.js";import"./object.js";import{i as r}from"./Logger.js";import{property as s}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as t}from"../core/accessorSupport/decorators/subclass.js";import{a as e}from"./JSONSupport.js";import"../core/urlUtils.js";import"./resourceExtension.js";import{c as p}from"./materialUtils.js";var c;let i=c=class extends e{constructor(){super(...arguments),this.color=null}clone(){return new c({color:r(this.color)?this.color.clone():null})}};o([s(p)],i.prototype,"color",void 0),i=c=o([t("esri.symbols.support.Symbol3DMaterial")],i);var l=i;export{l as S};
