/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{j as t,a as n}from"./unitUtils.js";function e(e,a){const i=a||e.extent,r=e.width,s=n(i&&i.spatialReference);return i&&r?i.width/r*s*t*96:0}function a(e,a){return e/(n(a)*t*96)}function i(e,a){return e*(n(a)*t*96)}function r(t,n){const e=t.extent,i=t.width,r=a(n,e.spatialReference);return e.clone().expand(r*i/e.width)}export{i as a,a as b,r as c,e as g};
