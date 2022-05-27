// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../chunks/_rollupPluginBabelHelpers"],function(d){return function(){function b(a,c=29){this.name=a;this._counter=0;this._items=Array(c)}b.prototype.record=function(a){this._items[++this._counter%this._items.length]=a};d._createClass(b,[{key:"median",get:function(){return this._items.slice().sort()[Math.floor(this._items.length/2)]}},{key:"average",get:function(){return this._items.reduce((a,c)=>a+c,0)/this._items.length}},{key:"last",get:function(){return this._items[this._counter%this._items.length]}}]);
return b}()});