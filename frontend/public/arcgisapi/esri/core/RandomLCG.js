// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../chunks/_rollupPluginBabelHelpers"],function(f){let e=function(){function a(b=1){this._seed=b}var c=a.prototype;c.getInt=function(){return this._seed=(a._a*this._seed+a._c)%a._m};c.getFloat=function(){return this.getInt()/(a._m-1)};c.getIntRange=function(b,d){return Math.round(this.getFloatRange(b,d))};c.getFloatRange=function(b,d){d-=b;const g=this.getInt()/a._m;return b+Math.floor(g*d)};f._createClass(a,[{key:"seed",set:function(b){this._seed=null==b?Math.random()*a._m:b}}]);return a}();
e._m=2147483647;e._a=48271;e._c=0;return e});