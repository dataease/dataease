/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{_ as e}from"./tslib.es6.js";import{subclass as s}from"../core/accessorSupport/decorators/subclass.js";let t=0;const r=r=>{let o=class extends r{constructor(...e){super(...e),Object.defineProperty(this,"uid",{writable:!1,configurable:!1,value:Date.now().toString(16)+"-object-"+t++})}};return o=e([s("esri.core.Identifiable")],o),o};let o=class extends(r(class{})){};o=e([s("esri.core.Identifiable")],o);export{r as I};
