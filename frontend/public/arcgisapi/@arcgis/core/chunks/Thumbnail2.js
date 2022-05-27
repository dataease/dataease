/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as r}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as o}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as s}from"../core/accessorSupport/decorators/subclass.js";import{a as t}from"./JSONSupport.js";import"../core/urlUtils.js";import"./resourceExtension.js";var e;let p=e=class extends t{constructor(){super(...arguments),this.url=""}destroy(){this.url=""}clone(){return new e({url:this.url})}};r([o({type:String,json:{write:{isRequired:!0}}})],p.prototype,"url",void 0),p=e=r([s("esri.webdoc.support.Thumbnail")],p);var i=p;export{i as T};
