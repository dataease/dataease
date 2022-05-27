/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import e from"../layers/support/LabelClass.js";const r=/\[([^\[\]]+)\]/gi;function n(n,s,t){return n?n.map((n=>{const o=new e;if(o.read(n,t),o.labelExpression){const e=s.fields||s.layerDefinition&&s.layerDefinition.fields||this.fields;o.labelExpression=o.labelExpression.replace(r,((r,n)=>`[${function(e,r){if(!r)return e;const n=e.toLowerCase();for(let e=0;e<r.length;e++){const s=r[e].name;if(s.toLowerCase()===n)return s}return e}(n,e)}]`))}return o})):null}export{n as r};
