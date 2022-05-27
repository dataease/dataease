// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(function(){return function(){function d(a=[]){this._elements=a}var b=d.prototype;b.length=function(){return this._elements.length};b.get=function(a){return this._elements[a]};b.toArray=function(){const a=[];for(let c=0;c<this.length();c++)a.push(this.get(c));return a};return d}()});