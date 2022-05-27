/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import"../chunks/object.js";import{clone as t}from"./lang.js";import"../config.js";import{L as e}from"../chunks/Logger.js";import"../chunks/string.js";import{M as s}from"../chunks/Message.js";class r extends s{constructor(t,e,s){if(super(t,e,s),!(this instanceof r))return new r(t,e,s)}toJSON(){if(null!=this.details)try{return{name:this.name,message:this.message,details:JSON.parse(JSON.stringify(this.details,((e,s)=>{if(s&&"object"==typeof s&&"function"==typeof s.toJSON)return s;try{return t(s)}catch(t){return"[object]"}})))}}catch(t){throw e.getLogger("esri.core.Error").error(t),t}return{name:this.name,message:this.message,details:this.details}}static fromJSON(t){return new r(t.name,t.message,t.details)}}r.prototype.type="error";export default r;
