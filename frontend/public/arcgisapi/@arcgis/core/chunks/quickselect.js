/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
import{c as o}from"./_commonjsHelpers.js";var r=o((function(o){var r;void 0!==(r=function(){function o(o,t,a,f,i){r(o,t,a||0,f||o.length-1,i||n)}function r(o,n,a,f,i){for(;f>a;){if(f-a>600){var c=f-a+1,e=n-a+1,h=Math.log(c),u=.5*Math.exp(2*h/3),s=.5*Math.sqrt(h*u*(c-u)/c)*(e-c/2<0?-1:1);r(o,n,Math.max(a,Math.floor(n-e*u/c+s)),Math.min(f,Math.floor(n+(c-e)*u/c+s)),i)}var M=o[n],m=a,v=f;for(t(o,a,n),i(o[f],M)>0&&t(o,a,f);m<v;){for(t(o,m,v),m++,v--;i(o[m],M)<0;)m++;for(;i(o[v],M)>0;)v--}0===i(o[a],M)?t(o,a,v):t(o,++v,f),v<=n&&(a=v+1),n<=v&&(f=v-1)}}function t(o,r,t){var n=o[r];o[r]=o[t],o[t]=n}function n(o,r){return o<r?-1:o>r?1:0}return o}())&&(o.exports=r)}));export{r as q};
