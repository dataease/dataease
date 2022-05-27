/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{M as t}from"./MemCache.js";class e{constructor(e,s){this._storage=new t,this._storage.maxSize=e,s&&this._storage.registerRemoveFunc("",s)}put(t,e){this._storage.put(t,e,1,1)}pop(t){return this._storage.pop(t)}get(t){return this._storage.get(t)}clear(){this._storage.clearAll()}destroy(){this._storage.clearAll()}}export{e as I};
