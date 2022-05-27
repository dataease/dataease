// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./promiseUtils"],function(c,f){c.assertResult=function(b){if(!0===b.ok)return b.value;throw b.error;};c.forEach=function(b,a,g){return f.eachAlways(b.map((d,e)=>a.apply(g,[d,e])))};c.map=function(b,a,g){return f.eachAlways(b.map((d,e)=>a.apply(g,[d,e]))).then(d=>d.map(e=>e.value))};c.result=function(b){return b.then(a=>({ok:!0,value:a})).catch(a=>({ok:!1,error:a}))};c.resultOrAbort=function(b){return b.then(a=>({ok:!0,value:a})).catch(a=>{f.throwIfAbortError(a);return{ok:!1,error:a}})};
Object.defineProperty(c,"__esModule",{value:!0})});