/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{property as e}from"../core/accessorSupport/decorators/property.js";import{J as r}from"./jsonMap.js";function n(n,o={ignoreUnknown:!0}){const t=n instanceof r?n:new r(n,o),a={type:null!=o&&o.ignoreUnknown?t.apiValues:String,readOnly:null==o?void 0:o.readOnly,json:{type:t.jsonValues,read:(null==o||!o.readOnly)&&{reader:t.read},write:{writer:t.write}}};return void 0!==(null==o?void 0:o.default)&&(a.json.default=o.default),e(a)}export{n as e};
