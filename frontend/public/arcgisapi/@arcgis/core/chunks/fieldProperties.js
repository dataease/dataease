/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{L as e}from"./Logger.js";import{hasField as i,fixFields as t}from"../layers/support/fieldUtils.js";import s from"../layers/support/Field.js";const r=e.getLogger("esri.layers.support.fieldProperties");function l(){return{fields:{type:[s],value:null},outFields:{type:[String],dependsOn:["fields"],json:{read:!1},set:function(e){this._userOutFields=e,this.notifyChange("outFields")},get:function(){const e=this._userOutFields;if(!e||!e.length)return null;if(e.includes("*"))return["*"];if(!this.fields)return e;for(const t of e){i(this.fields,t)||r.error("field-attributes-layer:invalid-field",`Invalid field ${t} found in outFields`,{layer:this,outFields:e})}return t(this.fields,e)}}}}export{l as d};
