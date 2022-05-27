/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
function n(){const n=new Float32Array(6);return n[0]=1,n[3]=1,n}function t(n,t,o,c){const r=t[c],e=t[c+1];n[c]=o[0]*r+o[2]*e+o[4],n[c+1]=o[1]*r+o[3]*e+o[5]}function o(n,o,c,r=0,e=0,s=2){const a=e||o.length/s;for(let e=r;e<a;e++){t(n,o,c,e*s)}}export{n as c,o as t};
