// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/styles/utils",["dojo/_base/lang","dojo/_base/array","dojo/has","../kernel"],function(g,e,h,k){function f(a,c){return a.r===c.r&&a.g===c.g&&a.b===c.b}var b={haveIdenticalColors:function(a,c){var b,d=0;a.length===c.length&&((b=e.every(a,function(a,b){return f(a,c[b])}))?d=1:(a=a.slice(0).reverse(),(b=e.every(a,function(a,b){return f(a,c[b])}))&&(d=-1)));return d}};h("extend-esri")&&g.setObject("styles.utils",b,k);return b});