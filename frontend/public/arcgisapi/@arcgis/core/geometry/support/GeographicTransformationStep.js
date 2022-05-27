/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
let s=0;class t{constructor(t=null){this.uid=s++,t?(this._wkt=void 0!==t.wkt?t.wkt:null,this._wkid=void 0!==t.wkid?t.wkid:-1,this._isInverse=void 0!==t.isInverse&&!0===t.isInverse):(this._wkt=null,this._wkid=-1,this._isInverse=!1)}static fromGE(s){const i=new t;return i._wkt=s.wkt,i._wkid=s.wkid,i._isInverse=s.isInverse,i}get wkt(){return this._wkt}set wkt(t){this._wkt=t,this.uid=s++}get wkid(){return this._wkid}set wkid(t){this._wkid=t,this.uid=s++}get isInverse(){return this._isInverse}set isInverse(t){this._isInverse=t,this.uid=s++}getInverse(){const s=new t;return s._wkt=this.wkt,s._wkid=this._wkid,s._isInverse=!this.isInverse,s}}export default t;
