/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function r(r){return r.split(",").map((r=>r.trim()))}function e(e){const n="string"==typeof e?r(e):e;return function(r,e){r.hasOwnProperty("_renderableProps")||(r._renderableProps=r._renderableProps?r._renderableProps.slice():[]);const s=r._renderableProps;var p;n?s.push.apply(s,(p=e,n.map((r=>function(r,e){return 0===r.indexOf(e)?r:`${e}.${r}`}(r,p))))):s.push(e)}}export{e as r,r as s};
