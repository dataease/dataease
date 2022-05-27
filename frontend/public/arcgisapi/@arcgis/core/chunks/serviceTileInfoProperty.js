/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import r from"../layers/support/TileInfo.js";const e={type:r,json:{origins:{service:{read:{source:["tileInfo","minScale","maxScale","minLOD","maxLOD"],reader:n}}}}};function n(e,n,l,o){if(!e)return null;const{minScale:s,maxScale:i,minLOD:t,maxLOD:a}=n;if(null!=t&&null!=a)return o&&o.ignoreMinMaxLOD?r.fromJSON(e):r.fromJSON({...e,lods:e.lods.filter((({level:r})=>null!=r&&r>=t&&r<=a))});if(0!==s&&0!==i){const n=r=>Math.round(1e4*r)/1e4,l=s?n(s):1/0,o=i?n(i):-1/0;return r.fromJSON({...e,lods:e.lods.filter((r=>{const e=n(r.scale);return e<=l&&e>=o}))})}return r.fromJSON(e)}export{n as r,e as s};
