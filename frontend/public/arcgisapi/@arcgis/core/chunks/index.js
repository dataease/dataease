/*
All material copyright ESRI, All Rights Reserved, unless otherwise specified.
See https://js.arcgis.com/4.18/esri/copyright.txt for details.
*/
var e=function(r,o){for(var t=0,n=r.length;t<n;t++){var d=r[t];Array.isArray(d)?e(d,o):null!=d&&!1!==d&&(d.hasOwnProperty("vnodeSelector")||(d={vnodeSelector:"",properties:void 0,children:void 0,text:d.toString(),domNode:null}),o.push(d))}},r=function(r,o){for(var t=[],n=2;n<arguments.length;n++)t[n-2]=arguments[n];if(1===t.length&&"string"==typeof t[0])return{vnodeSelector:r,properties:o||void 0,children:void 0,text:t[0],domNode:null};var d=[];return e(t,d),{vnodeSelector:r,properties:o||void 0,children:d,text:void 0,domNode:null}};export{r as j};
