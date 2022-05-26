// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/promiseList",["dojo/_base/array","dojo/Deferred","dojo/when"],function(g,k,m){var n=g.forEach;return function(e){function l(a,c){b[c]=a;d.progress([a,c]);0===--g&&d.resolve(b)}var c,a;e instanceof Array?a=e:e&&"object"===typeof e&&(c=e);var b,f=[];if(c){a=[];for(var h in c)Object.hasOwnProperty.call(c,h)&&(f.push(h),a.push(c[h]));b={}}else a&&(b=[]);if(!a||!a.length)return(new k).resolve(b);var d=new k;d.promise.always(function(){b=f=null});var g=a.length;n(a,function(a,b){c||f.push(b);
m(a,function(a){d.isFulfilled()||l(a,f[b])},function(a){d.isFulfilled()||l(a,f[b])})});return d.promise}});