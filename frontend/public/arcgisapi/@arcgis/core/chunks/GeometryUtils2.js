/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
const n=128/Math.PI,t=1/Math.LN2;function r(n,t){return(n%=t)>=0?n:n+t}function u(t){return r(t*n,256)}function o(n){return r(.7111111111111111*n,256)}function a(n){return Math.log(n)*t}function c(n,t,r){return n>=t&&n<=r||n>=r&&n<=t}export{c as b,o as d,a as l,u as r};
