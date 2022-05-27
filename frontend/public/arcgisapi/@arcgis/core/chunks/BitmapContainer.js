/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{b as s}from"./brushes.js";import{W as e}from"./enums.js";import{W as r}from"./WGLContainer.js";class a extends r{prepareRenderPasses(r){const a=r.registerRenderPass({name:"bitmap",brushes:[s.bitmap],target:()=>this.children,drawPhase:e.MAP});return[...super.prepareRenderPasses(r),a]}}export{a as B};
