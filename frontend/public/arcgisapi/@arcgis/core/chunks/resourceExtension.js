/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{a as t}from"./object.js";import{getPathExtension as n}from"../core/urlUtils.js";function i(){const t=o.getRandomValues(new Uint16Array(8));t[3]=4095&t[3]|16384,t[4]=16383&t[4]|32768;const n=n=>t[n].toString(16);return n(0)+n(1)+"-"+n(2)+"-"+n(3)+"-"+n(4)+"-"+n(5)+n(6)+n(7)}const o=t.crypto||t.msCrypto;function p(t){return a[function(t){if(t instanceof Blob)return t.type;return function(t){const i=n(t);return g[i]||e}(t.url)}(t)]||r}const a={},e="text/plain",r=a[e],g={png:"image/png",jpeg:"image/jpeg",jpg:"image/jpg",bmp:"image/bmp",gif:"image/gif",json:"application/json",txt:"text/plain",xml:"application/xml",svg:"image/svg+xml",zip:"application/zip",pbf:"application/vnd.mapbox-vector-tile",gz:"application/gzip"};for(const t in g)a[g[t]]=t;export{p as a,i as g};
