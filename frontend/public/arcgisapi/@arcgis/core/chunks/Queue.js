/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{a as e}from"./Logger.js";class t{constructor(e=(e=>e.values().next().value)){this._peeker=e,this._items=new Set}get length(){return this._items.size}clear(){this._items.clear()}peek(){if(0!==this._items.size)return this._peeker(this._items)}push(e){this.contains(e)||this._items.add(e)}contains(e){return this._items.has(e)}pop(){if(0===this.length)return;const t=this.peek();return this._items.delete(e(t)),t}remove(e){this._items.delete(e)}filter(e){return this._items.forEach((t=>{e(t)||this._items.delete(t)})),this}}export{t as Q};
