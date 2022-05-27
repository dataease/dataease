// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports"],function(g){var f=function(d,e){for(var a=0,b=d.length;a<b;a++){var c=d[a];Array.isArray(c)?f(c,e):null!==c&&void 0!==c&&!1!==c&&(c.hasOwnProperty("vnodeSelector")||(c={vnodeSelector:"",properties:void 0,children:void 0,text:c.toString(),domNode:null}),e.push(c))}};g.jsx=function(d,e){for(var a=[],b=2;b<arguments.length;b++)a[b-2]=arguments[b];if(1===a.length&&"string"===typeof a[0])return{vnodeSelector:d,properties:e||void 0,children:void 0,text:a[0],domNode:null};b=[];f(a,b);
return{vnodeSelector:d,properties:e||void 0,children:b,text:void 0,domNode:null}}});