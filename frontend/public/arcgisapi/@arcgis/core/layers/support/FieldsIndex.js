/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function t(t){return"date"===t.type||"esriFieldTypeDate"===t.type}function e(t){return t.toLowerCase().trim()}export default class{constructor(s){if(this.fields=s,this._fieldsMap=new Map,this._dateFieldsSet=new Set,this.dateFields=[],!s)return;const i=[];for(const d of s){const s=d&&d.name;if(s){const a=e(s);this._fieldsMap.set(s,d),this._fieldsMap.set(a,d),i.push(a),t(d)&&(this.dateFields.push(d),this._dateFieldsSet.add(d))}}i.sort(),this.uid=i.join(",")}destroy(){this._fieldsMap.clear()}has(t){return null!=this.get(t)}get(t){return null!=t?this._fieldsMap.get(t)||this._fieldsMap.get(e(t)):void 0}isDateField(t){return this._dateFieldsSet.has(this.get(t))}normalizeFieldName(t){const e=this.get(t);if(e)return e.name}}
