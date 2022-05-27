// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../../chunks/_rollupPluginBabelHelpers"],function(f){return function(){function b(){this._scales=[-1,-1,-1,-1];this._offsets=[-1,-1,-1,-1]}var c=b.prototype;c.clear=function(){this._scales[0]=this._scales[1]=this._scales[2]=this._scales[3]=-1;this._offsets[0]=this._offsets[1]=this._offsets[2]=this._offsets[3]=-1};c.setScale=function(a,d,e){this._scales[2*a]=d;this._scales[2*a+1]=e};c.setOffset=function(a,d,e){this._offsets[2*a]=d;this._offsets[2*a+1]=e};f._createClass(b,[{key:"scales",
get:function(){return this._scales}},{key:"offsets",get:function(){return this._offsets}}]);return b}()});