// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["require","exports","../../core/promiseUtils","../../support/requestImageUtils"],function(g,h,l,k){h.loadMagnifierResources=async function(b){var f=new Promise(function(c,d){g(["./mask-svg"],function(e){c(Object.freeze({__proto__:null,"default":e}))},d)}),a=new Promise(function(c,d){g(["./overlay-svg"],function(e){c(Object.freeze({__proto__:null,"default":e}))},d)});f=k.requestImage((await f).default,{signal:b});a=k.requestImage((await a).default,{signal:b});a={mask:await f,overlay:await a};
l.throwIfAborted(b);return a};Object.defineProperty(h,"__esModule",{value:!0})});