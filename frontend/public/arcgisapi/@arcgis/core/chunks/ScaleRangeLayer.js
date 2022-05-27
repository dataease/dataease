/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import"./object.js";import"./Logger.js";import{property as r}from"../core/accessorSupport/decorators/property.js";import"./ensureType.js";import{subclass as o}from"../core/accessorSupport/decorators/subclass.js";import"../core/urlUtils.js";import"./resourceExtension.js";const s=s=>{let t=class extends s{constructor(){super(...arguments),this.minScale=0,this.maxScale=0}get scaleRangeId(){return`${this.minScale},${this.maxScale}`}};return e([r({type:Number,nonNullable:!0,json:{write:!0}})],t.prototype,"minScale",void 0),e([r({type:Number,nonNullable:!0,json:{write:!0}})],t.prototype,"maxScale",void 0),e([r({readOnly:!0,dependsOn:["minScale","maxScale"]})],t.prototype,"scaleRangeId",null),t=e([o("esri.layers.mixins.ScaleRangeLayer")],t),t};export{s as S};
