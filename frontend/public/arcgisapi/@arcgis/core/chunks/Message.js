/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{g as t}from"./object.js";class s{constructor(e,i,r){var n;(this.name=e,this.details=r,this.message=void 0,this instanceof s)&&(this.message=i&&(n=r,i.replace(/\$\{([^\s\:\}]*)(?:\:([^\s\:\}]+))?\}/g,(function(s,e){if(""===e)return"$";const i=t(e,n),r=null==i?"":i;if(void 0===r)throw new Error(`could not find key "${e}" in template`);return r.toString()})))||"")}toString(){return"["+this.name+"]: "+this.message}}export{s as M};
