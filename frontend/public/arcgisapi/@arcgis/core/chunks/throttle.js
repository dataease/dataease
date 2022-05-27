/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function e(e,t,l,n){let o=null,u=1e3;"number"==typeof t?(u=t,n=l):(o=null!=t?t:null,u=l);let a,p=0;const r=()=>{p=0,e.apply(n,a)},c=(...e)=>{o&&o.apply(n,e),a=e,u?p||(p=setTimeout(r,u)):r()};return c.remove=()=>{p&&(clearTimeout(p),p=0)},c.forceUpdate=()=>{p&&(clearTimeout(p),r())},c.hasPendingUpdates=()=>!!p,c}export{e as t};
