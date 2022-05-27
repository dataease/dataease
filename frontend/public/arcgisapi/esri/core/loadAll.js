// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","./maybe","./Collection","./Loadable","./asyncUtils"],function(c,n,p,q,h){async function k(d,f){await d.load();return l(d,f)}async function l(d,f){const m=[],g=(...a)=>{for(const b of a)n.isNone(b)||(Array.isArray(b)?g(...b):p.isCollection(b)?b.forEach(r=>g(r)):q.isLoadable(b)&&m.push(b))};f(g);let e=null;await h.map(m,async a=>{!1!==(await h.result("loadAll"in a&&"function"===typeof a.loadAll?a.loadAll():a.load())).ok||e||(e=a)});if(e)throw e.loadError;return d}c.default=k;c.loadAll=
k;c.loadAllChildren=l;Object.defineProperty(c,"__esModule",{value:!0})});