/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as r}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as o}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as s}from"../core/accessorSupport/decorators/subclass.js";import"../core/urlUtils.js";import"./resourceExtension.js";import{I as e}from"./Identifiable.js";const t=t=>{let i=class extends(e(t)){constructor(){super(...arguments),this.graphics=null,this.renderer=null,this.view=null}};return r([o()],i.prototype,"graphics",void 0),r([o()],i.prototype,"renderer",void 0),r([o()],i.prototype,"updating",void 0),r([o()],i.prototype,"view",void 0),i=r([s("esri.views.layers.GraphicsView")],i),i};export{t as G};
