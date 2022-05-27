// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["./global"],function(e){function f(b){c.push(b);1===c.length&&h(()=>{for(var a of d)a();a=c.slice();c.length=0;for(const g of a)g()})}const h=function(){return e.queueMicrotask?e.queueMicrotask:b=>{e.Promise.resolve().then(b)}}(),c=[];let d=[];(function(b){b.before=function(a){d.push(a);return{remove(){d=d.filter(g=>g!==a)}}}})(f||(f={}));return f});