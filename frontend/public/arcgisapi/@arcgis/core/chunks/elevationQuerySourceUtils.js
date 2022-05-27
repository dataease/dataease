/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{d as n,b as r,i as t}from"./Logger.js";import{l,k as s}from"../core/scheduling.js";import{a as o}from"./unitUtils.js";function e(t){const s=n(t,(n=>n.tileInfo));if(r(s))return null;const e=l(s.lods);return r(e)?null:e.resolution*o(s.spatialReference)}function i(n){var l;if(r(n))return null;const o=n.layers.items.map(u).filter(t);return null!=(l=s(o))?l:null}function u(n){return"tileInfo"in n?e(n):null}export{e as a,i as g};
