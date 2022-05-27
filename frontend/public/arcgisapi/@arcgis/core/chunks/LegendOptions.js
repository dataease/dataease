/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as r}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as t}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as o}from"../core/accessorSupport/decorators/subclass.js";import{a as s}from"./JSONSupport.js";import"../core/urlUtils.js";import"./resourceExtension.js";var e;let p=e=class extends s{constructor(){super(...arguments),this.title=null}clone(){return new e({title:this.title})}};r([t({type:String,json:{write:!0}})],p.prototype,"title",void 0),p=e=r([o("esri.renderers.support.LegendOptions")],p);var i=p;export{i as L,p as a};
