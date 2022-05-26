// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See http://js.arcgis.com/3.32/esri/copyright.txt for details.
//>>built
define("esri/core/timerUtils",[],function(){function e(a){var b,c=a.length;for(b=0;b<c;b++)a[b]()}var c={LOW:1,HIGH:2},d,b={};b[c.LOW]=[];b[c.HIGH]=[];var f=function(){clearTimeout(d);d=null;var a=b[c.HIGH];b[c.HIGH]=[];e(a);a=b[c.LOW];b[c.LOW]=[];e(a)};return{priority:c,callbackQueue:b,setTimeout:function(a,c){a=[c,b[c].push(a)-1];d||(d=setTimeout(f,0));return a},clearTimeout:function(a){a&&b[a[0]].splice(a[1],1)}}});