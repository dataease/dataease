// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../core/promiseUtils ../WebStyleSymbol ../../chunks/symbols ./cimAnalyzer ./ExpandedCIM".split(" "),function(c,g,h,n,k,l){const e=async(a,b)=>new l.ExpandedCIM(await k.analyzeCIMSymbol(a.data,b),a.data,a.rendererKey),f=async(a,b,d)=>a?"cim"===a.type?e(a,b):"web-style"===a.type?(a={type:"cim",data:(await h.fromJSON(a).fetchCIMSymbol(d)).data,rendererKey:a.rendererKey},e(a,b)):a:null;c.expandSymbol=f;c.expandSymbols=async(a,b,d)=>g.all(a.map(m=>f(m,b,d)));Object.defineProperty(c,
"__esModule",{value:!0})});