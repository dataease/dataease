// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(function(){return function(){function k(a){this.readFile=a}var g=k.prototype;g.resolveIncludes=function(a){return this.resolve(a)};g.resolve=function(a,b=new Map){if(b.has(a))return b.get(a);const c=this.read(a);if(!c)throw Error(`cannot find shader file ${a}`);const l=/^[^\S\n]*#include\s+<(\S+)>[^\S\n]?/gm;let d=l.exec(c);const m=[];for(;null!=d;)m.push({path:d[1],start:d.index,length:d[0].length}),d=l.exec(c);let h=0,e="";m.forEach(f=>{e+=c.slice(h,f.start);e+=b.has(f.path)?"":this.resolve(f.path,
b);h=f.start+f.length});e+=c.slice(h);b.set(a,e);return e};g.read=function(a){return this.readFile(a)};return k}()});