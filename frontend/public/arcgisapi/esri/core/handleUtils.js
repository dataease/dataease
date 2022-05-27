// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./maybe"],function(c,e){function d(a){return{remove:()=>{a&&(a(),a=void 0)}}}c.asyncHandle=function(a,b){let f=!1,g=null;a.then(h=>{f?h.remove():g=h});return d(()=>{f=!0;e.isSome(g)?g.remove():e.isSome(b)&&(b.abort(),b=null)})};c.destroyHandle=function(a){return d(e.isSome(a)?()=>a.destroy():void 0)};c.handlesGroup=function(a){return d(()=>a.forEach(b=>e.isSome(b)&&b.remove()))};c.makeHandle=d;c.refHandle=function(a){return d(()=>{const b=a();e.isSome(b)&&b.remove()})};c.timeoutHandle=
function(a,b){const f=setTimeout(a,b);return d(()=>clearTimeout(f))};Object.defineProperty(c,"__esModule",{value:!0})});